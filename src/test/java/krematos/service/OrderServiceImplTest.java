package krematos.service;

import krematos.dto.order.CreateOrderRequest;
import krematos.dto.order.OrderItemRequest;
import krematos.dto.order.OrderResponse;
import krematos.mapper.OrderMapper;
import krematos.model.Order;
import krematos.model.OrderItem;
import krematos.model.Product;
import krematos.model.User;
import krematos.repository.OrderRepository;
import krematos.repository.ProductRepository;
import krematos.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit testy pro OrderServiceImpl.
 *
 * Pokrývají:
 *  - createOrder (úspěch, filtrace nevalidních položek, null user, prázdný košík,
 *                 produkt nenalezen, všechny položky jsou nevalidní)
 *  - findOrdersByUser / findAllOrders / findOrderById
 *  - pomocné metody Order entity (addOrderItem, removeOrderItem, recalculateTotalPrice)
 *
 * POZNÁMKY k věcem, které by měly být dodělány ve službě:
 *  - deleteOrder(Long id)      – zatím chybí v OrderService i OrderServiceImpl
 *  - cancelOrder(Long id)      – pro storno objednávky (zatím chybí)
 *  - OrderStatus enum          – OrderResponse má pole "status", ale doména status nesleduje
 *  - Stock management          – při vytvoření objednávky by se měl odečíst sklad
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
@ActiveProfiles("test")
class OrderServiceImplTest {

    // ──────────────────────────────────────────────
    //  Mocks & Subject Under Test
    // ──────────────────────────────────────────────

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    // ──────────────────────────────────────────────
    //  Sdílená testovací data
    // ──────────────────────────────────────────────

    private static final Long USER_ID      = 1L;
    private static final Long PRODUCT_ID_A = 100L;
    private static final Long PRODUCT_ID_B = 200L;
    private static final Long ORDER_ID     = 10L;

    private User    user;
    private Product phone;
    private Product laptop;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .username("testUser")
                .email("test@example.com")
                .build();

        phone = Product.builder()
                .id(PRODUCT_ID_A)
                .name("iPhone 15")
                .price(BigDecimal.valueOf(20_000))
                .build();

        laptop = Product.builder()
                .id(PRODUCT_ID_B)
                .name("MacBook Pro")
                .price(BigDecimal.valueOf(50_000))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helper factories
    // ─────────────────────────────────────────────────────────────────────────

    /** Vytvoří jednoduchý CreateOrderRequest s jednou položkou. */
    private CreateOrderRequest singleItemRequest(Long productId, int qty) {
        return new CreateOrderRequest(USER_ID, List.of(new OrderItemRequest(productId, qty)));
    }

    /** Vytvoří CreateOrderRequest s více položkami. */
    private CreateOrderRequest multiItemRequest(OrderItemRequest... items) {
        return new CreateOrderRequest(USER_ID, List.of(items));
    }

    /** Vytvoří jednoduchý OrderResponse DTO. */
    private OrderResponse buildOrderResponse(long id, String username, BigDecimal total) {
        return new OrderResponse(id, username, Collections.emptyList(), total, "CREATED", Instant.now());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  createOrder – úspěšné případy
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createOrder – úspěch")
    class CreateOrderSuccess {

        @Test
        @DisplayName("Jedna položka – telefon (1 ks × 20 000 = 20 000 Kč)")
        void shouldCreateOrder_WithSingleItem() {
            // Given
            when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(phone));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Order result = orderService.createOrder(singleItemRequest(PRODUCT_ID_A, 1), user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOrderItems()).hasSize(1);
            assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20_000));
            assertThat(result.getUser()).isEqualTo(user);
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Dvě položky – telefon + laptop = 70 000 Kč")
        void shouldCreateOrder_WithMultipleItems() {
            // Given
            when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(phone));
            when(productRepository.findById(PRODUCT_ID_B)).thenReturn(Optional.of(laptop));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateOrderRequest request = multiItemRequest(
                    new OrderItemRequest(PRODUCT_ID_A, 1),
                    new OrderItemRequest(PRODUCT_ID_B, 1)
            );

            // When
            Order result = orderService.createOrder(request, user);

