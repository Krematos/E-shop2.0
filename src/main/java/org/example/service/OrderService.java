package org.example.service;

import org.example.dto.OrderDto;
import org.example.model.Order;
import org.example.model.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public interface OrderService {

    Order createOrder(String productName, int quantity, BigDecimal Price, User user);
    List<OrderDto> findAllOrders();

    Optional<OrderDto> findOrderById(Long id);

    List<OrderDto> findOrdersByUser(String userName);

}
