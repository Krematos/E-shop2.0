package org.example.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {
    private Long id;

    @NotBlank(message = "Název produktu je povinný")
    @Size(min = 2, max = 100, message = "Název musí mít 2–100 znaků")
    private String name;

    @NotBlank(message = "Popis produktu je povinný")
    @Size(max = 1000, message = "Popis nesmí překročit 1000 znaků")
    private String description;

    @NotNull(message = "Cena je povinná")
    @DecimalMin(value = "0.01", message = "Cena musí být větší než 0")
    @Digits(integer = 10, fraction = 2, message = "Neplatný formát ceny")
    private BigDecimal price;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
