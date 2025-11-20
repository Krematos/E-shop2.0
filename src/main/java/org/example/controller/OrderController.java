package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mapper.OrderMapper;
import org.example.model.User;
import org.example.service.order.OrderService;
import org.example.dto.OrderDto;
import org.example.model.Order;
import org.example.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderMapper orderMapper;

    // ✅ Vytvoření nové objednávky
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody OrderDto orderDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Uživatel {} vytváří novou objednávku.", userDetails.getUsername());

        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nenalezen: " + userDetails.getUsername()));

        // Použijeme price z DTO (s @JsonProperty("Price") pro kompatibilitu s frontendem)
        BigDecimal price = orderDto.getPrice() != null ? orderDto.getPrice() : orderDto.getTotalPrice();
        
        Order createdOrder = orderService.createOrder(
                orderDto.getProductName(),
                orderDto.getQuantity(),
                price,
                currentUser
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toDto(createdOrder));
    }

    // ✅ Získání všech objednávek (ADMIN)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        log.info("Admin požadoval seznam všech objednávek.");
        List<OrderDto> orders = orderService.findAllOrders();
        return ResponseEntity.ok(orders);
    }

    // ✅ Získání objednávek přihlášeného uživatele
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<OrderDto>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nebyl nalezen po autentizaci."));

        List<OrderDto> orders = orderService.findOrdersByUser(currentUser.getUsername());
        return ResponseEntity.ok(orders);
    }

    // ✅ Detail objednávky podle ID
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails.getUsername();
        log.info("Uživatel {} požaduje detail objednávky s ID {}.", username, orderId);

        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nebyl nalezen po autentizaci."));

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Získáme OrderDto z service
        return orderService.findOrderById(orderId)
                .map(orderDto -> {
                    // Pro kontrolu oprávnění potřebujeme Order entitu
                    // Pokud není admin, ověříme že objednávka patří uživateli
                    // (toto ověření by mělo být v service vrstvě, ale pro teď to necháme zde)
                    if (!isAdmin) {
                        // Zkontrolujeme, zda objednávka patří uživateli přes service
                        List<OrderDto> userOrders = orderService.findOrdersByUser(currentUser.getUsername());
                        boolean belongsToUser = userOrders.stream()
                                .anyMatch(o -> o.getId().equals(orderId));
                        if (!belongsToUser) {
                            return null; // Vrátíme null, což způsobí 404
                        }
                    }
                    return orderDto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
