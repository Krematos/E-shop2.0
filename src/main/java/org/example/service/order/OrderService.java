package org.example.service.order;

import org.example.dto.OrderResponse;
import org.example.model.Order;
import org.example.model.User;
import org.springframework.stereotype.Service;

import org.example.dto.CreateOrderRequest;

import java.util.List;
import java.util.Optional;

@Service
public interface OrderService {


    List<OrderResponse> findAllOrders();

    Optional<OrderResponse> findOrderById(Long id);

    List<OrderResponse> findOrdersByUser(String userName);

    Order createOrder(CreateOrderRequest request, User currentUser);

}
