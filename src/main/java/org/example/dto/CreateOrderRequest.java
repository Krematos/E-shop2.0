package org.example.dto;

import lombok.Data;

import java.util.List;
@Data
public class CreateOrderRequest {
    private List<OrderItemRequest> orderItems;
}
