package org.example.controller;

import org.example.dto.product.ProductResponse;
import org.example.mapper.ProductMapper;
import org.example.model.Product;
import org.example.service.JwtService;
import org.example.service.ProductService;
import org.example.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.example.service.user.UserService;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simplicity in this unit test
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductMapper productMapper;

    @MockBean
    private JwtService jwtService; // Required because of security config

    @MockBean
    private UserDetailsServiceImpl userDetailsService; // Required because of security config

    @MockBean
    private AuthenticationManager authenticationManager; // Required for AuthController

    @MockBean
    private UserService userService; // Required for AuthController

    @Test
    void getProductById_Success() throws Exception {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");

        ProductResponse productDto = new ProductResponse(
                productId,
                "Test Product",
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        when(productService.findProductById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productDto);

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getProductById_NotFound() throws Exception {
        Long productId = 999L;
        when(productService.findProductById(productId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProducts_Success() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        ProductResponse productDto = new ProductResponse(
                1L,
                "Test Product",
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));

        when(productService.findAllProducts(any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toDto(product)).thenReturn(productDto);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }
}
