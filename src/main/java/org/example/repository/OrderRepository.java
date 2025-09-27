package org.example.repository;

import org.example.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser_Username(String username); // Assuming Order has a field 'user' with a 'username' property

    List<Order> findByProductName(String name); // Assuming Order has a field 'product' with a 'name' property

}
