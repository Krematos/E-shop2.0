package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mapper.OrderMapper;
import org.example.model.User;
import org.example.service.OrderService;
import org.example.dto.OrderDto;
import org.example.model.Order;
import org.example.mapper.OrderMapper;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nenalezen: " + userDetails.getUsername()));

        Order createdOrder = orderService.createOrder(
                orderDto.getProductName(),
                orderDto.getQuantity(),
                orderDto.getPrice(),
                currentUser
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toDto(createdOrder));
    }

    // ✅ Získání všech objednávek (ADMIN)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(
                orderService.findAllOrders()
                        .stream()
                        .map(orderMapper::toDto)
                        .collect(Collectors.toList())
        );
    }

    // ✅ Získání objednávek přihlášeného uživatele
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<OrderDto>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nebyl nalezen po autentizaci."));

        return ResponseEntity.ok(
                orderService.findOrdersByUser(currentUser.getUsername())
                        .stream()
                        .map(orderMapper::toDto)
                        .collect(Collectors.toList())
        );
    }

    // ✅ Detail objednávky podle ID
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nebyl nalezen po autentizaci."));

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return orderService.findOrderById(orderId)
                .filter(order -> isAdmin || order.getUser().getId().equals(currentUser.getId()))
                .map(orderMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
