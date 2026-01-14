package org.example.mapper;

import org.example.dto.OrderItemResponse;
import org.example.model.Order;
import org.example.dto.OrderResponse;
import org.example.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
