package org.example.mapper;

import org.example.dto.OrderItemDto;
import org.example.model.Order;
import org.example.dto.OrderDto;
import org.example.model.OrderItem;
import org.example.model.Product;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Optional;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    // --- Order → OrderDto ---
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "orderItems", target = "items")
    OrderDto toDto(Order order);

    List<OrderDto> toDtoList(List<Order> orders);

    @Mapping(source = "price", target = "pricePerUnit")
    OrderItemDto toOrderItemDto(OrderItem orderItem);


    List<OrderItemDto> toOrderItemDtoList(List<OrderItem> orderItems);

}
