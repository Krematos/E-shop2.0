package krematos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import krematos.config.SecurityConfig;
import krematos.dto.product.ProductResponse;
import krematos.exception.product.ProductNotFoundException;
import krematos.mapper.ProductMapper;
import krematos.model.Product;
import krematos.service.JwtService;
import krematos.service.ProductService;
import krematos.service.impl.UserDetailsServiceImpl;
import krematos.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit testy pro ProductController.
 * Testuje všechny endpointy controlleru s různými scénáři.
 */
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false) // Vypnutí security filtrů pro unit testy
@DisplayName("ProductController Tests")
@Import(SecurityConfig.class)
class ProductControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ProductService productService;

        @MockBean
        private ProductMapper productMapper;

        // Security dependencies - vyžadovány kvůli security konfiguraci
        @MockBean
        private JwtService jwtService;

        @MockBean
        private UserDetailsServiceImpl userDetailsService;

        @MockBean
        private AuthenticationManager authenticationManager;

        @MockBean
        private UserService userService;

        // Konstanty pro testy
        private static final String API_PRODUCTS_PATH = "/api/products";
        private static final Long VALID_PRODUCT_ID = 1L;
        private static final Long NON_EXISTENT_PRODUCT_ID = 999L;
        private static final String PRODUCT_NAME = "Test Product";
        private static final String PRODUCT_DESCRIPTION = "Test Description";
        private static final BigDecimal PRODUCT_PRICE = new BigDecimal("99.99");
        private static final String PRODUCT_CATEGORY = "Electronics";

        /**
         * Testy pro GET /api/products/{id} - Získání produktu podle ID
         */
        @Nested
        @DisplayName("GET /api/products/{id} - Get Product By ID")
        @WithMockUser(roles = "ADMIN") // Simulace přihlášeného uživatele s rolí ADMIN
        class GetProductByIdTests {

                @Test
                @DisplayName("Měl by vrátit produkt když existuje")
                void shouldReturnProduct_WhenProductExists() throws Exception {
                        // Given
                        Product product = createTestProduct();
                        ProductResponse productResponse = createTestProductResponse();

                        when(productService.findProductById(VALID_PRODUCT_ID)).thenReturn(Optional.of(product));
                        when(productMapper.toDto(product)).thenReturn(productResponse);

                        // When & Then
                        mockMvc.perform(get(API_PRODUCTS_PATH + "/{id}", VALID_PRODUCT_ID)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.id").value(VALID_PRODUCT_ID))
                                        .andExpect(jsonPath("$.name").value(PRODUCT_NAME))
                                        .andExpect(jsonPath("$.description").value(PRODUCT_DESCRIPTION))
                                        .andExpect(jsonPath("$.price").value(PRODUCT_PRICE.doubleValue()))
                                        .andExpect(jsonPath("$.category").value(PRODUCT_CATEGORY));

                        verify(productService, times(1)).findProductById(VALID_PRODUCT_ID);
                        verify(productMapper, times(1)).toDto(product);
                }

                @Test
                @DisplayName("Měl by vrátit 404 když produkt neexistuje")
                void shouldReturnNotFound_WhenProductDoesNotExist() throws Exception {
                        // Given
                        when(productService.findProductById(NON_EXISTENT_PRODUCT_ID)).thenReturn(Optional.empty());

                        // When & Then
                        mockMvc.perform(get(API_PRODUCTS_PATH + "/{id}", NON_EXISTENT_PRODUCT_ID)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isNotFound());

                        verify(productService, times(1)).findProductById(NON_EXISTENT_PRODUCT_ID);
                        verify(productMapper, never()).toDto(any());
                }

                @ParameterizedTest
                @ValueSource(longs = { 1L, 10L, 100L, 1000L })
                @DisplayName("Měl by správně zpracovat různá platná ID")
                void shouldHandleVariousValidIds(Long productId) throws Exception {
                        // Given
                        Product product = createTestProduct();
                        product.setId(productId);
                        ProductResponse productResponse = createTestProductResponse();

                        when(productService.findProductById(productId)).thenReturn(Optional.of(product));
                        when(productMapper.toDto(product)).thenReturn(productResponse);

                        // When & Then
                        mockMvc.perform(get(API_PRODUCTS_PATH + "/{id}", productId)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(VALID_PRODUCT_ID));

                        verify(productService, times(1)).findProductById(productId);
                }
        }

        /**
         * Testy pro GET /api/products - Získání všech produktů s paginací
         */
        @Nested
        @DisplayName("GET /api/products - Get All Products")
        class GetAllProductsTests {

                @Test
                @DisplayName("Měl by vrátit stránku produktů")
                void shouldReturnPageOfProducts() throws Exception {
                        // Given
                        Product product = createTestProduct();
                        ProductResponse productResponse = createTestProductResponse();
                        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));

                        when(productService.findAllProducts(any(Pageable.class))).thenReturn(productPage);
                        when(productMapper.toDto(product)).thenReturn(productResponse);

                        // When & Then
                        mockMvc.perform(get(API_PRODUCTS_PATH)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.content", hasSize(1)))
                                        .andExpect(jsonPath("$.content[0].id").value(VALID_PRODUCT_ID))
                                        .andExpect(jsonPath("$.content[0].name").value(PRODUCT_NAME));

                        verify(productService, times(1)).findAllProducts(any(Pageable.class));
                        verify(productMapper, times(1)).toDto(product);
                }

                @Test
                @DisplayName("Měl by vrátit prázdnou stránku když nejsou žádné produkty")
                void shouldReturnEmptyPage_WhenNoProductsExist() throws Exception {
                        // Given
                        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
                        when(productService.findAllProducts(any(Pageable.class))).thenReturn(emptyPage);

                        // When & Then
                        mockMvc.perform(get(API_PRODUCTS_PATH)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content", hasSize(0)))
                                        .andExpect(jsonPath("$.totalElements").value(0));

                        verify(productService, times(1)).findAllProducts(any(Pageable.class));
                        verify(productMapper, never()).toDto(any());
                }

                @Test
                @DisplayName("Měl by správně zpracovat parametry paginace")
                void shouldHandlePaginationParameters() throws Exception {
                        // Given
                        int page = 1;
                        int size = 5;
                        Product product = createTestProduct();
                        ProductResponse productResponse = createTestProductResponse();
                        Page<Product> productPage = new PageImpl<>(
                                        Collections.singletonList(product),
                                        PageRequest.of(page, size),
                                        10);

                        when(productService.findAllProducts(any(Pageable.class))).thenReturn(productPage);
                        when(productMapper.toDto(product)).thenReturn(productResponse);

                        // When & Then
                        mockMvc.perform(get(API_PRODUCTS_PATH)
                                        .param("page", String.valueOf(page))
                                        .param("size", String.valueOf(size))
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content", hasSize(1)))
                                        .andExpect(jsonPath("$.number").value(page))
                                        .andExpect(jsonPath("$.size").value(size))
                                        .andExpect(jsonPath("$.totalElements").value(10));

                        verify(productService, times(1)).findAllProducts(any(Pageable.class));
                }

                @Test
                @DisplayName("Měl by vrátit více produktů na stránce")
                void shouldReturnMultipleProducts() throws Exception {
                        // Given
                        List<Product> products = List.of(
                                        createTestProduct(),
                                        createTestProductWithId(2L, "Product 2"),
                                        createTestProductWithId(3L, "Product 3"));
                        Page<Product> productPage = new PageImpl<>(products);

                        when(productService.findAllProducts(any(Pageable.class))).thenReturn(productPage);
                        when(productMapper.toDto(any(Product.class))).thenAnswer(invocation -> {
                                Product p = invocation.getArgument(0);
                                return createTestProductResponseWithId(p.getId(), p.getName());
                        });

                        // When & Then
                        mockMvc.perform(get(API_PRODUCTS_PATH)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content", hasSize(3)))
                                        .andExpect(jsonPath("$.content[0].id").value(1L))
                                        .andExpect(jsonPath("$.content[1].id").value(2L))
                                        .andExpect(jsonPath("$.content[2].id").value(3L));

                        verify(productService, times(1)).findAllProducts(any(Pageable.class));
                        verify(productMapper, times(3)).toDto(any(Product.class));
                }
        }

        /**
         * Testy pro POST /api/products - Vytvoření nového produktu
         */
        @Nested
        @DisplayName("POST /api/products - Create Product")
        @WithMockUser(roles = "ADMIN") // Simulace přihlášeného uživatele s rolí ADMIN
        class CreateProductTests {

                @Test
                @DisplayName("Měl by vytvořit produkt s obrázky")
                void shouldCreateProduct_WithImages() throws Exception {
                        // Given
                        MockMultipartFile image1 = new MockMultipartFile(
                                        "imagesFilenames",
                                        "image1.jpg",
                                        MediaType.IMAGE_JPEG_VALUE,
                                        "image1 content".getBytes());

                        MockMultipartFile image2 = new MockMultipartFile(
                                        "imagesFilenames",
                                        "image2.jpg",
                                        MediaType.IMAGE_JPEG_VALUE,
                                        "image2 content".getBytes());

                        Product savedProduct = createTestProduct();
                        ProductResponse productResponse = createTestProductResponse();

                        when(productService.createProductWithImages(any(ProductResponse.class)))
                                        .thenReturn(savedProduct);
                        when(productMapper.toDto(savedProduct)).thenReturn(productResponse);

                        // When & Then
                        mockMvc.perform(multipart(API_PRODUCTS_PATH)
                                        .file(image1)
                                        .file(image2)
                                        .param("name", PRODUCT_NAME)
                                        .param("description", PRODUCT_DESCRIPTION)
                                        .param("price", PRODUCT_PRICE.toString())
                                        .param("category", PRODUCT_CATEGORY)
                                        .contentType(MediaType.MULTIPART_FORM_DATA))
                                        .andExpect(status().isCreated())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.id").value(VALID_PRODUCT_ID))
                                        .andExpect(jsonPath("$.name").value(PRODUCT_NAME));

                        verify(productService, times(1)).createProductWithImages(any(ProductResponse.class));
                        verify(productMapper, times(1)).toDto(savedProduct);
                }

                @Test
                @DisplayName("Měl by vytvořit produkt bez obrázků")
                void shouldCreateProduct_WithoutImages() throws Exception {
                        // Given
                        Product savedProduct = createTestProduct();
                        ProductResponse productResponse = createTestProductResponse();

                        when(productService.createProductWithImages(any(ProductResponse.class)))
                                        .thenReturn(savedProduct);
                        when(productMapper.toDto(savedProduct)).thenReturn(productResponse);

                        // When & Then
                        mockMvc.perform(multipart(API_PRODUCTS_PATH)
                                        .param("name", PRODUCT_NAME)
                                        .param("description", PRODUCT_DESCRIPTION)
                                        .param("price", PRODUCT_PRICE.toString())
                                        .param("category", PRODUCT_CATEGORY)
                                        .contentType(MediaType.MULTIPART_FORM_DATA))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").value(VALID_PRODUCT_ID))
                                        .andExpect(jsonPath("$.name").value(PRODUCT_NAME));

                        verify(productService, times(1)).createProductWithImages(any(ProductResponse.class));
                }
        }

        /**
         * Testy pro PUT /api/products/{id} - Aktualizace produktu
         */
        @Nested
        @DisplayName("PUT /api/products/{id} - Update Product")
        @WithMockUser(roles = "ADMIN") // Simulace přihlášeného uživatele s rolí ADMIN
        class UpdateProductTests {

                @Test
                @DisplayName("Měl by aktualizovat existující produkt")
                void shouldUpdateProduct_WhenProductExists() throws Exception {
                        // Given
                        Product updatedProduct = createTestProduct();
                        updatedProduct.setName("Updated Product");
                        ProductResponse productResponse = createTestProductResponseWithId(VALID_PRODUCT_ID,
                                        "Updated Product");

                        when(productService.updateProduct(eq(VALID_PRODUCT_ID), any(ProductResponse.class)))
                                        .thenReturn(Optional.of(updatedProduct));
                        when(productMapper.toDto(updatedProduct)).thenReturn(productResponse);

                        // When & Then
                        mockMvc.perform(multipart(API_PRODUCTS_PATH + "/{id}", VALID_PRODUCT_ID)
                                        .with(request -> {
                                                request.setMethod("PUT");
                                                return request;
                                        })
                                        .param("name", "Updated Product")
                                        .param("description", PRODUCT_DESCRIPTION)
                                        .param("price", PRODUCT_PRICE.toString())
                                        .param("category", PRODUCT_CATEGORY)
                                        .contentType(MediaType.MULTIPART_FORM_DATA))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.id").value(VALID_PRODUCT_ID))
                                        .andExpect(jsonPath("$.name").value("Updated Product"));

                        verify(productService, times(1)).updateProduct(eq(VALID_PRODUCT_ID),
                                        any(ProductResponse.class));
                        verify(productMapper, times(1)).toDto(updatedProduct);
                }

                @Test
                @DisplayName("Měl by vrátit 404 při aktualizaci neexistujícího produktu")
                void shouldReturnNotFound_WhenUpdatingNonExistentProduct() throws Exception {
                        // Given
                        when(productService.updateProduct(eq(NON_EXISTENT_PRODUCT_ID), any(ProductResponse.class)))
                                        .thenReturn(Optional.empty());

                        // When & Then
                        mockMvc.perform(multipart(API_PRODUCTS_PATH + "/{id}", NON_EXISTENT_PRODUCT_ID)
                                        .with(request -> {
                                                request.setMethod("PUT");
                                                return request;
                                        })
                                        .param("name", "Updated Product")
                                        .param("description", PRODUCT_DESCRIPTION)
                                        .param("price", PRODUCT_PRICE.toString())
                                        .param("category", PRODUCT_CATEGORY)
                                        .contentType(MediaType.MULTIPART_FORM_DATA))
                                        .andExpect(status().isNotFound());

                        verify(productService, times(1)).updateProduct(eq(NON_EXISTENT_PRODUCT_ID),
                                        any(ProductResponse.class));
                        verify(productMapper, never()).toDto(any());
                }

                @Test
                @DisplayName("Měl by aktualizovat produkt s novými obrázky")
                void shouldUpdateProduct_WithNewImages() throws Exception {
                        // Given
                        MockMultipartFile newImage = new MockMultipartFile(
                                        "imagesFilenames",
                                        "new-image.jpg",
                                        MediaType.IMAGE_JPEG_VALUE,
                                        "new image content".getBytes());

                        Product updatedProduct = createTestProduct();
                        ProductResponse productResponse = createTestProductResponse();

                        when(productService.updateProduct(eq(VALID_PRODUCT_ID), any(ProductResponse.class)))
                                        .thenReturn(Optional.of(updatedProduct));
                        when(productMapper.toDto(updatedProduct)).thenReturn(productResponse);

                        // When & Then
                        mockMvc.perform(multipart(API_PRODUCTS_PATH + "/{id}", VALID_PRODUCT_ID)
                                        .file(newImage)
                                        .with(request -> {
                                                request.setMethod("PUT");
                                                return request;
                                        })
                                        .param("name", PRODUCT_NAME)
                                        .param("description", PRODUCT_DESCRIPTION)
                                        .param("price", PRODUCT_PRICE.toString())
                                        .param("category", PRODUCT_CATEGORY)
                                        .contentType(MediaType.MULTIPART_FORM_DATA))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(VALID_PRODUCT_ID));

                        verify(productService, times(1)).updateProduct(eq(VALID_PRODUCT_ID),
                                        any(ProductResponse.class));
                }
        }

        /**
         * Testy pro DELETE /api/products/{id} - Smazání produktu
         */
        @Nested
        @DisplayName("DELETE /api/products/{id} - Delete Product")
        @WithMockUser(roles = "ADMIN") // Simulace přihlášeného uživatele s rolí ADMIN
        class DeleteProductTests {

                @Test
                @DisplayName("Měl by smazat existující produkt")
                void shouldDeleteProduct_WhenProductExists() throws Exception {

                        // Given
                        Long productId = 1L;
                        when(productService.deleteProductById(productId)).thenReturn(true);

                        // When & Then
                        mockMvc.perform(delete("/api/products/{id}", productId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNoContent());
                }

                @Test
                @DisplayName("Měl by vrátit 404 při mazání neexistujícího produktu")
                void shouldReturnNotFound_WhenDeletingNonExistentProduct() throws Exception {
                        // Given
                        Long nonExistentId = 999L;
                        Mockito.doThrow(new ProductNotFoundException("Produkt nenalezen"))
                                .when(productService).deleteProductById(nonExistentId);

                        // When & Then
                        mockMvc.perform(delete("/api/products/{id}", nonExistentId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
                }

                @ParameterizedTest
                @ValueSource(longs = { 1L, 5L, 10L, 100L })
                @DisplayName("Měl by správně zpracovat mazání různých ID")
                void shouldHandleDeletionOfVariousIds(Long productId) throws Exception {
                        // Given
                        when(productService.deleteProductById(productId)).thenReturn(true);

                        // When & Then
                        mockMvc.perform(delete(API_PRODUCTS_PATH + "/{id}", productId)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isNoContent());

                        verify(productService, times(1)).deleteProductById(productId);
                }
        }

        // ========== Helper Methods ==========

        /**
         * Vytvoří testovací Product entitu s výchozími hodnotami
         */
        private Product createTestProduct() {
                Product product = new Product();
                product.setId(VALID_PRODUCT_ID);
                product.setName(PRODUCT_NAME);
                product.setDescription(PRODUCT_DESCRIPTION);
                product.setPrice(PRODUCT_PRICE);
                product.setCategory(PRODUCT_CATEGORY);
                product.setCreatedAt(Instant.now());
                product.setUpdatedAt(Instant.now());
                return product;
        }

        /**
         * Vytvoří testovací Product entitu s vlastním ID a názvem
         */
        private Product createTestProductWithId(Long id, String name) {
                Product product = createTestProduct();
                product.setId(id);
                product.setName(name);
                return product;
        }

        /**
         * Vytvoří testovací ProductResponse DTO s výchozími hodnotami
         */
        private ProductResponse createTestProductResponse() {
                return new ProductResponse(
                                VALID_PRODUCT_ID,
                                PRODUCT_NAME,
                                PRODUCT_DESCRIPTION,
                                PRODUCT_PRICE,
                                PRODUCT_CATEGORY,
                                null, // imagesFilenames
                                List.of("image1.jpg", "image2.jpg"), // images
                                Instant.now(),
                                Instant.now());
        }

        /**
         * Vytvoří testovací ProductResponse DTO s vlastním ID a názvem
         */
        private ProductResponse createTestProductResponseWithId(Long id, String name) {
                return new ProductResponse(
                                id,
                                name,
                                PRODUCT_DESCRIPTION,
                                PRODUCT_PRICE,
                                PRODUCT_CATEGORY,
                                null,
                                List.of("image1.jpg", "image2.jpg"),
                                Instant.now(),
                                Instant.now());
        }
}
