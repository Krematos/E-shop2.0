package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_name", columnList = "name")
})
public class Product  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @NotBlank(message = "Název produktu nesmí být prázdný")
    @Size(max = 100, message = "Název produktu nesmí překročit 100 znaků")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Popis produktu nesmí být prázdný")
    @Size(max = 1000, message = "Popis produktu nesmí překročit 1000 znaků")
    @Column(name = "description", nullable = false)
    private String description;

    @NotBlank(message = "Cena produktu nesmí být prázdná")
    @DecimalMin(value = "0.01", inclusive = false, message = "Cena produktu musí být kladné číslo")
    @Digits(integer = 10, fraction = 2, message = "Cena produktu musí mít maximálně 10 číslic před desetinnou čárkou a 2 číslice za ní")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotBlank(message = "Kategorie produktu nesmí být prázdná")
    @Column(name = "category", nullable = false)
    private String category;

    private String currency;

    @ElementCollection
    private List<String> images = new ArrayList<>();

    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}
