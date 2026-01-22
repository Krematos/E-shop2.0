package org.example.dto.order;


import org.example.dto.order.OrderItemRequest;

import java.util.List;

public record CreateOrderRequest(        Long customerId,
        List<OrderItemRequest> items
) {

}
