package krematos.service;

import krematos.dto.product.ProductResponse;
import krematos.exception.product.InvalidFileException;
import krematos.exception.product.ProductImageFileIsTooBig;
import krematos.mapper.ProductMapper;
import krematos.model.Product;
import krematos.repository.ProductRepository;
import krematos.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.io.File;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Comparator;

/**
 * Unit testy pro ProductService s použitím best practices.
 * Testy jsou organizovány do vnořených tříd pro lepší přehlednost.
 * Všechny výjimky odpovídají GlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private static final Long PRODUCT_ID = 1L;
    private static final String PRODUCT_NAME = "Test Product";
    private static final String PRODUCT_DESCRIPTION = "Test Description";
    private static final BigDecimal PRODUCT_PRICE = new BigDecimal("99.99");
    private static final String PRODUCT_CATEGORY = "Electronics";

    private final String TEST_UPLOAD_DIR = "test-uploads";

    // Nastavení upload directory pro testy
    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(productService, "uploadDir", "test-uploads");
        Files.createDirectories(Paths.get(TEST_UPLOAD_DIR));
    }
    // Smaže testovací složku a její obsah po každém testu
    @AfterEach
    void tearDown() throws IOException {
        Path path = Paths.get(TEST_UPLOAD_DIR);
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder()) // Nejdřív soubory, pak složky
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    // --- Helper Methods ---

    /**
     * Vytvoří testovací Product entitu s výchozími hodnotami.
     */
    private Product createTestProduct() {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setName(PRODUCT_NAME);
        product.setDescription(PRODUCT_DESCRIPTION);
        product.setPrice(PRODUCT_PRICE);
        product.setCategory(PRODUCT_CATEGORY);
        product.setImages(new ArrayList<>());
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        return product;
    }

    /**
     * Vytvoří testovací ProductResponse DTO s výchozími hodnotami.
     */
    private ProductResponse createTestProductResponse() {
        return new ProductResponse(
                PRODUCT_ID,
                PRODUCT_NAME,
                PRODUCT_DESCRIPTION,
                PRODUCT_PRICE,
                PRODUCT_CATEGORY,
                null,
                new ArrayList<>(),
                Instant.now(),
                Instant.now());
    }

    /**
     * Vytvoří mock MultipartFile pro testování uploadů.
     */
    private MockMultipartFile createMockImageFile(String filename, String contentType, long size) {
        byte[] content = new byte[(int) size];
        return new MockMultipartFile("file", filename, contentType, content);
    }

    // --- Nested Test Classes ---

    @Nested
    @DisplayName("findProductById Tests")
    class FindProductByIdTests {

        @Test
        @DisplayName("Měl by vrátit produkt, když existuje")
        void shouldReturnProduct_WhenProductExists() {
            // Given
            Product product = createTestProduct();
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            // When
            Optional<Product> result = productService.findProductById(PRODUCT_ID);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get())
                    .extracting(Product::getId, Product::getName, Product::getDescription)
                    .containsExactly(PRODUCT_ID, PRODUCT_NAME, PRODUCT_DESCRIPTION);
            verify(productRepository, times(1)).findById(PRODUCT_ID);
        }

        @Test
        @DisplayName("Měl by vrátit prázdný Optional, když produkt neexistuje")
        void shouldReturnEmpty_WhenProductDoesNotExist() {
            // Given
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            // When
            Optional<Product> result = productService.findProductById(PRODUCT_ID);

            // Then
            assertThat(result).isEmpty();
            verify(productRepository, times(1)).findById(PRODUCT_ID);
        }

        @Test
        @DisplayName("Měl by vrátit prázdný Optional pro null ID")
        void shouldReturnEmpty_WhenIdIsNull() {
            // Given
            when(productRepository.findById(null)).thenReturn(Optional.empty());

            // When
            Optional<Product> result = productService.findProductById(null);

            // Then
            assertThat(result).isEmpty();
            verify(productRepository, times(1)).findById(null);
        }
    }

    @Nested
    @DisplayName("findAllProducts Tests")
    class FindAllProductsTests {

        @Test
        @DisplayName("Měl by vrátit stránku produktů")
        void shouldReturnPageOfProducts() {
            // Given
            Product product = createTestProduct();
            Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
            Pageable pageable = PageRequest.of(0, 10);

            when(productRepository.findAll(pageable)).thenReturn(productPage);

            // When
            Page<Product> result = productService.findAllProducts(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(PRODUCT_ID);
            verify(productRepository, times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("Měl by vrátit prázdnou stránku, když neexistují žádné produkty")
        void shouldReturnEmptyPage_WhenNoProductsExist() {
            // Given
            Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
            Pageable pageable = PageRequest.of(0, 10);

            when(productRepository.findAll(pageable)).thenReturn(emptyPage);

            // When
            Page<Product> result = productService.findAllProducts(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
            verify(productRepository, times(1)).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("saveProduct Tests")
    class SaveProductTests {

        @Test
        @DisplayName("Měl by uložit produkt úspěšně")
        void shouldSaveProduct_Successfully() {
            // Given
            Product product = createTestProduct();
            when(productRepository.save(product)).thenReturn(product);

            // When
            Product result = productService.saveProduct(product);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_ID);
            assertThat(result.getName()).isEqualTo(PRODUCT_NAME);
            verify(productRepository, times(1)).save(product);
        }

        @Test
        @DisplayName("Měl by vyhodit InvalidFileException, když je produkt null")
        void shouldThrowInvalidFileException_WhenProductIsNull() {
            // When & Then
            assertThatThrownBy(() -> productService.saveProduct(null))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessage("Produkt nesmí být null");
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteProductById Tests")
    class DeleteProductByIdTests {

        @BeforeEach
        void setUpTransaction() {
            TransactionSynchronizationManager.initSynchronization();
        }

        @AfterEach
        void tearDownTransaction() {
            TransactionSynchronizationManager.clearSynchronization();
        }

        @Test
        @DisplayName("Měl by smazat produkt úspěšně, když existuje")
        void shouldDeleteProduct_WhenProductExists() {
            // Given
            Product product = createTestProduct();
            product.setImages(new ArrayList<>(List.of("image1.jpg", "image2.jpg")));

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
            doNothing().when(productRepository).delete(product);

            // When
            boolean result = productService.deleteProductById(PRODUCT_ID);

            // Then
            assertThat(result).isTrue();
            verify(productRepository, times(1)).findById(PRODUCT_ID);
            verify(productRepository, times(1)).delete(product);
        }

        @Test
        @DisplayName("Měl by vrátit false, když produkt neexistuje")
        void shouldReturnFalse_WhenProductDoesNotExist() {
            // Given
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            // When
            boolean result = productService.deleteProductById(PRODUCT_ID);

            // Then
            assertThat(result).isFalse();
            verify(productRepository, times(1)).findById(PRODUCT_ID);
            verify(productRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Měl by smazat produkt i s prázdným seznamem obrázků")
        void shouldDeleteProduct_WithEmptyImagesList() {
            // Given
            Product product = createTestProduct();
            product.setImages(new ArrayList<>());

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
            doNothing().when(productRepository).delete(product);

            // When
            boolean result = productService.deleteProductById(PRODUCT_ID);

            // Then
            assertThat(result).isTrue();
            verify(productRepository, times(1)).delete(product);
        }
    }

    @Nested
    @DisplayName("createProductWithImages Tests")
    class CreateProductWithImagesTests {

        @Test
        @DisplayName("Měl by vytvořit produkt bez obrázků")
        void shouldCreateProduct_WithoutImages() {
            // Given
            ProductResponse productDto = createTestProductResponse();
            Product product = createTestProduct();

            when(productMapper.toEntity(productDto)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);

            // When
            Product result = productService.createProductWithImages(productDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_ID);
            verify(productMapper, times(1)).toEntity(productDto);
            verify(productRepository, times(1)).save(product);
        }

        @Test
        @DisplayName("Měl by vyhodit InvalidFileException pro nepovolený typ souboru")
        void shouldThrowInvalidFileException_ForInvalidFileType() {
            // Given
            MockMultipartFile invalidFile = createMockImageFile("test.txt", "text/plain", 1024);
            List<MultipartFile> files = List.of(invalidFile);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);

            // When & Then
            assertThatThrownBy(() -> productService.createProductWithImages(productDto))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("Nepovolený typ souboru");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Měl by vyhodit ProductImageFileIsTooBig pro příliš velký soubor")
        void shouldThrowProductImageFileIsTooBig_ForLargeFile() {
            // Given
            long fileSize = 6 * 1024 * 1024; // 6MB (větší než limit 5MB)
            MockMultipartFile largeFile = createMockImageFile("large.jpg", "image/jpeg", fileSize);
            List<MultipartFile> files = List.of(largeFile);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);

            // When & Then
            assertThatThrownBy(() -> productService.createProductWithImages(productDto))
                    .isInstanceOf(ProductImageFileIsTooBig.class)
                    .hasMessage("Soubor je příliš velký. Max 5MB.");

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateProduct Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Měl by aktualizovat produkt úspěšně")
        void shouldUpdateProduct_Successfully() {
            // Given
            Product existingProduct = createTestProduct();
            ProductResponse productDto = createTestProductResponse();

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            doNothing().when(productMapper).updateProductFromDto(productDto, existingProduct);
            when(productRepository.save(existingProduct)).thenReturn(existingProduct);

            // When
            Optional<Product> result = productService.updateProduct(PRODUCT_ID, productDto);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(PRODUCT_ID);
            verify(productRepository, times(1)).findById(PRODUCT_ID);
            verify(productMapper, times(1)).updateProductFromDto(productDto, existingProduct);
            verify(productRepository, times(1)).save(existingProduct);
        }

        @Test
        @DisplayName("Měl by vrátit prázdný Optional, když produkt neexistuje")
        void shouldReturnEmpty_WhenProductDoesNotExist() {
            // Given
            ProductResponse productDto = createTestProductResponse();
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            // When
            Optional<Product> result = productService.updateProduct(PRODUCT_ID, productDto);

            // Then
            assertThat(result).isEmpty();
            verify(productRepository, times(1)).findById(PRODUCT_ID);
            verify(productMapper, never()).updateProductFromDto(any(), any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Měl by aktualizovat produkt bez nových obrázků")
        void shouldUpdateProduct_WithoutNewImages() {
            // Given
            Product existingProduct = createTestProduct();
            ProductResponse productDto = new ProductResponse(
                    PRODUCT_ID, "Updated Name", PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, null, new ArrayList<>(), null, null);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            doNothing().when(productMapper).updateProductFromDto(productDto, existingProduct);
            when(productRepository.save(existingProduct)).thenReturn(existingProduct);

            // When
            Optional<Product> result = productService.updateProduct(PRODUCT_ID, productDto);

            // Then
            assertThat(result).isPresent();
            verify(productRepository, times(1)).save(existingProduct);
        }

        @Test
        @DisplayName("Měl by vyhodit InvalidFileException při aktualizaci s neplatným souborem")
        void shouldThrowInvalidFileException_WhenUpdatingWithInvalidFile() {
            // Given
            Product existingProduct = createTestProduct();
            MockMultipartFile invalidFile = createMockImageFile("test.pdf", "application/pdf", 1024);

            ProductResponse productDto = new ProductResponse(
                    PRODUCT_ID, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, List.of(invalidFile), new ArrayList<>(), null, null);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            doNothing().when(productMapper).updateProductFromDto(productDto, existingProduct);

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, productDto))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("Nepovolený typ souboru");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Měl by správně zpracovat produkt s maximální cenou")
        void shouldHandleProduct_WithMaxPrice() {
            // Given
            Product product = createTestProduct();
            product.setPrice(new BigDecimal("9999999999.99"));

            when(productRepository.save(product)).thenReturn(product);

            // When
            Product result = productService.saveProduct(product);

            // Then
            assertThat(result.getPrice()).isEqualByComparingTo("9999999999.99");
        }

        @Test
        @DisplayName("Měl by správně zpracovat produkt s minimální cenou")
        void shouldHandleProduct_WithMinPrice() {
            // Given
            Product product = createTestProduct();
            product.setPrice(new BigDecimal("0.01"));

            when(productRepository.save(product)).thenReturn(product);

            // When
            Product result = productService.saveProduct(product);

            // Then
            assertThat(result.getPrice()).isEqualByComparingTo("0.01");
        }

        @Test
        @DisplayName("Měl by správně zpracovat prázdný seznam obrázků")
        void shouldHandleEmptyImagesList() {
            // Given
            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, Collections.emptyList(), new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);

            // When
            Product result = productService.createProductWithImages(productDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getImages()).isEmpty();
        }

        @Test
        @DisplayName("Měl by správně zpracovat null seznam obrázků")
        void shouldHandleNullImagesList() {
            // Given
            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, null, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);

            // When
            Product result = productService.createProductWithImages(productDto);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository, times(1)).save(product);
        }

        @Test
        @DisplayName("Měl by ignorovat prázdné soubory v seznamu")
        void shouldIgnoreEmptyFilesInList() {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);
            List<MultipartFile> files = List.of(emptyFile);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);

            // When
            Product result = productService.createProductWithImages(productDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getImages()).isEmpty();
        }

        @Test
        @DisplayName("Měl by ignorovat null soubory v seznamu")
        void shouldIgnoreNullFilesInList() {
            // Given
            List<MultipartFile> files = new ArrayList<>();
            files.add(null);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);

            // When
            Product result = productService.createProductWithImages(productDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getImages()).isEmpty();
        }
    }

    @Nested
    @DisplayName("File Validation Tests")
    class FileValidationTests {

        @Test
        @DisplayName("Měl by přijmout JPEG obrázek")
        void shouldAcceptJpegImage() {
            // Given
            MockMultipartFile jpegFile = createMockImageFile("test.jpg", "image/jpeg", 1024);
            List<MultipartFile> files = List.of(jpegFile);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);

            // When & Then - mělo by projít bez výjimky
            assertThatCode(() -> productService.createProductWithImages(productDto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Měl by přijmout PNG obrázek")
        void shouldAcceptPngImage() {
            // Given
            MockMultipartFile pngFile = createMockImageFile("test.png", "image/png", 2048);
            List<MultipartFile> files = List.of(pngFile);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);

            // When & Then
            assertThatCode(() -> productService.createProductWithImages(productDto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Měl by přijmout GIF obrázek")
        void shouldAcceptGifImage() {
            // Given
            MockMultipartFile gifFile = createMockImageFile("test.gif", "image/gif", 3072);
            List<MultipartFile> files = List.of(gifFile);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);

            // When & Then
            assertThatCode(() -> productService.createProductWithImages(productDto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Měl by odmítnout BMP obrázek")
        void shouldRejectBmpImage() {
            // Given
            MockMultipartFile bmpFile = createMockImageFile("test.bmp", "image/bmp", 1024);
            List<MultipartFile> files = List.of(bmpFile);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);

            // When & Then
            assertThatThrownBy(() -> productService.createProductWithImages(productDto))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("Nepovolený typ souboru: image/bmp");
        }

        @Test
        @DisplayName("Měl by odmítnout SVG obrázek")
        void shouldRejectSvgImage() {
            // Given
            MockMultipartFile svgFile = createMockImageFile("test.svg", "image/svg+xml", 512);
            List<MultipartFile> files = List.of(svgFile);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);

            // When & Then
            assertThatThrownBy(() -> productService.createProductWithImages(productDto))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("Nepovolený typ souboru");
        }

        @Test
        @DisplayName("Měl by přijmout soubor přesně na limitu velikosti (5MB)")
        void shouldAcceptFileAtSizeLimit() {
            // Given
            long exactLimit = 5 * 1024 * 1024; // Přesně 5MB
            MockMultipartFile limitFile = createMockImageFile("limit.jpg", "image/jpeg", exactLimit);
            List<MultipartFile> files = List.of(limitFile);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);

            // When & Then
            assertThatCode(() -> productService.createProductWithImages(productDto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Měl by odmítnout soubor o 1 byte větší než limit")
        void shouldRejectFileOneByteOverLimit() {
            // Given
            long overLimit = (5 * 1024 * 1024) + 1; // 5MB + 1 byte
            MockMultipartFile overLimitFile = createMockImageFile("overlimit.jpg", "image/jpeg", overLimit);
            List<MultipartFile> files = List.of(overLimitFile);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);

            // When & Then
            assertThatThrownBy(() -> productService.createProductWithImages(productDto))
                    .isInstanceOf(ProductImageFileIsTooBig.class)
                    .hasMessage("Soubor je příliš velký. Max 5MB.");
        }
    }

    @Nested
    @DisplayName("Update Product with Images Tests")
    class UpdateProductWithImagesTests {

        @Test
        @DisplayName("Měl by aktualizovat produkt a přidat nové validní obrázky")
        void shouldUpdateProductAndAddValidImages() {
            // Given
            Product existingProduct = createTestProduct();
            MockMultipartFile validFile = createMockImageFile("new.jpg", "image/jpeg", 2048);

            ProductResponse productDto = new ProductResponse(
                    PRODUCT_ID, "Updated Name", PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, List.of(validFile), new ArrayList<>(), null, null);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            doNothing().when(productMapper).updateProductFromDto(productDto, existingProduct);
            when(productRepository.save(existingProduct)).thenReturn(existingProduct);

            // When & Then
            assertThatCode(() -> productService.updateProduct(PRODUCT_ID, productDto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Měl by aktualizovat produkt s více validními obrázky")
        void shouldUpdateProductWithMultipleValidImages() {
            // Given
            Product existingProduct = createTestProduct();
            MockMultipartFile file1 = createMockImageFile("image1.jpg", "image/jpeg", 1024);
            MockMultipartFile file2 = createMockImageFile("image2.png", "image/png", 2048);
            MockMultipartFile file3 = createMockImageFile("image3.gif", "image/gif", 3072);

            ProductResponse productDto = new ProductResponse(
                    PRODUCT_ID, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, List.of(file1, file2, file3), new ArrayList<>(), null, null);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            doNothing().when(productMapper).updateProductFromDto(productDto, existingProduct);
            when(productRepository.save(existingProduct)).thenReturn(existingProduct);

            // When & Then
            assertThatCode(() -> productService.updateProduct(PRODUCT_ID, productDto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Měl by aktualizovat produkt s prázdným seznamem obrázků")
        void shouldUpdateProductWithEmptyImagesList() {
            // Given
            Product existingProduct = createTestProduct();
            ProductResponse productDto = new ProductResponse(
                    PRODUCT_ID, "Updated Name", PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, Collections.emptyList(), new ArrayList<>(), null, null);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            doNothing().when(productMapper).updateProductFromDto(productDto, existingProduct);
            when(productRepository.save(existingProduct)).thenReturn(existingProduct);

            // When
            Optional<Product> result = productService.updateProduct(PRODUCT_ID, productDto);

            // Then
            assertThat(result).isPresent();
            verify(productRepository, times(1)).save(existingProduct);
        }
    }

    @Nested
    @DisplayName("Delete Image File Tests")
    class DeleteImageFileTests {

        @Test
        @DisplayName("Měl by správně zpracovat null název souboru")
        void shouldHandleNullFileName() {
            // When & Then - nemělo by vyhodit výjimku
            assertThatCode(() -> productService.deleteImageFile(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Měl by správně zpracovat prázdný název souboru")
        void shouldHandleEmptyFileName() {
            // When & Then
            assertThatCode(() -> productService.deleteImageFile(""))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Měl by správně zpracovat whitespace název souboru")
        void shouldHandleWhitespaceFileName() {
            // When & Then
            assertThatCode(() -> productService.deleteImageFile("   "))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Měl by správně zpracovat validní název souboru")
        void shouldHandleValidFileName() {
            // When & Then - nemělo by vyhodit výjimku i když soubor neexistuje
            assertThatCode(() -> productService.deleteImageFile("test-image.jpg"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Create Product with Multiple Images Tests")
    class CreateProductWithMultipleImagesTests {

        @Test
        @DisplayName("Měl by vytvořit produkt s více validními obrázky")
        void shouldCreateProductWithMultipleValidImages() {
            // Given
            MockMultipartFile file1 = createMockImageFile("image1.jpg", "image/jpeg", 1024);
            MockMultipartFile file2 = createMockImageFile("image2.png", "image/png", 2048);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, List.of(file1, file2), new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);

            // When & Then
            assertThatCode(() -> productService.createProductWithImages(productDto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Měl by vyhodit výjimku, když jeden z více souborů je nevalidní")
        void shouldThrowException_WhenOneOfMultipleFilesIsInvalid() {
            // Given
            MockMultipartFile validFile = createMockImageFile("valid.jpg", "image/jpeg", 1024);
            MockMultipartFile invalidFile = createMockImageFile("invalid.exe", "application/exe", 512);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, List.of(validFile, invalidFile), new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);

            // When & Then
            assertThatThrownBy(() -> productService.createProductWithImages(productDto))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("Nepovolený typ souboru");
        }

        @Test
        @DisplayName("Měl by vytvořit produkt se smíšeným seznamem (validní, null, prázdné)")
        void shouldCreateProductWithMixedFileList() {
            // Given
            MockMultipartFile validFile = createMockImageFile("valid.jpg", "image/jpeg", 1024);
            MockMultipartFile emptyFile = new MockMultipartFile("empty", "", "image/jpeg", new byte[0]);

            List<MultipartFile> files = new ArrayList<>();
            files.add(validFile);
            files.add(null);
            files.add(emptyFile);

            ProductResponse productDto = new ProductResponse(
                    null, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE,
                    PRODUCT_CATEGORY, files, new ArrayList<>(), null, null);

            Product product = createTestProduct();
            when(productMapper.toEntity(productDto)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);

            // When & Then - mělo by zpracovat pouze validní soubor
            assertThatCode(() -> productService.createProductWithImages(productDto))
                    .doesNotThrowAnyException();
        }
    }
}
