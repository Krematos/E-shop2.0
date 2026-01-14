package org.example.dto;


public record OrderItemRequest(
        Long productId,
        int quantity
) {
}
