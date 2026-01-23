package krematos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import krematos.model.Product;
import krematos.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrační testy pro produktovou logiku.
 * Tyto testy ověřují celý stack (controller → service → repository → databáze)
 * bez mockování závislostí.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback změn v databázi po každém testu
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Vyčištění databáze před každým testem
        productRepository.deleteAll();
    }

    // --- 1. TESTY PRO ZÍSKÁNÍ PRODUKTU PODLE ID ---

    @Test
    @DisplayName("GET /api/products/{id} - Úspěšné získání produktu")
    void getProductById_Success() throws Exception {
        // Arrange: Vytvoření produktu v databázi
        Product product = createTestProduct("Test Product", "Test Description", "19.99", "Electronics");
        Product savedProduct = productRepository.save(product);

        // Act & Assert: Volání API a ověření odpovědi
        mockMvc.perform(get("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.category").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Produkt neexistuje (404)")
    void getProductById_NotFound() throws Exception {
        // Act & Assert: Požadavek na neexistující produkt
        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // --- 2. TESTY PRO ZÍSKÁNÍ VŠECH PRODUKTŮ ---

    @Test
    @DisplayName("GET /api/products - Získání seznamu všech produktů s paginací")
    void getAllProducts_Success() throws Exception {
        // Arrange: Vytvoření více produktů v databázi
        Product product1 = createTestProduct("Product 1", "Description 1", "10.00", "Category A");
        Product product2 = createTestProduct("Product 2", "Description 2", "20.00", "Category B");
        Product product3 = createTestProduct("Product 3", "Description 3", "30.00", "Category C");

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        // Act & Assert: Volání API a ověření odpovědi
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name").value("Product 1"))
                .andExpect(jsonPath("$.content[1].name").value("Product 2"))
                .andExpect(jsonPath("$.content[2].name").value("Product 3"))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("GET /api/products - Prázdný seznam produktů")
    void getAllProducts_EmptyList() throws Exception {
        // Act & Assert: Žádné produkty v databázi
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // --- 3. TESTY PRO VYTVOŘENÍ PRODUKTU ---

    @Test
    @WithMockUser(roles = { "ADMIN" }) // Pouze admin může vytvářet produkty
    @DisplayName("POST /api/products - Úspěšné vytvoření produktu")
    void createProduct_Success() throws Exception {
        // Arrange: Příprava dat pro nový produkt
        String productJson = """
                {
                    "name": "New Product",
                    "description": "New Description",
                    "price": 99.99,
                    "category": "New Category"
                }
                """;

        // Act: Vytvoření produktu přes API
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "New Product")
                .param("description", "New Description")
                .param("price", "99.99")
                .param("category", "New Category"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.category").value("New Category"));

        // Assert: Ověření, že produkt byl uložen v databázi
        Optional<Product> savedProduct = productRepository.findByName("New Product");
        assertThat(savedProduct).isPresent();
        assertThat(savedProduct.get().getName()).isEqualTo("New Product");
        assertThat(savedProduct.get().getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("POST /api/products - Nepřihlášený uživatel nemůže vytvořit produkt (403)")
    void createProduct_Unauthorized() throws Exception {
        // Act & Assert: Pokus o vytvoření produktu bez přihlášení
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "Unauthorized Product")
                .param("description", "Description")
                .param("price", "50.00")
                .param("category", "Category"))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockUser(roles = { "USER" }) // Běžný uživatel není admin
    @DisplayName("POST /api/products - Uživatel bez role ADMIN nemůže vytvořit produkt (403)")
    void createProduct_Forbidden() throws Exception {
        // Act & Assert: Pokus o vytvoření produktu s rolí USER
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "Forbidden Product")
                .param("description", "Description")
                .param("price", "50.00")
                .param("category", "Category"))
                .andExpect(status().isForbidden());
    }

    // --- 4. TESTY PRO AKTUALIZACI PRODUKTU ---

    @Test
    @WithMockUser(roles = { "ADMIN" }) // Pouze admin může aktualizovat produkty
    @DisplayName("PUT /api/products/{id} - Úspěšná aktualizace produktu")
    void updateProduct_Success() throws Exception {
        // Arrange: Vytvoření existujícího produktu
        Product product = createTestProduct("Old Product", "Old Description", "50.00", "Old Category");
        Product savedProduct = productRepository.save(product);

        // Act: Aktualizace produktu přes API
        mockMvc.perform(put("/api/products/{id}", savedProduct.getId())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "Updated Product")
                .param("description", "Updated Description")
                .param("price", "75.00")
                .param("category", "Updated Category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.price").value(75.00))
                .andExpect(jsonPath("$.category").value("Updated Category"));

        // Assert: Ověření, že změny byly persistovány v databázi
        Optional<Product> updatedProduct = productRepository.findById(savedProduct.getId());
        assertThat(updatedProduct).isPresent();
        assertThat(updatedProduct.get().getName()).isEqualTo("Updated Product");
        assertThat(updatedProduct.get().getDescription()).isEqualTo("Updated Description");
        assertThat(updatedProduct.get().getPrice()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(updatedProduct.get().getCategory()).isEqualTo("Updated Category");
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    @DisplayName("PUT /api/products/{id} - Aktualizace neexistujícího produktu (404)")
    void updateProduct_NotFound() throws Exception {
        // Act & Assert: Pokus o aktualizaci neexistujícího produktu
        mockMvc.perform(put("/api/products/{id}", 999L)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "Updated Product")
                .param("description", "Updated Description")
                .param("price", "75.00")
                .param("category", "Updated Category"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Nepřihlášený uživatel nemůže aktualizovat produkt (403)")
    void updateProduct_Unauthorized() throws Exception {
        // Arrange: Vytvoření produktu
        Product product = createTestProduct("Product", "Description", "50.00", "Category");
        Product savedProduct = productRepository.save(product);

        // Act & Assert: Pokus o aktualizaci bez přihlášení
        mockMvc.perform(put("/api/products/{id}", savedProduct.getId())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", "Updated Product")
                .param("description", "Updated Description")
                .param("price", "75.00")
                .param("category", "Updated Category"))
                .andExpect(status().is(403));
    }

    // --- 5. TESTY PRO SMAZÁNÍ PRODUKTU ---

    @Test
    @WithMockUser(roles = { "ADMIN" }) // Pouze admin může mazat produkty
    @DisplayName("DELETE /api/products/{id} - Úspěšné smazání produktu")
    void deleteProduct_Success() throws Exception {
        // Arrange: Vytvoření produktu v databázi
        Product product = createTestProduct("Product to Delete", "Description", "25.00", "Category");
        Product savedProduct = productRepository.save(product);

        // Act: Smazání produktu přes API
        mockMvc.perform(delete("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isNoContent());

        // Assert: Ověření, že produkt byl odstraněn z databáze
        Optional<Product> deletedProduct = productRepository.findById(savedProduct.getId());
        assertThat(deletedProduct).isEmpty();
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    @DisplayName("DELETE /api/products/{id} - Smazání neexistujícího produktu (404)")
    void deleteProduct_NotFound() throws Exception {
        // Act & Assert: Pokus o smazání neexistujícího produktu
        mockMvc.perform(delete("/api/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Nepřihlášený uživatel nemůže smazat produkt (403)")
    void deleteProduct_Unauthorized() throws Exception {
        // Arrange: Vytvoření produktu
        Product product = createTestProduct("Product", "Description", "25.00", "Category");
        Product savedProduct = productRepository.save(product);

        // Act & Assert: Pokus o smazání bez přihlášení
        mockMvc.perform(delete("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockUser(roles = { "USER" }) // Běžný uživatel není admin
    @DisplayName("DELETE /api/products/{id} - Uživatel bez role ADMIN nemůže smazat produkt (403)")
    void deleteProduct_Forbidden() throws Exception {
        // Arrange: Vytvoření produktu
        Product product = createTestProduct("Product", "Description", "25.00", "Category");
        Product savedProduct = productRepository.save(product);

        // Act & Assert: Pokus o smazání s rolí USER
        mockMvc.perform(delete("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isForbidden());
    }

    // --- POMOCNÉ METODY ---

    /**
     * Vytvoří testovací produkt s danými hodnotami.
     */
    private Product createTestProduct(String name, String description, String price, String category) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .category(category)
                .active(true)
                .build();
    }
}
