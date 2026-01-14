package org.example.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

import org.example.dto.ProductResponse;
import org.example.model.Product;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ProductService {

    Optional<Product> findProductById(Long id);
    Page<Product> findAllProducts(Pageable pageable);
    Product saveProduct(Product product);
    boolean deleteProductById(Long id);

    Product createProductWithImages(ProductResponse productDto) throws IOException;

    Optional<Product> updateProduct(Long id, ProductResponse productDto);

}
