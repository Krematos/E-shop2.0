package org.example.dto;


import java.math.BigDecimal;

public record OrderItemResponse(

        String productName,
        int quantity,
        BigDecimal price,
        BigDecimal totalPrice
) {
}
