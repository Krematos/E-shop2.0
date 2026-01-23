package krematos.dto.order;


public record OrderItemRequest(
        Long productId,
        int quantity
) {
}
