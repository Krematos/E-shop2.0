package krematos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import krematos.model.Order;
import krematos.model.User;
import krematos.service.order.OrderService;
import krematos.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import krematos.dto.order.CreateOrderRequest;
import krematos.mapper.OrderMapper;
import krematos.dto.order.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller pro správu objednávek.
 * Poskytuje endpointy pro vytváření, zobrazení a správu objednávek.
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Objednávky", description = "API pro správu objednávek (vytváření, zobrazení, správa objednávek)")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderMapper orderMapper;

    /**
     * 🛒 Vytvoření nové objednávky
     *
     * @param request     Požadavek s detaily objednávky (položky, dodací adresa,
     *                    atd.)
     * @param userDetails Autentizovaný uživatel
     * @return Vytvořená objednávka
     */
    @Operation(summary = "Vytvoření nové objednávky", description = "Vytvoří novou objednávku pro přihlášeného uživatele. "
            +
            "Vyžaduje autentizaci (JWT token). " +
            "Objednávka obsahuje položky z košíku, dodací informace a způsob platby.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Objednávka byla úspěšně vytvořena", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Neplatná data v požadavku (chybí položky, neplatná adresa)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Uživatel není přihlášen", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produkt z objednávky nebyl nalezen", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponse> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Detaily nové objednávky", required = true, content = @Content(schema = @Schema(implementation = CreateOrderRequest.class))) @Valid @RequestBody CreateOrderRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PŘIJATÝ JSON REQUEST: {}", request);
        log.info("Vytváření nové objednávky pro uživatele: {}", userDetails.getUsername());
        // Získání aktuálního uživatele
        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nenalezen: " + userDetails.getUsername()));

        // Vytvoření objednávky pomocí service
        Order createdOrder = orderService.createOrder(request, currentUser);
        // Mapping na DTO a vrácení odpovědi
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toDto(createdOrder));
    }

    /**
     * 📋 Získání všech objednávek (pouze ADMIN)
     *
     * @return Seznam všech objednávek v systému
     */
    @Operation(summary = "Získání všech objednávek", description = "Vrátí seznam všech objednávek v systému. " +
            "Tento endpoint je dostupný pouze pro administrátory.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seznam objednávek byl úspěšně vrácen", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "401", description = "Uživatel není přihlášen", content = @Content),
            @ApiResponse(responseCode = "403", description = "Uživatel nemá oprávnění (pouze ADMIN)", content = @Content)
    })
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Admin požadoval seznam všech objednávek.");
        List<OrderResponse> orders = orderService.findAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * 👤 Získání objednávek přihlášeného uživatele
     *
     * @param userDetails Autentizovaný uživatel
     * @return Seznam objednávek aktuálního uživatele
     */
    @Operation(summary = "Získání objednávek uživatele", description = "Vrátí seznam všech objednávek přihlášeného uživatele. "
            +
            "Vyžaduje autentizaci (JWT token).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seznam objednávek uživatele byl úspěšně vrácen", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "401", description = "Uživatel není přihlášen", content = @Content),
            @ApiResponse(responseCode = "404", description = "Uživatel nebyl nalezen", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @Parameter(hidden = true) @Valid @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nebyl nalezen po autentizaci."));

        List<OrderResponse> orders = orderService.findOrdersByUser(currentUser.getUsername());
        return ResponseEntity.ok(orders);
    }

    /**
     * 🔍 Detail objednávky podle ID
     *
     * @param orderId ID objednávky
     * @return Detail objednávky
     */
    @Operation(summary = "Získání detailu objednávky", description = "Vrátí detail konkrétní objednávky podle ID. " +
            "Uživatel může zobrazit pouze své vlastní objednávky, admin může zobrazit všechny.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detail objednávky byl úspěšně vrácen", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "401", description = "Uživatel není přihlášen", content = @Content),
            @ApiResponse(responseCode = "403", description = "Uživatel nemá oprávnění zobrazit tuto objednávku", content = @Content),
            @ApiResponse(responseCode = "404", description = "Objednávka nebyla nalezena", content = @Content)
    })
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOwner(#orderId, principal.username)")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "ID objednávky", required = true, example = "1") @PathVariable Long orderId) {
        log.info("Požadavek na detail objednávky s ID {}.", orderId);

        // Získá OrderDto z service
        return orderService.findOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
