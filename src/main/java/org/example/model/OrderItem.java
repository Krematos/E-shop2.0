package org.example.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "order_items")
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId; // ID produktu

    @Column(name = "quantity", nullable = false)
    private int quantity; // Množství produktu

    @Column(name = "product_name", nullable = false)
    private String productName; // Jméno produktu

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice; // celková cena položky

    @Column(name = "price", nullable = false)
    private BigDecimal price; // cena za jednotku

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Související objednávka


}
