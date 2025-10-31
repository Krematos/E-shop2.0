package org.example.controller;


import jakarta.validation.Valid;
import org.example.dto.ProductDto;
import org.example.mapper.ProductMapper;
import org.example.model.Product;
import org.example.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/products")
public class ProductController {



    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    /**
     * 🔍 Získání produktu podle ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return productService.findProductById(id)
                .map(productMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ➕ Vytvoření nového produktu (pouze ADMIN).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Pouze admin může přidávat produkty
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        Product savedProduct = productService.saveProduct(productMapper.toEntity(productDto));
        return new ResponseEntity<>(productMapper.toDto(savedProduct), HttpStatus.CREATED);
    }

    /**
     * ♻️ Aktualizace existujícího produktu (pouze ADMIN).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Pouze admin může aktualizovat produkty
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        return productService.findProductById(id)
                .map(existingProduct -> {
                    productMapper.updateProductFromDto(productDto, existingProduct);
                    Product updatedProduct = productService.saveProduct(existingProduct);
                    return ResponseEntity.ok(productMapper.toDto(updatedProduct));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    /**
     * ❌ Smazání produktu podle ID (pouze ADMIN).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Pouze admin může mazat produkty
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        return productService.deleteProductById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
    /**
     * 📋 Získání seznamu všech produktů.
     */
    @GetMapping
    public List<ProductDto> getAllProducts() {
        return productService.findAllProducts().stream()
                .map(productMapper::toDto)
                .toList();
    }


}
