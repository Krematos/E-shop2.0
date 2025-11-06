package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    
    @NotNull(message = "Cena je povinná")
    @Min(value = 0, message = "Cena nesmí být záporná")
    @JsonProperty("Price") // Podpora pro frontend, který posílá "Price" s velkým P
    private BigDecimal price;
    
    private BigDecimal totalPrice;

    private LocalDateTime createdAt;
    private List<OrderItemDto> orderItems;

}
