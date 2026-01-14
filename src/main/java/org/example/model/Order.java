package org.example.model;


import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID objednávky
    // Uživatel, který vytvořil objednávku
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Uživatel, který vytvořil objednávku
    // Produkt, který je objednán

    // Položky objednávky
    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>(); // Položky objednávky

    @Column(name = "order_date", nullable = false)
    private Instant orderDate; // Datum a čas vytvoření objednávky

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice; // Celková cena objednávky

    // --- Metody pro přidání a odebrání položek objednávky ---
    public void addOrderItem(OrderItem item) {
        if(item == null) {
            throw new IllegalArgumentException("OrderItem nesmí být null");
        }
        orderItems.add(item);
        item.setOrder(this);
        recalculateTotalPrice();
    }

    public void removeOrderItem(OrderItem item) {
        if(item == null) {
            throw new IllegalArgumentException("OrderItem nesmí být null");
        }
        orderItems.remove(item);
        item.setOrder(null);
        recalculateTotalPrice();
    }

    public void recalculateTotalPrice() {
        this.totalPrice = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
