package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CreateOrderRequest;
import org.example.mapper.OrderMapper;
import org.example.model.User;
import org.example.service.order.OrderService;
import org.example.dto.OrderResponse;
import org.example.model.Order;
import org.example.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderMapper orderMapper;

    /**
     * ✅ Vytvoření nové objednávky
     * @param request
     * @param userDetails
     * @return
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
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
     * ✅ Získání všech objednávek (ADMIN)
     * @return
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Admin požadoval seznam všech objednávek.");
        List<OrderResponse> orders = orderService.findAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * ✅ Získání objednávek přihlášeného uživatele
     * @param userDetails
     * @return
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@Valid @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nebyl nalezen po autentizaci."));

        List<OrderResponse> orders = orderService.findOrdersByUser(currentUser.getUsername());
        return ResponseEntity.ok(orders);
    }

    /**
     * ✅ Detail objednávky podle ID
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOwner(#orderId, principal.username)")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId
    ) {
        log.info("Požadavek na detail objednávky s ID {}.", orderId);

        // Získá OrderDto z service
        return orderService.findOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


}
