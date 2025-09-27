import org.example.model.Order;
import org.example.model.User;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

        @Mock
        private OrderRepository orderRepository;


        private OrderService orderService;

        private ProductRepository productRepository;



        @BeforeEach
        void setUp() {
            orderService = new OrderServiceImpl(orderRepository, productRepository);
        }

     @Test
     void testCreateOrder() {
         orderService = new OrderServiceImpl(orderRepository, productRepository);
         User user = new User();
         user.setUsername("John Doe");
         Order order = new Order();
         order.setProduct("Test Product");
         order.setQuantity(2);
         order.setTotalPrice(100.0);
         order.setUser(user);

         Mockito.when(orderRepository.save(Mockito.any(Order.class))).thenReturn(order);

         // Act
        Order createdOrder = orderService.createOrder("Test Product", 2, 100.0, user);

         // Assert
        assertNotNull(createdOrder);
        assertEquals("Test Product", createdOrder.getProduct());
        assertEquals(2, createdOrder.getQuantity());
         assertEquals(100.0, createdOrder.getTotalPrice());
         assertEquals(user, createdOrder.getUser());
         assertEquals("John Doe", createdOrder.getUser().getUsername());
     }
}
