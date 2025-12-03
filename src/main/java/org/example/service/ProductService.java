package org.example.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import org.example.dto.ProductDto;
import org.example.model.Product;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ProductService {

    Optional<Product> findProductById(Long id);
    Page<Product> findAllProducts(Pageable pageable);
    Product saveProduct(Product product);
    boolean deleteProductById(Long id);

    Product createProductWithImages(ProductDto productDto) throws IOException;

    Optional<Product> updateProduct(Long id, ProductDto productDto);

}
