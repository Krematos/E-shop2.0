package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.product.ProductResponse;
import org.example.mapper.ProductMapper;
import org.example.model.Product;
import org.example.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

/**
 * Controller pro správu produktů.
 * Poskytuje endpointy pro CRUD operace s produkty a nahrávání obrázků.
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produkty", description = "API pro správu produktů (vytváření, úpravy, mazání, zobrazení produktů)")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    /**
     * 🔍 Získání produktu podle ID
     *
     * @param id ID produktu
     * @return Detail produktu
     */
    @Operation(summary = "Získání produktu podle ID", description = "Vrátí detail konkrétního produktu na základě jeho ID. "
            +
            "Tento endpoint je veřejný a nevyužívá autentizace.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt byl úspěšně nalezen", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produkt nebyl nalezen", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID produktu", required = true, example = "1") @PathVariable Long id) {
        log.info("GET /api/products/{} - Získání produktu podle ID", id);
        return productService.findProductById(id)
                .map(productMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ➕ Vytvoření nového produktu (pouze ADMIN)
     *
     * @param productDto Data nového produktu (název, popis, cena, kategorie,
     *                   obrázky)
     * @return Vytvořený produkt
     * @throws IOException Pokud nastane chyba při nahrávání obrázků
     */
    @Operation(summary = "Vytvoření nového produktu", description = "Vytvoří nový produkt včetně nahrání obrázků. " +
            "Tento endpoint je dostupný pouze pro administrátory. " +
            "Přijímá multipart/form-data pro podporu nahrávání souborů.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produkt byl úspěšně vytvořen", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Neplatná data v požadavku (chybějící povinné pole, neplatná cena)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Uživatel není přihlášen", content = @Content),
            @ApiResponse(responseCode = "403", description = "Uživatel nemá oprávnění (pouze ADMIN)", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')") // Pouze admin může přidávat produkty
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "Data nového produktu (název, popis, cena, kategorie, obrázky)", required = true) @ModelAttribute ProductResponse productDto)
            throws IOException {
        log.info("POST /api/products - Vytvoření nového produktu: {}", productDto.name());
        if (productDto.imagesFilenames() == null || productDto.imagesFilenames().isEmpty()) {
            log.warn("Produkt nemá obrázky.");
        }

        Product savedProduct = productService.createProductWithImages(productDto);
        return new ResponseEntity<>(productMapper.toDto(savedProduct), HttpStatus.CREATED);
    }

    /**
     * ♻️ Aktualizace existujícího produktu (pouze ADMIN)
     *
     * @param id         ID produktu k aktualizaci
     * @param productDto Nová data produktu
     * @return Aktualizovaný produkt
     */
    @Operation(summary = "Aktualizace produktu", description = "Aktualizuje existující produkt podle ID. " +
            "Může aktualizovat všechny atributy včetně obrázků. " +
            "Tento endpoint je dostupný pouze pro administrátory.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt byl úspěšně aktualizován", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Neplatná data v požadavku", content = @Content),
            @ApiResponse(responseCode = "401", description = "Uživatel není přihlášen", content = @Content),
            @ApiResponse(responseCode = "403", description = "Uživatel nemá oprávnění (pouze ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produkt nebyl nalezen", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')") // Pouze admin může aktualizovat produkty
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID produktu k aktualizaci", required = true, example = "1") @PathVariable Long id,
            @Parameter(description = "Nová data produktu", required = true) @ModelAttribute ProductResponse productDto) {
        log.info("PUT /api/products/{} - Aktualizace produktu: {}", id, productDto);
        return productService.updateProduct(id, productDto)
                .map(productMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * ❌ Smazání produktu podle ID (pouze ADMIN)
     *
     * @param id ID produktu ke smazání
     * @return 204 No Content při úspěchu, 404 pokud produkt neexistuje
     */
    @Operation(summary = "Smazání produktu", description = "Smaže produkt podle ID včetně všech jeho obrázků. " +
            "Tento endpoint je dostupný pouze pro administrátory.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produkt byl úspěšně smazán", content = @Content),
            @ApiResponse(responseCode = "401", description = "Uživatel není přihlášen", content = @Content),
            @ApiResponse(responseCode = "403", description = "Uživatel nemá oprávnění (pouze ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produkt nebyl nalezen", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Pouze admin může mazat produkty
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID produktu ke smazání", required = true, example = "1") @PathVariable Long id) {
        log.info("DELETE /api/products/{} - Smazání produktu", id);
        return productService.deleteProductById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * 📋 Získání seznamu všech produktů s paginací
     *
     * @param pageable Parametry paginace (stránka, velikost, řazení)
     * @return Stránka produktů
     */
    @Operation(summary = "Získání seznamu produktů", description = "Vrátí stránkovaný seznam všech produktů. " +
            "Podporuje paginaci a řazení. " +
            "Tento endpoint je veřejný a nevyužívá autentizace.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seznam produktů byl úspěšně vrácen", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public Page<ProductResponse> getAllProducts(
            @Parameter(description = "Parametry paginace a řazení (page, size, sort)", example = "page=0&size=10&sort=name,asc") Pageable pageable) {
        log.info("GET /api/products - Získání seznamu všech produktů s paginací: {}", pageable);
        return productService.findAllProducts(pageable)
                .map(productMapper::toDto);
    }

}
