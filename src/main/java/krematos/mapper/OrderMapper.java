package krematos.mapper;

import krematos.model.Order;
import krematos.model.OrderItem;
import krematos.dto.order.OrderItemResponse;
import krematos.dto.order.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    // --- Order → OrderDto ---

    OrderResponse toDto(Order order);

    List<OrderResponse> toDtoList(List<Order> orders);


    OrderItemResponse toOrderItemDto(OrderItem orderItem);


    List<OrderItemResponse> toOrderItemDtoList(List<OrderItem> orderItems);

}
