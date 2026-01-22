package org.example.service;

import org.example.mapper.ProductMapper;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.example.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void findProductById_Success() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Optional<Product> foundProduct = productService.findProductById(productId);

        assertTrue(foundProduct.isPresent());
        assertEquals(productId, foundProduct.get().getId());
        assertEquals("Test Product", foundProduct.get().getName());
    }

    @Test
    void findProductById_NotFound() {
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Optional<Product> foundProduct = productService.findProductById(productId);

        assertFalse(foundProduct.isPresent());
    }

    @Test
    void deleteProductById_Success() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setImages(new ArrayList<>()); // Initialize images list to avoid NPE

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsById(productId)).thenReturn(true);

        boolean result = productService.deleteProductById(productId);

        assertTrue(result);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void deleteProductById_NotFound() {
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        when(productRepository.existsById(productId)).thenReturn(false);

        boolean result = productService.deleteProductById(productId);

        assertFalse(result);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void findAllProducts_Success() {
        Product product = new Product();
        product.setId(1L);
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        Page<Product> result = productService.findAllProducts(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals(product.getId(), result.getContent().get(0).getId());
    }
    /*
    @Test
    void createProductWithImages_Success() throws IOException {
        ProductResponse productDto = new ProductResponse();
        productDto.name("New Product");
        productDto.images(new ArrayList<>()); // Empty list of images

        Product product = new Product();
        product.setName("New Product");
        product.setImages(new ArrayList<>());

        when(productMapper.toEntity(productDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);

        Product createdProduct = productService.createProductWithImages(productDto);

        assertNotNull(createdProduct);
        assertEquals("New Product", createdProduct.getName());
        verify(productRepository, times(1)).save(product);
    }*/
}
