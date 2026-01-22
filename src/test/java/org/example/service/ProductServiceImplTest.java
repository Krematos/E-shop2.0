package org.example.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {
    /*
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productService, "uploadDir", tempDir.toString());
    }

    @Test
    void createProductWithImages_ShouldSaveFileAndEntity() throws IOException {
        // 1. PŘÍPRAVA DAT
        MockMultipartFile file = new MockMultipartFile(
                "imageFiles",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test data".getBytes());

        ProductResponse dto = ProductResponse.builder()
                .name("Test Product")
                .imagesFilenames(List.of(file))
                .build();

        Product productEntity = Product.builder()
                .name("Test Product")
                .images(new ArrayList<>()) // Důležité: inicializovat list
                .build();

        // Mockování mapperu a repozitáře
        when(productMapper.toEntity(dto)).thenReturn(productEntity);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. AKCE
        Product savedProduct = productService.createProductWithImages(dto);

        // 3. OVĚŘENÍ (Assertions)
        // a) Ověří, že se volal save
        verify(productRepository).save(any(Product.class));

        // b) Ověří, že produkt má v seznamu 1 obrázek
        assertEquals(1, savedProduct.getImages().size());
        String savedFileName = savedProduct.getImages().get(0);

        // c) Ověří, že soubor fyzicky existuje v dočasné složce
        Path savedFilePath = tempDir.resolve(savedFileName);
        assertTrue(Files.exists(savedFilePath), "Soubor by měl existovat na disku");
        assertTrue(savedFileName.contains("test-image.jpg"), "Název by měl obsahovat původní jméno");
    }

    @Test
    void createProduct_WithInvalidFileType_ShouldThrowException() {
        // 1. PŘÍPRAVA - Textový soubor místo obrázku
        MockMultipartFile badFile = new MockMultipartFile(
                "imageFiles", "text.txt", "text/plain", "obsah".getBytes());
        ProductResponse dto = ProductResponse.builder().imagesFilenames(List.of(badFile)).build();

        // Mock mapperu (musí vrátit entitu, aby kód nespadl dříve)
        when(productMapper.toEntity(dto)).thenReturn(new Product());

        // 2. & 3. AKCE a OVĚŘENÍ
        assertThrows(IllegalArgumentException.class, () -> {
            productService.createProductWithImages(dto);
        }, "Měl by vyhodit výjimku pro nepovolený typ souboru");

        // Ujistíme se, že se nic neuložilo do DB
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProductById_ShouldDeleteFileAndEntity() throws IOException {
        // 1. PŘÍPRAVA
        String fileName = "uuid_delete-test.jpg";
        Path filePath = tempDir.resolve(fileName);
        Files.createFile(filePath); // Fyzicky vytvoříme soubor

        Product product = Product.builder()
                .id(1L)
                .images(List.of(fileName))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsById(1L)).thenReturn(true); // Pro kontrolu v metodě (pokud ji tam máš)

        // 2. AKCE
        boolean result = productService.deleteProductById(1L);

        // 3. OVĚŘENÍ
        assertTrue(result);
        verify(productRepository).delete(product); // Ověříme smazání z DB
        assertFalse(Files.exists(filePath), "Soubor by měl být smazán z disku");
    }

    @Test
    void updateProduct_ShouldReplaceImage() throws IOException {
        // 1. PŘÍPRAVA
        // Starý soubor (bude smazán)
        String oldFileName = "old-image.jpg";
        Path oldFilePath = tempDir.resolve(oldFileName);
        Files.createFile(oldFilePath);

        // Nový soubor (bude nahrán)
        MockMultipartFile newFile = new MockMultipartFile(
                "imageFiles", "new.jpg", MediaType.IMAGE_JPEG_VALUE, "new data".getBytes());

        Product existingProduct = Product.builder()
                .id(1L)
                .name("Old Name")
                .images(new ArrayList<>(List.of(oldFileName))) // Mutable list
                .build();

        ProductResponse updateDto = ProductResponse.builder()
                .name("New Name")
                .imagesFilenames(List.of(newFile))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        // Nastavení chování mapperu při update (void metoda)
        // doNothing().when(productMapper).updateProductFromDto(updateDto,
        // existingProduct);
        // ^ Toto není nutné, pokud mapper je mock a nic nedělá, ale v realitě by měnil
        // pole.

        // 2. AKCE
        productService.updateProduct(1L, updateDto);

        // 3. OVĚŘENÍ
        // Starý soubor zmizel?
        assertFalse(Files.exists(oldFilePath), "Starý soubor měl být smazán");

        // Nový soubor je v seznamu?
        assertEquals(1, existingProduct.getImages().size());
        String newStoredName = existingProduct.getImages().get(0);
        assertTrue(newStoredName.contains("new.jpg"));

        // Nový soubor existuje fyzicky?
        assertTrue(Files.exists(tempDir.resolve(newStoredName)));
    }

    @Test
    void testFindProductById_ProductExist() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.findProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());
    }

    @Test
    void testFindProductById_ProductNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.findProductById(1L);

        assertTrue(result.isEmpty());
    }*/
}
