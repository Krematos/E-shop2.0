package krematos.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import krematos.dto.order.CreateOrderRequest;
import krematos.dto.order.OrderResponse;
import krematos.dto.order.OrderItemRequest;
import krematos.mapper.OrderMapper;
import krematos.model.OrderItem;
import krematos.model.Product;
import krematos.model.User;
import krematos.model.Order;
import krematos.repository.OrderRepository;
import krematos.repository.ProductRepository;
import krematos.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private final OrderMapper orderMapper;

     private final OrderRepository orderRepository;

     private final ProductRepository productRepository;

        /**
        * Vytvoří novou objednávku.
        *
        * @param request  Požadavek na vytvoření objednávky obsahující název produktu a počet kusů.
        * @param user Uživatel, který objednávku vytvořil.
        * @return Vytvořená objednávka.
        */

     @Override
     @Transactional
     @CacheEvict(value = {"ordersById", "allOrders", "ordersByUser"}, allEntries = true)
    public Order createOrder(CreateOrderRequest request, User user) {

         // 1. Validace základních vstupů
         if (user == null) throw new IllegalArgumentException("Uživatel nesmí být null");
         if (request.customerId() == null || request.items().isEmpty()) {
             throw new IllegalArgumentException("Objednávka musí obsahovat alespoň jednu položku");
         }

         log.info("Vytvářím objednávku pro uživatele: {}", user.getUsername());

         // 2. Vytvoření kostry objednávky (zatím bez položek a s cenou 0)
         Order order = Order.builder()
                 .user(user)
                 .orderDate(Instant.now())
                 .orderItems(new ArrayList<>()) // Inicializace listu
                 .totalPrice(BigDecimal.ZERO)
                 .build();

         // Pomocná proměnná pro sčítání celkové ceny
         BigDecimal runningTotal = BigDecimal.ZERO;

         // 3. Iterace přes položky v košíku (Requestu)
         for (OrderItemRequest itemRequest : request.items()) {

             // Validace množství
             if (itemRequest.quantity() <= 0) {
                 log.warn("Položka s ID {} má neplatné množství {}, přeskakuji.", itemRequest.productId(), itemRequest.quantity());
                 continue;
             }

             // A. Načtení produktu z DB (Zásadní pro získání správné ceny!)
             Product product = productRepository.findById(itemRequest.productId())
                     .orElseThrow(() -> new IllegalArgumentException("Produkt nenalezen ID: " + itemRequest.productId()));

             // B. Výpočet ceny za položku (Cena produktu * Množství)
             BigDecimal itemTotalPrice = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));

             // C. Vytvoření entity OrderItem
             OrderItem orderItem = OrderItem.builder()
                     .order(order) // Nastavení vazby na rodiče
                     .productId(product.getId())
                     .productName(product.getName())
                     .quantity(itemRequest.quantity())
                     .price(product.getPrice()) // Ukládá jednotkovou cenu v době nákupu
                     .totalPrice(itemTotalPrice)
                     .build();

             // D. Přidání do seznamu v objednávce
             order.getOrderItems().add(orderItem);

             // E. Přičtení k celkové ceně objednávky
             runningTotal = runningTotal.add(itemTotalPrice);
         }

         // 4. Finální nastavení celkové ceny a uložení
         if (order.getOrderItems().isEmpty()) {
             throw new IllegalArgumentException("Nepodařilo se přidat žádné platné položky do objednávky.");
         }

         order.setTotalPrice(runningTotal);

         Order savedOrder = orderRepository.save(order);
         log.info("Objednávka ID {} úspěšně uložena. Celková cena: {}", savedOrder.getId(), savedOrder.getTotalPrice());

         return savedOrder;
     }


    /**
     * Najde všechny objednávky uživatele s cachováním.
     */
    @Override
    @Cacheable(value = "ordersByUser", key = "#userName")
    public List<OrderResponse> findOrdersByUser(String userName) {
        log.info("Hledání objednávek pro uživatele: {}", userName);
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
    public List<OrderResponse> findAllOrders() {
        log.info("Načítání všech objednávek");
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }
    /**
     * Najde objednávku podle ID.
     */
    @Override
    public Optional<OrderResponse> findOrderById(Long id) {
        log.info("Hledání objednávky podle ID: {}", id);
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
