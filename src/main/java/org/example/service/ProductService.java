package org.example.service;

import org.example.dto.ProductDto;
import org.example.model.Product;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
public interface ProductService {
    //ProductDto createProduct(ProductDto productDto);

    Optional<Product> findProductById(Long id);
    List<Product> findAllProducts();
    Product saveProduct(Product product);
    boolean deleteProductById(Long id);

    Product createProductWithImages(ProductDto productDto);

}
