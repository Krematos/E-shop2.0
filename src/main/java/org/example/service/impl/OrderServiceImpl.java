package org.example.service.impl;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.OrderDto;
import org.example.mapper.OrderMapper;
import org.example.model.OrderItem;
import org.example.model.Product;
import org.example.model.User;
import org.example.model.Order;
import org.example.dto.OrderDto;
import org.example.mapper.OrderMapper;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;
import org.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {


    private final OrderMapper orderMapper;

     private final OrderRepository orderRepository;

     private final ProductRepository productRepository;





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
     @CacheEvict(value = {"ordersById", "allOrders", "ordersByUser"}, allEntries = true)
    public Order createOrder(String productName, int quantity, BigDecimal price, User user) {
         if (user == null) throw new IllegalArgumentException("Uživatel nesmí být null");
         if (quantity <= 0) throw new IllegalArgumentException("Počet kusů musí být kladný");
         if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
             throw new IllegalArgumentException("Cena musí být kladná");

         Product product = productRepository.findByName(productName)
                 .orElseThrow(() -> new IllegalArgumentException("Produkt nebyl nalezen: " + productName));
        // Vytvoření položky objednávky
         OrderItem orderItem = new OrderItem();
         orderItem.setProductName(product.getName());
         orderItem.setQuantity(quantity);
         orderItem.setPrice(price);
         orderItem.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));
         orderItem.setCreatedAt(LocalDateTime.now());
         orderItem.setProduct(product);
            log.info("Vytvořena položka objednávky: {} x {} za cenu {}", quantity, productName, orderItem.getTotalPrice());
            // Vytvoření objednávky
         Order order = new Order();
         order.setOrderDate(LocalDateTime.now());
         order.setUser(user);
         order.setOrderItems(List.of(orderItem));
         order.setTotalPrice(orderItem.getTotalPrice());
         orderItem.setOrder(order); // vazba zpět na objednávku (1:N vztah)

         order.setOrderItems(List.of(orderItem));
         log.info("Vytvořena objednávka pro uživatele {} s celkovou cenou {}", user.getUsername(), order.getTotalPrice());

         Order savedOrder = orderRepository.save(order);
         log.info("Vytvořena objednávka {} pro uživatele {}", savedOrder.getId(), user.getUsername());

         return savedOrder;
     }


    /**
     * Najde všechny objednávky uživatele s cachováním.
     */
    @Override
    @Cacheable(value = "ordersByUser", key = "#userName")
    public List<OrderDto> findOrdersByUser(String userName) {
        return orderRepository.findByUser_Username(userName)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }
    /**
     * Najde všechny objednávky s cachováním.
     */
    @Override
    @Cacheable(value = "allOrders")
    public List<OrderDto> findAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }
    /**
     * Najde objednávku podle ID.
     */
    @Override
    public Optional<OrderDto> findOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto);
    }
    /**
     * Validuje vstupy pro vytvoření objednávky.
     */
    private  void validateOrderInput(String productName, int quantity, User user) {
        if (user == null) {
            throw new IllegalArgumentException("Uživatel nesmí být null");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Název produktu nesmí být prázdný");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Počet kusů musí být kladný");
        }
    }
}
