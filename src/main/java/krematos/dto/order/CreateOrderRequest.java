package krematos.dto.order;


import java.util.List;

public record CreateOrderRequest(        Long customerId,
        List<OrderItemRequest> items
) {

}
