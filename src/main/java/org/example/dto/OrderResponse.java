package org.example.dto;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String username,
        List<OrderItemResponse> items,
        BigDecimal totalPrice,
        String status,
        Instant createdAt
) {
}
