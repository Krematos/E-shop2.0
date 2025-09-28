import org.example.model.Order;
import org.example.model.Product;
import org.example.model.User;
import org.example.repository.ProductRepository;
import org.example.service.OrderService;
import org.example.repository.OrderRepository;
import org.example.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {
        private static final Logger log = LoggerFactory.getLogger(OrderServiceImplTest.class);
        @Mock
        private OrderRepository orderRepository;


        private OrderService orderService;
        @Mock
        private ProductRepository productRepository;



        @BeforeEach
        void setUp() {
            orderService = new OrderServiceImpl(orderRepository, productRepository);
        }

     @Test
     void testCreateOrder() {
         log.info("Start testCreateOrder");
         orderService = new OrderServiceImpl(orderRepository, productRepository);
         User user = new User();
         user.setUsername("John Doe");
         Product product = new Product();
         product.setName("Test Product");

         Mockito.when(productRepository.findByName(Mockito.anyString())).thenReturn(Optional.of(product));

            Order order = new Order();
            order.setProduct(product);
         order.setQuantity(2);
         order.setTotalPrice(BigDecimal.valueOf(100.0));
         order.setUser(user);

         Mockito.when(orderRepository.save(Mockito.any(Order.class))).thenReturn(order);

         // Act
        Order createdOrder = orderService.createOrder("Test Product", 2, BigDecimal.valueOf(100.0), user);

         // Assert
        assertNotNull(createdOrder);
        assertEquals("Test Product", createdOrder.getProduct().getName());
        assertEquals(2, createdOrder.getQuantity());
         assertEquals(BigDecimal.valueOf(100.0), createdOrder.getTotalPrice());
         assertEquals(user, createdOrder.getUser());
         assertEquals("John Doe", createdOrder.getUser().getUsername());

         log.info("Created order: {}", createdOrder);
     }
}
