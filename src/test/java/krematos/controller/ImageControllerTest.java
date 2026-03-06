package krematos.controller;

import krematos.security.JwtAuthenticationFilter;
import krematos.service.JwtService;
import krematos.service.user.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ImageController.class)
@AutoConfigureMockMvc(addFilters = false) // Vypne bezpečnostní filtry, aby testy nebyly závislé na autentizaci
@TestPropertySource(properties = "app.upload.dir=${java.io.tmpdir}/image-controller-test")
@DisplayName("ImageController – testy GET /api/images/{filename}")
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * AuthController je označen @RestControllerAdvice, takže ho @WebMvcTest načte
     * i přes filtr controllers = ImageController.class.
     * Mockujeme jeho závislosti, aby kontext šel nastartovat.
     */
    @MockBean
    AuthenticationManager authenticationManager;
    @MockBean
    UserService userService;
    @MockBean
    JwtService jwtService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    /** Sdílený dočasný adresář – shoduje se s app.upload.dir nastaveným výše. */
    @TempDir
    static Path uploadDir;

    // -----------------------------------------------------------------------
    // Helper metody
    // -----------------------------------------------------------------------

    /**
     * Vytvoří skutečný soubor v dočasném adresáři se zadaným názvem a obsahem.
     */
    private void createTestFile(String filename, byte[] content) throws Exception {
        Files.write(uploadDir.resolve(filename), content);
    }

    /** Minimální validní JPEG magic bytes (SOI marker). */
    private static byte[] jpegMagicBytes() {
        return new byte[] {
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01
        };
    }

    /** Minimální validní PNG magic bytes. */
    private static byte[] pngMagicBytes() {
        return new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };
    }

    /** Minimální validní WebP magic bytes (RIFF….WEBP). */
    private static byte[] webpMagicBytes() {
        return new byte[] {
                0x52, 0x49, 0x46, 0x46, // "RIFF"
                0x24, 0x00, 0x00, 0x00, // file size (placeholder)
                0x57, 0x45, 0x42, 0x50 // "WEBP"
        };
    }

    // =======================================================================
    // Skupina: 200 OK – úspěšné načtení obrázku
    // =======================================================================

    @Nested
    @DisplayName("200 OK – soubor existuje")
    class SuccessTests {

        @Test
        @DisplayName("Měl by vrátit JPEG soubor s Content-Type image/jpeg")
        void shouldReturnJpegImage() throws Exception {
            String filename = "product-photo.jpg";
            createTestFile(filename, jpegMagicBytes());

            mockMvc.perform(get("/api/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG));
        }

        @Test
        @DisplayName("Měl by vrátit PNG soubor s Content-Type image/png")
        void shouldReturnPngImage() throws Exception {
            String filename = "product-photo.png";
            createTestFile(filename, pngMagicBytes());

            mockMvc.perform(get("/api/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG));
        }

        @Test
        @DisplayName("Měl by vrátit WebP soubor s Content-Type image/webp")
        void shouldReturnWebpImage() throws Exception {
            String filename = "product-photo.webp";
            createTestFile(filename, webpMagicBytes());

            mockMvc.perform(get("/api/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("image/webp")));
        }

        @Test
        @DisplayName("Měl by vrátit soubor s UUID prefixem (formát generovaný ProductServiceImpl)")
        void shouldReturnFileWithUuidPrefix() throws Exception {
            // ProductServiceImpl ukládá soubory jako UUID_originalFilename
            String filename = "550e8400-e29b-41d4-a716-446655440000_produkt.jpg";
            createTestFile(filename, jpegMagicBytes());

            mockMvc.perform(get("/api/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG));
        }

        @Test
        @DisplayName("Měl by vrátit správné bajty obsahu souboru")
        void shouldReturnCorrectFileContent() throws Exception {
            String filename = "exact-content.jpg";
            byte[] expectedContent = jpegMagicBytes();
            createTestFile(filename, expectedContent);

            mockMvc.perform(get("/api/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(content().bytes(expectedContent));
        }

        @Test
        @DisplayName("Měl by vrátit soubor neznámého typu jako application/octet-stream")
        void shouldFallbackToOctetStream_WhenContentTypeUnknown() throws Exception {
            // Soubor bez standardní přípony → Files.probeContentType() vrátí null
            // → controller použije "application/octet-stream"
            String filename = "binary-data";
            createTestFile(filename, new byte[] { 0x00, 0x01, 0x02, 0x03 });

            mockMvc.perform(get("/api/images/{filename}", filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM));
        }
    }

    // =======================================================================
    // Skupina: 404 Not Found – soubor neexistuje
    // =======================================================================

    @Nested
    @DisplayName("404 Not Found – soubor neexistuje")
    class NotFoundTests {

        @Test
        @DisplayName("Měl by vrátit 404 pro neexistující soubor")
        void shouldReturn404_WhenFileDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/images/{filename}", "neexistujici-obrazek.jpg"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Měl by vrátit 404 pro soubor s UUID prefixem, který neexistuje")
        void shouldReturn404_ForNonexistentUuidPrefixedFile() throws Exception {
            mockMvc.perform(get("/api/images/{filename}", "00000000-0000-0000-0000-000000000000_phantom.jpg"))
                    .andExpect(status().isNotFound());
        }
    }

    // =======================================================================
    // Skupina: 403 Forbidden – Directory Traversal
    // =======================================================================

    @Nested
    @DisplayName("403 Forbidden – Directory Traversal ochrana")
    class DirectoryTraversalTests {

        @Test
        @DisplayName("Měl by vrátit 403 při URL-enkódovaném pokusu o Directory Traversal (../)")
        void shouldReturn403_ForUrlEncodedDirectoryTraversal() throws Exception {
            // %2E%2E%2F => ../ – cesta mimo uploads/ → controller vrátí 403
            mockMvc.perform(get("/api/images/%2E%2E%2Fetc%2Fpasswd"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Měl by vrátit 404 při double-encoded traversal sekvenci (normalize() cestu srovná)")
        void shouldReturn404_ForDoubleEncodedTraversal() throws Exception {
            // %252E%252E%252F = double-encoded "../"
            // Spring dekóduje pouze jednou → literální řetězec "%2E%2E%2Fsecret.txt"
            // normalize() ho srovná jako platné jméno souboru → soubor nenalezen → 404
            mockMvc.perform(get("/api/images/%252E%252E%252Fsecret.txt"))
                    .andExpect(status().isNotFound());
        }
    }
}
