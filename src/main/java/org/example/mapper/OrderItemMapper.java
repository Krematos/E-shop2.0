package org.example.mapper;

import org.example.dto.OrderItemDto;
import org.example.model.OrderItem;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    // --- Entity → DTO ---
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "totalPrice", target = "totalPrice")
    OrderItemDto toDto(OrderItem orderItem);

    // --- DTO → Entity ---
    @InheritInverseConfiguration
    @Mapping(target = "order", ignore = true) // zabrání cyklické závislosti
    @Mapping(target = "productId", ignore = true) // bude nastaveno ručně
    OrderItem toEntity(OrderItemDto dto);
}
