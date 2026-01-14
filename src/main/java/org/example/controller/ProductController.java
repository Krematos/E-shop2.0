package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ProductResponse;
import org.example.mapper.ProductMapper;
import org.example.model.Product;
import org.example.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    /**
     * 🔍 Získání produktu podle ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{} - Získání produktu podle ID", id);
        return productService.findProductById(id)
                .map(productMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ➕ Vytvoření nového produktu (pouze ADMIN).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')") // Pouze admin může přidávat produkty
    public ResponseEntity<ProductResponse> createProduct(@ModelAttribute ProductResponse productDto) throws IOException {
        log.info("POST /api/products - Vytvoření nového produktu: {}", productDto.name());
        if(productDto.imagesFilenames() == null || productDto.imagesFilenames().isEmpty()){
            log.warn("Produkt nemá obrázky.");
        }

        Product savedProduct = productService.createProductWithImages(productDto);
        return new ResponseEntity<>(productMapper.toDto(savedProduct), HttpStatus.CREATED);
    }

    /**
     * ♻️ Aktualizace existujícího produktu (pouze ADMIN).
     */
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')") // Pouze admin může aktualizovat produkty
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @ModelAttribute ProductResponse productDto) {
        log.info("PUT /api/products/{} - Aktualizace produktu: {}", id, productDto);
        return productService.updateProduct(id, productDto)
                .map(productMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * ❌ Smazání produktu podle ID (pouze ADMIN).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Pouze admin může mazat produkty
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{} - Smazání produktu", id);
        return productService.deleteProductById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * 📋 Získání seznamu všech produktů.
     */
    @GetMapping
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("GET /api/products - Získání seznamu všech produktů s paginací: {}", pageable);
        return productService.findAllProducts(pageable)
                .map(productMapper::toDto);
    }

}
