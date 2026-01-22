package org.example.dto.order;


public record OrderItemRequest(
        Long productId,
        int quantity
) {
}
