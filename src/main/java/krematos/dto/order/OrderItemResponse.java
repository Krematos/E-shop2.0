package krematos.dto.order;


import java.math.BigDecimal;

public record OrderItemResponse(

        String productName,
        int quantity,
        BigDecimal price,
        BigDecimal totalPrice
) {
}
