package org.example.repository;

import java.util.List;
import org.example.model.OrderItem;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository  extends JdbcParameter {
    /**
     * Najde položky objednávky podle ID objednávky.
     *
     * @param orderId ID objednávky
     * @return seznam položek objednávky spojených s daným ID objednávky
     */
    List<OrderItem> findByOrderId(Long orderId);
    /**
     * Najde položky objednávky podle ID produktu a ID objednávky.
     *
     * @param productId ID produktu
     * @param orderId ID objednávky
     * @return seznam položek objednávky spojených s daným ID produktu a ID objednávky
     */

    List<OrderItem> findByProductIdAndProductId(Long productId, Long orderId);
}
