package org.example.repository;

import org.example.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * Najde objednávky podle uživatelského jména.
     *
     * @param username uživatelské jméno
     * @return seznam objednávek spojených s daným uživatelským jménem
     */
    List<Order> findByUser_Username(String username);
    /**
     * Najde objednávky podle názvu produktu.
     *
     * @param name název produktu
     * @return seznam objednávek obsahujících daný produkt
     */
    List<Order> findByProductName(String name);

}
