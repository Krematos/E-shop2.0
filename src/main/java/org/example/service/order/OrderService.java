package org.example.service.order;

import org.example.dto.OrderDto;
import org.example.model.Order;
import org.example.model.User;
import org.springframework.stereotype.Service;

import org.example.dto.CreateOrderRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public interface OrderService {


    List<OrderDto> findAllOrders();

    Optional<OrderDto> findOrderById(Long id);

    List<OrderDto> findOrdersByUser(String userName);

    Order createOrder(CreateOrderRequest request, User currentUser);

}
