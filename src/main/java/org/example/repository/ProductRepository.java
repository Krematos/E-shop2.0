package org.example.repository;


import org.example.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    /**
     * Najde produkt podle jeho názvu.
     *
     * @param name název produktu
     * @return {@link Optional} obsahující produkt, pokud existuje
     */
    Optional<Product> findByName(String name);
    /**
     * Najde produkt podle jeho popisu.
     *
     * @param description popis produktu
     * @return produkt, pokud existuje
     */

    Product findByDescription(String description);
    /**
     * Najde produkt podle jeho ceny.
     *
     * @param price cena produktu
     * @return produkt, pokud existuje
     */

    Product findByPrice(double price);
}
