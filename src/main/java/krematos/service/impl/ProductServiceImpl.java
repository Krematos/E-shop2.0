package krematos.service.impl;

import jakarta.annotation.PostConstruct;
import krematos.exception.product.FileStorageException;
import krematos.exception.product.InvalidFileException;
import krematos.exception.product.ProductImageFileIsTooBig;
import krematos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import krematos.dto.product.ProductResponse;
import krematos.mapper.ProductMapper;
import krematos.model.Product;
import krematos.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");
    private static final long MAX_FILE_SIZE = (long) 5 * 1024 * 1024; // 5MB

    // Inicializace složky při startu aplikace
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new InvalidFileException("Nelze vytvořit adresář pro upload");
        }
    }

    @Override
    @Cacheable(value = "productsById", key = "#id")
    @Transactional(readOnly = true)
    public Optional<Product> findProductById(Long id) {
        log.info("Hledání produktu podle ID: {}", id);
        return productRepository.findById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsById", "allProducts"}, allEntries = true)
    public boolean deleteProductById(Long id) {
        return productRepository.findById(id).map(product -> {

            // Uloží seznam souborů ke smazání
            List<String> imagesToDelete = new ArrayList<>(product.getImages());
            productRepository.delete(product);

            // Smazání souborů až PO commitu transakce
            deleteFilesAfterCommit(imagesToDelete);
            log.warn("Produkt s ID {} byl odstraněn", id);
            return true;
        }).orElseGet(() -> {
            log.warn("Odstranění selhalo - produkt s ID {} neexistuje", id);
            return false;
        });
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsById", "allProducts"}, allEntries = true)
    public Product saveProduct(Product product) {
        if (product == null) throw new InvalidFileException("Produkt nesmí být null");
        Product saved = productRepository.save(product);
        log.info("Produkt s ID {} byl uložen/aktualizován", saved.getId());
        return saved;
    }

    @Override
    @Cacheable(value = "allProducts", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    @Transactional(readOnly = true)
    public Page<Product> findAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsById", "allProducts"}, allEntries = true)
    public Product createProductWithImages(ProductResponse productDto) { // IOException řešíme uvnitř
        Product product = productMapper.toEntity(productDto);
        List<String> savedFiles = processImages(productDto.imagesFilenames());
        product.getImages().addAll(savedFiles);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsById", "allProducts"}, allEntries = true)
    public Optional<Product> updateProduct(Long id, ProductResponse productDto) {
        return productRepository.findById(id).map(existingProduct -> {
            productMapper.updateProductFromDto(productDto, existingProduct);

            if (productDto.imagesFilenames() != null && !productDto.imagesFilenames().isEmpty()) {
                List<String> newFiles = processImages(productDto.imagesFilenames());
                existingProduct.getImages().addAll(newFiles);
            }
            return productRepository.save(existingProduct);
        });
    }

    // --- Helper Methods ---

    private List<String> processImages(List<MultipartFile> files) {
        List<String> fileNames = new ArrayList<>();
        if (files == null || files.isEmpty()) return fileNames;

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                validateFile(file);
                try {
                    fileNames.add(saveFile(file));
                } catch (IOException e) {
                    throw new FileStorageException("Chyba při ukládání souboru: " + file.getOriginalFilename(), e);
                }
            }
        }
        return fileNames;
    }

    private void validateFile(MultipartFile file) {
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException("Nepovolený typ souboru: " + file.getContentType());
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ProductImageFileIsTooBig("Soubor je příliš velký. Max 5MB.");
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Soubor není přiložen");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new InvalidFileException("Soubor nemá platný název");
        }

        String sanitizedName = originalName.replaceAll(
                "[^a-zA-Z0-9\\.\\-]",
                "_"
        );

        String fileName = UUID.randomUUID() + "_" + sanitizedName;
        Path filePath = Paths.get(uploadDir).resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Soubor {} byl uložen", fileName);
        return fileName;
    }

    // Bezpečné mazání souborů spřažené s transakcí
    private void deleteFilesAfterCommit(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) return;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String fileName : fileNames) {
                    deleteImageFile(fileName);
                }
            }
        });
    }

    public void deleteImageFile(String fileName) {
        // 1. Validace
        if (fileName == null || fileName.isBlank()) {
            log.warn("Pokus o smazání souboru s prázdným názvem - ignoruji.");
            return;
        }

        // 2. Sanitizace: Odstraní mezery na začátku a na konci
        String sanitizedName = fileName.trim();

        try {
            Path filePath = Paths.get(uploadDir).resolve(sanitizedName);
            Files.deleteIfExists(filePath);
            log.info("Soubor {} byl smazán z disku", sanitizedName);
        } catch (java.nio.file.InvalidPathException e) {
            // 3. Záchranná síť: Pokud je jméno i po ořezání neplatné (např. obsahuje nepovolené znaky)
            log.warn("Neplatný formát cesty k souboru '{}': {}", fileName, e.getMessage());
        } catch (IOException e) {
            log.error("Nepodařilo se smazat soubor {}: {}", sanitizedName, e.getMessage());
        }
    }

}
