package org.example.model;


import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Uživatel, který vytvořil objednávku
    // Produkt, který je objednán
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Produkt, který je objednán
    // Položky objednávky
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate; // Datum a čas vytvoření objednávky
    private int quantity; // Množství objednaného produktu
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice; // Celková cena objednávky

    // --- Metody pro přidání a odebrání položek objednávky ---
    public void addOrderItem(OrderItem item) {
        if(item == null) {
            throw new IllegalArgumentException("OrderItem nesmí být null");
        }
        orderItems.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        if(item == null) {
            throw new IllegalArgumentException("OrderItem nesmí být null");
        }
        orderItems.remove(item);
        item.setOrder(null);
    }

}
