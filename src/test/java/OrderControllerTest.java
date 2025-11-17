import org.example.controller.OrderController;
import org.example.dto.OrderDto;
import org.example.model.User;
import org.example.service.OrderService;
import org.example.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(OrderController.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;


    @Test
    @DisplayName("✅ Vytvoření nové objednávky - úspěch")
    @WithMockUser(username = "johndoe", roles = {"USER"})
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        // Mock user
        User mockUser = new User();
        mockUser.setUsername("testuser");

        // Mock Dto odpovědi
        OrderDto mockOrderDto = OrderDto.builder()
                .id(1L)
                .productName("Notebook")
                .quantity(2)
                .price(new BigDecimal("15000"))
                .totalPrice(new BigDecimal("30000"))
                .createdAt(LocalDateTime.now())
                .build();

        // Mockování service vrstev
        Mockito.when(userService.findUserByUsername("testuser"))
                .thenReturn(Optional.of(mockUser));

        Mockito.when(orderService.createOrder(Mockito.anyString(), Mockito.anyInt(), Mockito.any(), Mockito.any()))
                .thenReturn(orderService.createOrder("Notebook", 2, new BigDecimal("15000"), mockUser));

        // JSON tělo požadavku
        String jsonRequest = """
                {
                  "productName": "Notebook",
                  "quantity": 2,
                  "price": 15000
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Notebook"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(30000))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("📦 Získání všech objednávek (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllOrders_ShouldReturnList() throws Exception {
        OrderDto dto = OrderDto.builder()
                .id(1L)
                .productName("Monitor")
                .quantity(1)
                .price(new BigDecimal("5000"))
                .totalPrice(new BigDecimal("5000"))
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(orderService.findAllOrders()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName", is("Monitor")))
                .andExpect(jsonPath("$[0].totalPrice", is(5000)));
    }
}
