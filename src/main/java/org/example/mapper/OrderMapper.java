package org.example.mapper;

import org.example.model.Order;
import org.example.dto.OrderDto;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "quantity", target = "quantity")
    OrderDto toDto(Order order);

    @InheritInverseConfiguration
    Order toEntity(OrderDto dto);

}