            // Then
            assertThat(result.getOrderItems()).hasSize(2);
            assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(70_000));
        }

        @Test
        @DisplayName("Více kusů jednoho produktu (3 × iPhone = 60 000 Kč)")
        void shouldCalculateTotalPrice_ForMultipleQuantities() {
            // Given
            when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(phone));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Order result = orderService.createOrder(singleItemRequest(PRODUCT_ID_A, 3), user);

            // Then
            assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(60_000));
            assertThat(result.getOrderItems()).hasSize(1);
            assertThat(result.getOrderItems().get(0).getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("OrderItem má správné informace o produktu")
        void shouldStoreProductInfoInOrderItem() {
            // Given
            when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(phone));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Order result = orderService.createOrder(singleItemRequest(PRODUCT_ID_A, 2), user);

            // Then
            OrderItem item = result.getOrderItems().get(0);
            assertThat(item.getProductId()).isEqualTo(PRODUCT_ID_A);
            assertThat(item.getProductName()).isEqualTo("iPhone 15");
            assertThat(item.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(20_000));
            assertThat(item.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(40_000));
        }

        @Test
        @DisplayName("Datum objednávky je nastaveno")
        void shouldSetOrderDate() {
            // Given
            when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(phone));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Instant before = Instant.now();

            // When
            Order result = orderService.createOrder(singleItemRequest(PRODUCT_ID_A, 1), user);

            // Then
            assertThat(result.getOrderDate()).isNotNull();
            assertThat(result.getOrderDate()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("Položka s množstvím 1 prochází validací")
        void shouldAcceptQuantityOfOne() {
            // Given
            when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(phone));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // When & Then
            assertThatCode(() -> orderService.createOrder(singleItemRequest(PRODUCT_ID_A, 1), user))
                    .doesNotThrowAnyException();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  createOrder – filtrace nevalidních položek
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createOrder – filtrace nevalidních položek")
    class CreateOrderFiltering {

        @Test
        @DisplayName("Záporné množství je přeskočeno; objednávka vznikne jen s validní položkou")
        void shouldSkipNegativeQuantity() {
            // Given
            when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(phone));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateOrderRequest request = multiItemRequest(
                    new OrderItemRequest(PRODUCT_ID_A, 1),    // validní
                    new OrderItemRequest(PRODUCT_ID_B, -5)     // nevalidní – přeskočit
            );

            // When
            Order result = orderService.createOrder(request, user);

            // Then
            assertThat(result.getOrderItems()).hasSize(1);
            assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20_000));
            // Produkt B by se nikdy neměl vyhledávat
            verify(productRepository, never()).findById(PRODUCT_ID_B);
        }

        @Test
        @DisplayName("Nulové množství je přeskočeno")
        void shouldSkipZeroQuantity() {
            // Given
            when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(phone));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateOrderRequest request = multiItemRequest(
                    new OrderItemRequest(PRODUCT_ID_A, 1),
                    new OrderItemRequest(PRODUCT_ID_B, 0)
            );

            // When
            Order result = orderService.createOrder(request, user);

            // Then
            assertThat(result.getOrderItems()).hasSize(1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  createOrder – chybové stavy (exceptions)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createOrder – chybové stavy")
    class CreateOrderErrors {

        @Test
        @DisplayName("Null user → IllegalArgumentException")
        void shouldThrow_WhenUserIsNull() {
            // Given
            CreateOrderRequest request = singleItemRequest(PRODUCT_ID_A, 1);

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(request, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Prázdný seznam položek → IllegalArgumentException")
        void shouldThrow_WhenItemsListIsEmpty() {
            // Given – customerId je nastaveno, ale items je prázdný list
            CreateOrderRequest request = new CreateOrderRequest(USER_ID, Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(request, user))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(orderRepository, never()).save(any());
        }
        /*
        @Test
        @DisplayName("Null seznam položek → IllegalArgumentException")
        void shouldThrow_WhenItemsListIsNull() {
            // Given – customerId je nastaveno, ale items je null
            CreateOrderRequest request = new CreateOrderRequest(USER_ID, null);

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(request, user))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(orderRepository, never()).save(any());
        } */

        @Test
        @DisplayName("Null customerId → IllegalArgumentException")
        void shouldThrow_WhenCustomerIdIsNull() {
            // Given – customerId = null, byť items není prázdný
            CreateOrderRequest request = new CreateOrderRequest(null, List.of(new OrderItemRequest(PRODUCT_ID_A, 1)));

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(request, user))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Produkt neexistuje → IllegalArgumentException")
        void shouldThrow_WhenProductNotFound() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());
            CreateOrderRequest request = singleItemRequest(999L, 1);

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(request, user))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("999");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Všechny položky mají neplatné množství → IllegalArgumentException (žádné platné položky)")
        void shouldThrow_WhenAllItemsHaveInvalidQuantity() {
            // Given – obě položky mají qty <= 0
            CreateOrderRequest request = multiItemRequest(
                    new OrderItemRequest(PRODUCT_ID_A, 0),
                    new OrderItemRequest(PRODUCT_ID_B, -3)
            );

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(request, user))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("platné položky");

            verify(orderRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  findOrdersByUser
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findOrdersByUser Tests")
    class FindOrdersByUserTests {

        @Test
        @DisplayName("Vrátí seznam DTO pro existující uživatele")
        void shouldReturnMappedOrders_WhenUserHasOrders() {
            // Given
            Order order = Order.builder().id(ORDER_ID).user(user).totalPrice(BigDecimal.valueOf(20_000)).build();
            OrderResponse dto = buildOrderResponse(ORDER_ID, "testUser", BigDecimal.valueOf(20_000));

            when(orderRepository.findByUser_Username("testUser")).thenReturn(List.of(order));
            when(orderMapper.toDto(order)).thenReturn(dto);

            // When
            List<OrderResponse> result = orderService.findOrdersByUser("testUser");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(ORDER_ID);
            assertThat(result.get(0).username()).isEqualTo("testUser");
            assertThat(result.get(0).totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20_000));

            verify(orderRepository, times(1)).findByUser_Username("testUser");
            verify(orderMapper, times(1)).toDto(order);
        }

        @Test
        @DisplayName("Vrátí prázdný seznam, když uživatel nemá žádné objednávky")
        void shouldReturnEmptyList_WhenUserHasNoOrders() {
            // Given
            when(orderRepository.findByUser_Username("newUser")).thenReturn(Collections.emptyList());

            // When
            List<OrderResponse> result = orderService.findOrdersByUser("newUser");

            // Then
            assertThat(result).isEmpty();
            verify(orderMapper, never()).toDto(any());
        }

        @Test
        @DisplayName("Vrátí všechny objednávky uživatele (více objednávek)")
        void shouldReturnAllOrders_ForUser() {
            // Given
            Order order1 = Order.builder().id(1L).user(user).totalPrice(BigDecimal.valueOf(20_000)).build();
            Order order2 = Order.builder().id(2L).user(user).totalPrice(BigDecimal.valueOf(50_000)).build();
            OrderResponse dto1 = buildOrderResponse(1L, "testUser", BigDecimal.valueOf(20_000));
            OrderResponse dto2 = buildOrderResponse(2L, "testUser", BigDecimal.valueOf(50_000));

            when(orderRepository.findByUser_Username("testUser")).thenReturn(List.of(order1, order2));
            when(orderMapper.toDto(order1)).thenReturn(dto1);
            when(orderMapper.toDto(order2)).thenReturn(dto2);

            // When
            List<OrderResponse> result = orderService.findOrdersByUser("testUser");

            // Then
            assertThat(result).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  findAllOrders
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findAllOrders Tests")
    class FindAllOrdersTests {

        @Test
        @DisplayName("Vrátí všechny objednávky namapované na DTO")
        void shouldReturnAllOrders() {
            // Given
            Order order = Order.builder().id(ORDER_ID).user(user).totalPrice(BigDecimal.valueOf(30_000)).build();
            OrderResponse dto = buildOrderResponse(ORDER_ID, "testUser", BigDecimal.valueOf(30_000));

            when(orderRepository.findAll()).thenReturn(List.of(order));
            when(orderMapper.toDto(order)).thenReturn(dto);

            // When
            List<OrderResponse> result = orderService.findAllOrders();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(ORDER_ID);
            verify(orderRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Vrátí prázdný seznam, když nejsou žádné objednávky")
        void shouldReturnEmptyList_WhenNoOrders() {
            // Given
            when(orderRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<OrderResponse> result = orderService.findAllOrders();

            // Then
            assertThat(result).isEmpty();
            verify(orderMapper, never()).toDto(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  findOrderById
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findOrderById Tests")
    class FindOrderByIdTests {

        @Test
        @DisplayName("Vrátí Optional<OrderResponse>, když objednávka existuje")
        void shouldReturnOrder_WhenOrderExists() {
            // Given
            Order order = Order.builder().id(ORDER_ID).user(user).totalPrice(BigDecimal.valueOf(20_000)).build();
            OrderResponse dto = buildOrderResponse(ORDER_ID, "testUser", BigDecimal.valueOf(20_000));

            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderMapper.toDto(order)).thenReturn(dto);

            // When
            Optional<OrderResponse> result = orderService.findOrderById(ORDER_ID);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(ORDER_ID);
            verify(orderRepository, times(1)).findById(ORDER_ID);
        }

        @Test
        @DisplayName("Vrátí prázdný Optional, když objednávka neexistuje")
        void shouldReturnEmpty_WhenOrderDoesNotExist() {
            // Given
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<OrderResponse> result = orderService.findOrderById(999L);

            // Then
            assertThat(result).isEmpty();
            verify(orderMapper, never()).toDto(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Order entity – pomocné metody
    //  (addOrderItem, removeOrderItem, recalculateTotalPrice)
    //  Tyto testy jsou čistě unit testy modelu – bez mockování.
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Order entity – helper metody")
    class OrderEntityHelperTests {

        private Order buildEmptyOrder() {
            return Order.builder()
                    .id(1L)
                    .user(user)
                    .orderDate(Instant.now())
                    .orderItems(new ArrayList<>())
                    .totalPrice(BigDecimal.ZERO)
                    .build();
        }

        private OrderItem buildItem(BigDecimal unit, int qty) {
            return OrderItem.builder()
                    .productId(PRODUCT_ID_A)
                    .productName("Test")
                    .price(unit)
                    .quantity(qty)
                    .totalPrice(unit.multiply(BigDecimal.valueOf(qty)))
                    .build();
        }

        @Test
        @DisplayName("addOrderItem přidá položku a přepočítá celkovou cenu")
        void addOrderItem_ShouldAddItemAndRecalculate() {
            // Given
            Order order = buildEmptyOrder();
            OrderItem item = buildItem(BigDecimal.valueOf(500), 2);

            // When
            order.addOrderItem(item);

            // Then
            assertThat(order.getOrderItems()).hasSize(1);
            assertThat(order.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(1000));
            assertThat(item.getOrder()).isEqualTo(order); // obousměrná vazba
        }

        @Test
        @DisplayName("addOrderItem pro null položku → IllegalArgumentException")
        void addOrderItem_ShouldThrow_WhenItemIsNull() {
            Order order = buildEmptyOrder();

            assertThatThrownBy(() -> order.addOrderItem(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("removeOrderItem odstraní položku a přepočítá cenu")
        void removeOrderItem_ShouldRemoveAndRecalculate() {
            // Given
            Order order = buildEmptyOrder();
            OrderItem item1 = buildItem(BigDecimal.valueOf(1000), 1);
            OrderItem item2 = buildItem(BigDecimal.valueOf(500), 2);
            order.addOrderItem(item1);
            order.addOrderItem(item2);
            assertThat(order.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(2000));

            // When
            order.removeOrderItem(item1);

            // Then
            assertThat(order.getOrderItems()).hasSize(1);
            assertThat(order.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(1000));
            assertThat(item1.getOrder()).isNull(); // vazba uvolněna
        }

        @Test
        @DisplayName("removeOrderItem pro null položku → IllegalArgumentException")
        void removeOrderItem_ShouldThrow_WhenItemIsNull() {
            Order order = buildEmptyOrder();

            assertThatThrownBy(() -> order.removeOrderItem(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("recalculateTotalPrice vrátí 0, když je seznam prázdný")
        void recalculateTotalPrice_ShouldReturnZero_WhenNoItems() {
            // Given
            Order order = buildEmptyOrder();

            // When
            order.recalculateTotalPrice();

            // Then
            assertThat(order.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("recalculateTotalPrice správně sečte více položek")
        void recalculateTotalPrice_ShouldSumAllItems() {
            // Given
            Order order = buildEmptyOrder();
            // Přidáme ručně, abychom testovali recalculate izolovaně
            OrderItem i1 = buildItem(BigDecimal.valueOf(100), 3); // 300
            OrderItem i2 = buildItem(BigDecimal.valueOf(200), 2); // 400
            i1.setOrder(order);
            i2.setOrder(order);
            order.getOrderItems().add(i1);
            order.getOrderItems().add(i2);

            // When
            order.recalculateTotalPrice();

            // Then
            assertThat(order.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(700));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TODO testy (věci, které by měly být dodělány)
    //   - deleteOrder(Long id) – vyhodit OrderNotFoundException / vrátit boolean
    //   - cancelOrder(Long id) – změnit status na CANCELLED
    //   - createOrder s kontrolou skladu (ProductNotFoundException + InsufficientStockException)
    //   - stránkovaný výpis objednávek (Pageable)
    // ─────────────────────────────────────────────────────────────────────────
}
