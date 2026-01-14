package org.example.dto;


import java.util.List;

public record CreateOrderRequest(        Long customerId,
        List<OrderItemRequest> items
) {

}
