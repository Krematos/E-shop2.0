package org.example.service.impl;


import org.example.dto.OrderDto;
import org.example.model.OrderItem;
import org.example.model.Product;
import org.example.model.User;
import org.example.model.Order;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.example.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

     private final OrderRepository orderRepository;

     private final ProductRepository productRepository;



     public OrderServiceImpl(OrderRepository orderRepository,
                             ProductRepository productRepository) {
         this.productRepository = productRepository;
        this.orderRepository = orderRepository;
     }

        /**
        * Vytvoří novou objednávku.
        *
        * @param productName Název produktu.
        * @param quantity Počet kusů.
        * @param user Uživatel, který objednávku vytvořil.
        * @return Vytvořená objednávka.
        */

     @Override
     @Transactional
    public Order createOrder(String productName, int quantity, BigDecimal price, User user) {
         Product product = productRepository.findByName(productName)
                 .orElseThrow(() -> new IllegalArgumentException("Produkt nebyl nalezen: " + productName));

         Order order = new Order();
         order.setOrderDate(LocalDateTime.now());
         order.setUser(user);

         OrderItem orderItem = new OrderItem();
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(quantity);
            orderItem.setPrice(price);
            orderItem.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));
            orderItem.setOrder(order);

        order.setOrderItems(Collections.singletonList(orderItem));
        order.setTotalPrice(orderItem.getTotalPrice());

         return orderRepository.save(order);
     }



    @Override
    public List<Order> findOrdersByUser(String userName) {
        return orderRepository.findByUser_Username(userName);
    }

    @Override
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> findOrderById(Long id) {
        return orderRepository.findById(id);
    }


}
