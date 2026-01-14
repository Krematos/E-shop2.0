package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {
        /*
         * @Mock
         * private OrderRepository orderRepository;
         * 
         * @InjectMocks
         * private OrderServiceImpl orderService;
         * 
         * @Mock
         * private ProductRepository productRepository;
         * 
         * @Mock
         * private OrderMapper orderMapper;
         * 
         * private User user;
         * private Product productPhone;
         * private Product productLaptop;
         * 
         * @BeforeEach
         * void setUp() {
         * // Příprava dat, která použijeme ve většině testů
         * user = User.builder()
         * .id(1L)
         * .username("testUser")
         * .email("test@example.com")
         * .build();
         * 
         * productPhone = Product.builder()
         * .id(100L)
         * .name("iPhone 15")
         * .price(BigDecimal.valueOf(20000))
         * .build();
         * 
         * productLaptop = Product.builder()
         * .id(200L)
         * .name("MacBook")
         * .price(BigDecimal.valueOf(50000))
         * .build();
         * }
         * // TEST úspěšného vytvoření objednávky
         * 
         * @Test
         * 
         * @DisplayName("✅ Vytvoření nové objednávky - úspěch")
         * void createOrder_Success() {
         * // 1. Request: 1x Telefon (20k) + 1x Laptop (50k)
         * OrderItemRequest req1 = new OrderItemRequest(); req1.setProductId(100L);
         * req1.setQuantity(1);
         * OrderItemRequest req2 = new OrderItemRequest(); req2.setProductId(200L);
         * req2.setQuantity(1);
         * 
         * CreateOrderRequest request = new CreateOrderRequest();
         * request.setOrderItems(List.of(req1, req2));
         * 
         * // 2. Mocking
         * when(productRepository.findById(100L)).thenReturn(Optional.of(productPhone));
         * when(productRepository.findById(200L)).thenReturn(Optional.of(productLaptop))
         * ;
         * when(orderRepository.save(any(Order.class))).thenAnswer(i ->
         * i.getArgument(0));
         * 
         * // 3. Akce
         * Order result = orderService.createOrder(request, user);
         * 
         * // 4. Ověření ceny: 20000 + 50000 = 70000
         * assertEquals(BigDecimal.valueOf(70000), result.getTotalPrice());
         * assertEquals(2, result.getOrderItems().size());
         * }
         * 
         * @Test
         * 
         * @DisplayName("Měl by ignorovat položky s nulovým nebo záporným množstvím")
         * void createOrder_ShouldSkipInvalidQuantities() {
         * // Request: 1x Telefon (Validní) a -5x Laptop (Nevalidní)
         * OrderItemRequest validReq = new OrderItemRequest();
         * validReq.setProductId(100L); validReq.setQuantity(1);
         * OrderItemRequest invalidReq = new OrderItemRequest();
         * invalidReq.setProductId(200L); invalidReq.setQuantity(-5);
         * 
         * CreateOrderRequest request = new CreateOrderRequest();
         * request.setOrderItems(List.of(validReq, invalidReq));
         * 
         * when(productRepository.findById(100L)).thenReturn(Optional.of(productPhone));
         * // Mock pro laptop není potřeba, protože by k němu kód neměl dojít
         * when(orderRepository.save(any(Order.class))).thenAnswer(i ->
         * i.getArgument(0));
         * 
         * Order result = orderService.createOrder(request, user);
         * 
         * // Měla by vzniknout objednávka jen s telefonem
         * assertEquals(1, result.getOrderItems().size());
         * assertEquals(BigDecimal.valueOf(20000), result.getTotalPrice());
         * }
         * 
         * // --- TESTY CHYB (EXCEPTIONS) ---
         * 
         * @Test
         * 
         * @DisplayName("Měl by vyhodit chybu, když uživatel je null")
         * void createOrder_Fail_NullUser() {
         * CreateOrderRequest request = new CreateOrderRequest();
         * assertThrows(IllegalArgumentException.class, () ->
         * orderService.createOrder(request, null));
         * }
         * 
         * @Test
         * 
         * @DisplayName("Měl by vyhodit chybu, když je košík prázdný")
         * void createOrder_Fail_EmptyCart() {
         * CreateOrderRequest request = new CreateOrderRequest();
         * request.setOrderItems(Collections.emptyList());
         * 
         * Exception exception = assertThrows(IllegalArgumentException.class,
         * () -> orderService.createOrder(request, user));
         * 
         * assertTrue(exception.getMessage().
         * contains("objednávka musí obsahovat alespoň jednu položku"));
         * }
         * 
         * @Test
         * 
         * @DisplayName("Měl by vyhodit chybu, když produkt neexistuje")
         * void createOrder_Fail_ProductNotFound() {
         * OrderItemRequest itemRequest = new OrderItemRequest();
         * itemRequest.setProductId(999L); // Neexistující ID
         * itemRequest.setQuantity(1);
         * 
         * CreateOrderRequest request = new CreateOrderRequest();
         * request.setOrderItems(List.of(itemRequest));
         * 
         * when(productRepository.findById(999L)).thenReturn(Optional.empty());
         * 
         * assertThrows(IllegalArgumentException.class,
         * () -> orderService.createOrder(request, user));
         * }
         * 
         * @Test
         * 
         * @DisplayName("Měl by vyhodit chybu, pokud po filtraci nezbydou žádné validní položky"
         * )
         * void createOrder_Fail_AllItemsInvalid() {
         * // Všechny položky mají množství 0
         * OrderItemRequest itemRequest = new OrderItemRequest();
         * itemRequest.setProductId(100L);
         * itemRequest.setQuantity(0);
         * 
         * CreateOrderRequest request = new CreateOrderRequest();
         * request.setOrderItems(List.of(itemRequest));
         * 
         * // Zde by měla servisa vyhodit výjimku na konci metody
         * Exception exception = assertThrows(IllegalArgumentException.class,
         * () -> orderService.createOrder(request, user));
         * 
         * assertTrue(exception.getMessage().
         * contains("Nepodařilo se přidat žádné platné položky"));
         * }
         * 
         * // --- TEST ČTENÍ ---
         * 
         * @Test
         * 
         * @DisplayName("Měl by najít objednávky uživatele a namapovat je na DTO")
         * void findOrdersByUser_Success() {
         * // Příprava dat z DB
         * Order order = Order.builder().id(1L).user(user).build();
         * List<Order> orders = List.of(order);
         * 
         * // Příprava očekávaného DTO
         * OrderDto orderDto = OrderDto.builder().id(1L).username("testUser").build();
         * 
         * // Mocking
         * when(orderRepository.findByUser_Username("testUser")).thenReturn(orders);
         * when(orderMapper.toDto(order)).thenReturn(orderDto);
         * 
         * // Akce
         * List<OrderDto> result = orderService.findOrdersByUser("testUser");
         * 
         * // Ověření
         * assertFalse(result.isEmpty());
         * assertEquals(1, result.size());
         * assertEquals(orderDto.getId(), result.get(0).getId());
         * }
         */
}
