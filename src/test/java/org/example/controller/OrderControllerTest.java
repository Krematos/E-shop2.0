package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.SecurityConfig;
import org.example.dto.CreateOrderRequest;
import org.example.dto.OrderResponse;
import org.example.mapper.OrderMapper;
import org.example.model.Order;
import org.example.model.User;
import org.example.security.JwtAuthenticationFilter;
import org.example.service.impl.UserDetailsServiceImpl;
import org.example.service.order.OrderService;
import org.example.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.example.security.JwtAuthenticationFilter;
import org.example.service.JwtService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Musí mocknout všechny závislosti, které Controller vyžaduje v konstruktoru
    @MockBean
    private OrderService orderService;

    @MockBean
    private UserService userService;

    @MockBean
    private OrderMapper orderMapper;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtService jwtService;



    // --- 1. TEST VYTVOŘENÍ OBJEDNÁVKY (POST) ---

    @Test
    @DisplayName("Vytvoření objednávky - Úspěch (201 Created)")
    @WithMockUser(username = "jan.novak", roles = "USER") // Simuluje přihlášeného uživatele
    void createOrder_Success() throws Exception {
        // Příprava dat
        CreateOrderRequest request = new CreateOrderRequest( 0L, Collections.emptyList());


        User mockUser = new User();
        mockUser.setUsername("jan.novak");

        Order mockOrder = new Order();
        mockOrder.setId(1L);

        // Odpověď, kterou vrátí Mapper
        OrderResponse responseDto = new OrderResponse(1L, "jan.novak", Collections.emptyList(), BigDecimal.TEN, "CREATED", Instant.now());

        // Definice chování mocků
        when(userService.findUserByUsername("jan.novak")).thenReturn(Optional.of(mockUser));
        when(orderService.createOrder(any(CreateOrderRequest.class), any(User.class))).thenReturn(mockOrder);
        when(orderMapper.toDto(mockOrder)).thenReturn(responseDto);

        // Provedení testu
        mockMvc.perform(post("/api/orders")
                        .with(csrf()) // Obchází CSRF ochranu u POST metod
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Očekává 201
                .andExpect(jsonPath("$.id").value(1L));

    }

    // --- 2. TEST ZÍSKÁNÍ VŠECH OBJEDNÁVEK (ADMIN ONLY) ---

    @Test
    @DisplayName("Admin získá všechny objednávky - Úspěch (200 OK)")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllOrders_Success() throws Exception {
        OrderResponse responseDto = new OrderResponse(
                1L, "user1", Collections.emptyList(), BigDecimal.valueOf(100), "PAID", Instant.now()
        );

        when(orderService.findAllOrders()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("Běžný user nesmí vidět všechny objednávky (403 Forbidden)")
    @WithMockUser(username = "user", roles = "USER") // Má jen roli USER
    void getAllOrders_ForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/orders/all"))
                .andExpect(status().isForbidden()); // Očekává 403
    }

    // --- 3. TEST ZÍSKÁNÍ VLASTNÍCH OBJEDNÁVEK ---

    @Test
    @DisplayName("Uživatel získá své objednávky - Úspěch (200 OK)")
    @WithMockUser(username = "jan.novak", roles = "USER")
    void getUserOrders_Success() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("jan.novak");

        OrderResponse responseDto = new OrderResponse(5L, "jan.novak", Collections.emptyList(), BigDecimal.valueOf(500), "SHIPPED", Instant.now());

        when(userService.findUserByUsername("jan.novak")).thenReturn(Optional.of(mockUser));
        when(orderService.findOrdersByUser("jan.novak")).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    // --- 4. TEST DETAILU OBJEDNÁVKY ---

    @Test
    @DisplayName("Získání detailu existující objednávky (200 OK)")
    @WithMockUser(username = "admin", roles = "ADMIN") // Admin může vidět vše
    void getOrderById_Found() throws Exception {
        Long orderId = 123L;
        OrderResponse responseDto = new OrderResponse(orderId, "adam123", Collections.emptyList(), BigDecimal.ZERO,  "PENDING", Instant.now());

        when(orderService.findOrderById(orderId)).thenReturn(Optional.of(responseDto));

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    @DisplayName("Získání detailu neexistující objednávky (404 Not Found)")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getOrderById_NotFound() throws Exception {
        Long orderId = 999L;
        // Service vrátí empty Optional -> Controller vrátí 404
        when(orderService.findOrderById(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }
}
