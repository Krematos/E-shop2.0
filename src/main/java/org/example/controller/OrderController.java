package org.example.controller;

import jakarta.validation.Valid;
import org.example.model.User;
import org.example.service.OrderService;
import org.example.dto.OrderDto;
import org.example.model.Order;
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
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;



    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER' , 'ADMIN')") // Pouze uživatelé a admini mohou vytvářet objednávky
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Uživatel nenalezen: " + userDetails.getUsername()));
        Order newOder = orderService.createOrder(orderDto.getProductName(), orderDto.getQuantity(), orderDto.getPrice(),  currentUser);
        return new ResponseEntity<>(convertToDto(newOder), HttpStatus.CREATED);
    }
    /**
     * Zobrazí všechny objednávky pro administrátora.
     * Vyžaduje ROLE_ADMIN.
     * @return Seznam OrderDto.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderDto> getAllOrders() {
        return orderService.findAllOrders().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Zobrazí objednávky pro přihlášeného uživatele.
     * Vyžaduje ROLE_USER nebo ROLE_ADMIN.
     * @param userDetails Aktuálně přihlášený uživatel.
     * @return Seznam OrderDto.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<OrderDto> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Uživatel nebyl nalezen po autentizaci."));

        return orderService.findOrdersByUser(currentUser.getUsername()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Zobrazí detail konkrétní objednávky.
     * Uživatel může vidět pouze své objednávky, admin jakoukoli.
     * @param orderId ID objednávky.
     * @param userDetails Aktuálně přihlášený uživatel.
     * @return OrderDto.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long orderId,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Uživatel nebyl nalezen po autentizaci."));

        return orderService.findOrderById(orderId)
                .filter(order -> order.getUser().getId().equals(currentUser.getId()) || userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // --- Pomocné metody pro konverzi mezi entitou a DTO ---
    private OrderDto convertToDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setProductName(order.getProductName());
        orderDto.setQuantity(order.getQuantity());
        orderDto.setTotalPrice(order.getTotalPrice());
        return orderDto;
    }
}
