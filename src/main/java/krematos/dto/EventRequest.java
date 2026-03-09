package krematos.dto;

public record EventRequest(
        Long userId,
        Long productId,
        Double price,
        Integer quantity,
        String eventType
) {
}
