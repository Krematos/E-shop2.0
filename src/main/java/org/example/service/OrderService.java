package org.example.service;

import org.example.model.Order;
import org.example.model.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public interface OrderService {

    Order createOrder(String productName, int quantity, BigDecimal Price, User user);
    List<Order> findAllOrders();

    Optional<Order> findOrderById(Long id);

    List<Order> findOrdersByUser(String userName);

}
