package krematos.service.order;

import krematos.dto.order.OrderResponse;
import krematos.model.Order;
import krematos.model.User;
import org.springframework.stereotype.Service;

import krematos.dto.order.CreateOrderRequest;

import java.util.List;
import java.util.Optional;

@Service
public interface OrderService {


    List<OrderResponse> findAllOrders();

    Optional<OrderResponse> findOrderById(Long id);

    List<OrderResponse> findOrdersByUser(String userName);

    Order createOrder(CreateOrderRequest request, User currentUser);

}
