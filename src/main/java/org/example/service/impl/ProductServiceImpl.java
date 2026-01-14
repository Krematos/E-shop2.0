package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ProductResponse;
import org.example.mapper.ProductMapper;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.example.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    @Cacheable(value = "productsById", key = "#id")
    public Optional<Product> findProductById(Long id) {
        log.info("Hledání produktu podle ID: {}", id);
        return productRepository.findById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsById", "allProducts"}, allEntries = true)
    public boolean deleteProductById(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (!productRepository.existsById(id)) {
            log.warn("Odstranění selhalo - produkt s ID {} neexistuje", id);
            return false;
        }
        Product product = productOpt.get();
        // Smazat soubory obrázků z filesystemu
        for (String fileName : product.getImages()) {
            deleteImageFile(fileName);
        }

        // Odstranit produkt z databáze
        productRepository.delete(product);
        log.warn("Produkt s ID {} byl odstraněn", id);
        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsById", "allProducts"}, allEntries = true)
    public Product saveProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Produkt nesmí být null");
        }
        Product saved = productRepository.save(product);
        log.info("Produkt s ID {} byl uložen/aktualizován", saved.getId());
        return saved;
    }

    @Override
    @Cacheable(value = "allProducts", key = "#pageable")
    public Page<Product> findAllProducts(Pageable pageable) {
        log.info("Načítání stránky produktů: {}", pageable);
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsById", "allProducts"}, allEntries = true)
    public Product createProductWithImages(ProductResponse productDto) throws IOException {
        Product product = productMapper.toEntity(productDto);
        if (productDto.imagesFilenames() != null && !productDto.imagesFilenames().isEmpty()) {
            for (MultipartFile file : productDto.imagesFilenames()) {
                if (file != null && !file.isEmpty()) {
                    validateFile(file);
                    String fileName = saveFile(file);
                    product.getImages().add(fileName);
                }
            }
        }
        return productRepository.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsById", "allProducts"}, allEntries = true)
    public Optional<Product> updateProduct(Long id, ProductResponse productDto) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    productMapper.updateProductFromDto(productDto, existingProduct);
                    if (productDto.imagesFilenames() != null && !productDto.imagesFilenames().isEmpty()) {
                        // Delete old images from filesystem
                        for(String oldImage : existingProduct.getImages()){
                            deleteImageFile(oldImage);
                        }
                        existingProduct.getImages().clear();
                        // Add new images
                        for (MultipartFile file : productDto.imagesFilenames()) {
                            if (file != null && !file.isEmpty()) {
                                validateFile(file);
                                String fileName = null;
                                try {
                                    fileName = saveFile(file);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                existingProduct.getImages().add(fileName);
                            }
                        }
                    }
                    return productRepository.save(existingProduct);
                });
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Soubor nesmí být prázdný.");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Nepovolený typ souboru: " + file.getContentType());
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        if(!Files.exists(Paths.get(uploadDir))){
            Files.createDirectories(Paths.get(uploadDir));
        }
        String sanitizedName = file.getOriginalFilename()
                .replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        String fileName = UUID.randomUUID().toString() + "_" + sanitizedName;
        Path uploadPath = Paths.get(uploadDir);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Soubor {} byl uložen", fileName);
        } catch (IOException e) {
            log.error("Chyba při ukládání souboru {}: {}", fileName, e.getMessage());
            throw new RuntimeException("Chyba při ukládání souboru", e);
        }
        return fileName;
    }

    public void deleteImageFile(String fileName){
        if(fileName == null || fileName.isEmpty()){
            log.warn("Název souboru pro smazání je prázdný");
            return;
        }
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
            log.info("Soubor {} byl smazán", fileName);
        } catch (IOException e) {
            log.error("Chyba při mazání souboru {}: {}", fileName, e.getMessage());
        }
    }

}
