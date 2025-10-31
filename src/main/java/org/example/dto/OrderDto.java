package org.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.example.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    @NotBlank(message = "Název produktu nesmí být prázdný")
    private String productName;

    @NotNull(message = "Množství je povinné")
    @Min(value = 1, message = "Množství musí být alespoň 1")
    private Integer quantity;
    @NotNull(message = "Cena je povinná ")
    @Min(value = 0, message = "Cena nesmí být záporná")
    public BigDecimal Price;
    private BigDecimal totalPrice;

    private LocalDateTime createdAt;

}
