package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mapper.ProductMapper;
import org.example.model.Product;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.example.repository.ProductRepository;
import org.example.service.ProductService;
import org.example.dto.ProductDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {



    private final ProductRepository productRepository;
    private final ProductMapper productMapper;


    /**
     * Vrátí produkt podle ID, s cachováním.
     */

    @Override
    @Cacheable(value = "productsById", key = "#id")
    public Optional<Product> findProductById(Long id) {
        log.info("Hledání produktu podle ID: {}", id);
        return productRepository.findById(id);
    }
    /**
     * Odstraní produkt podle ID, invaliduje cache.
     */
    @Override
    @Transactional
    @CacheEvict(value = {"productsByID", "allProducts"}, allEntries = true)
    public boolean deleteProductById(Long id) {
        if (!productRepository.existsById(id)) {
            log.warn("Odstranění selhalo - produkt s ID {} neexistuje", id);
            return false;

        }
        productRepository.deleteById(id);
        log.warn("Produkt s ID {} byl odstraněn", id);
        return true;
    }
    /**
     * Uloží nový nebo aktualizovaný produkt.
     */

    @Override
    @Transactional
    @CacheEvict(value = {"productsByID", "allProducts"}, allEntries = true)
    public Product saveProduct(Product product) {
        if(product == null){
            throw new IllegalArgumentException("Produkt nesmí být null");
        }
        Product saved = productRepository.save(product);
        log.info("Produkt s ID {} byl uložen/aktualizován", saved.getId());
        return saved;
    }
    /**
     * Vrátí všechny produkty, s cachováním.
     */
    @Override
    @Cacheable(value = "allProducts")
    public List<Product> findAllProducts() {
        log.info("Načítání všech produktů");
        return productRepository.findAll();
    }

    /**
     * Vytvoří nový produkt z DTO.
     */
    @Override
    @Transactional
    @CacheEvict(value = {"productsByID", "allProducts"}, allEntries = true)
    public ProductDto createProduct(ProductDto productDto) {
        if (productDto == null) {
            throw new IllegalArgumentException("ProductDto nesmí být null");
        }
        Product product = productMapper.toEntity(productDto);
        Product saved = productRepository.save(product);
        log.info("Vytvořen nový produkt s ID {}", saved.getId());
        return productMapper.toDto(saved);
    }
}
