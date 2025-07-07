package org.example.repository;

import java.util.List;
import org.example.model.OrderItem;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository  extends JdbcParameter {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByProductIdAndProductId(Long productId, Long orderId);
}
