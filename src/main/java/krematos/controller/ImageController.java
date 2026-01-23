package krematos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller pro správu obrázků produktů.
 * Poskytuje endpoint pro získání a zobrazení nahraných obrázků.
 */
@RestController
@RequestMapping("/api/images")
@Tag(name = "Obrázky", description = "API pro správu a získávání obrázků produktů")
public class ImageController {

    /**
     * 🖼️ Získání obrázku podle názvu souboru.
     *
     * @param filename Název souboru obrázku
     * @return Resource s obrázkem nebo 404 pokud obrázek neexistuje
     */
    @Operation(summary = "Získání obrázku", description = "Vrátí obrázek produktu na základě názvu souboru. " +
            "Podporované formáty: JPEG, PNG, GIF. " +
            "Obrázky jsou uloženy v adresáři 'uploads/'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obrázek byl úspěšně nalezen a vrácen", content = @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Obrázek nebyl nalezen", content = @Content),
            @ApiResponse(responseCode = "500", description = "Interní chyba serveru při načítání obrázku", content = @Content)
    })
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(
            @Parameter(description = "Název souboru obrázku (např. 'produkt-123.jpg')", required = true, example = "uuid_product-image.jpg") @PathVariable String filename) {
        try {
            String uploadDir = "uploads/";
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                // Zjistí typ souboru (image/jpeg, image/png...)
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
