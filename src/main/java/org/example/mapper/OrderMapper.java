package org.example.mapper;

import org.example.model.Order;
import org.example.dto.OrderDto;
import org.example.model.Product;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Optional;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {
    // --- Order → OrderDto ---
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "totalPrice", target = "totalPrice")
    @Mapping(source = "orderDate", target = "createdAt")
    @Mapping(target = "orderItems", source = "orderItems")
    @Mapping(target = "price", expression = "java(order.getOrderItems() != null && !order.getOrderItems().isEmpty() ? order.getOrderItems().get(0).getPrice() : order.getTotalPrice().divide(java.math.BigDecimal.valueOf(order.getQuantity()), java.math.RoundingMode.HALF_UP))")
    OrderDto toDto(Order order);

    @InheritInverseConfiguration
    @Mapping(target = "user", ignore = true) // nebude se mapovat automaticky
    @Mapping(target = "product", ignore = true) // nastavíš ručně podle jména produktu
    Order toEntity(OrderDto dto);

    // === Pomocné metody pro extrakci hodnot z položek objednávky ===
    default String getProductName(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return Optional.ofNullable(order.getProduct())
                    .map(Product::getName)
                    .orElse(null);
        }
        return order.getOrderItems().get(0).getProductName();
    }

    default Integer getQuantity(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return order.getQuantity();
        }
        return order.getOrderItems().get(0).getQuantity();
    }
}
