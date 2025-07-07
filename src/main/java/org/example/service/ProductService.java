package org.example.service;

import org.example.model.Product;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
public interface ProductService {

    Optional<Product> findProductById(Long id);
    List<Product> findAllProducts();
    Product saveProduct(Product product);
    boolean deleteProductById(Long id);

}
