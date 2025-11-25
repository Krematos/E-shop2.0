package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ProductDto;
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

    @Value("${app.upload.url:/uploads/}")
    private String uploadUrl;

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
        if (!productRepository.existsById(id)) {
            log.warn("Odstranění selhalo - produkt s ID {} neexistuje", id);
            return false;
        }
        productRepository.deleteById(id);
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
    public Product createProductWithImages(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        if (productDto.getImages() != null && !productDto.getImages().isEmpty()) {
            for (MultipartFile file : productDto.getImages()) {
                if (file != null && !file.isEmpty()) {
                    validateFile(file);
                    String fileName = saveFile(file);
                    product.getImages().add(uploadUrl + fileName);
                }
            }
        }
        return productRepository.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsById", "allProducts"}, allEntries = true)
    public Optional<Product> updateProduct(Long id, ProductDto productDto) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    productMapper.updateProductFromDto(productDto, existingProduct);
                    if (productDto.getImages() != null && !productDto.getImages().isEmpty()) {
                        // This is a simplified approach. In a real application, you'd probably
                        // want to delete the old images from the filesystem.
                        existingProduct.getImages().clear();
                        for (MultipartFile file : productDto.getImages()) {
                            if (file != null && !file.isEmpty()) {
                                validateFile(file);
                                String fileName = saveFile(file);
                                existingProduct.getImages().add(uploadUrl + fileName);
                            }
                        }
                    }
                    Product updatedProduct = productRepository.save(existingProduct);
                    return updatedProduct;
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

    private String saveFile(MultipartFile file) {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
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
}
