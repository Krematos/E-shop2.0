package org.example.repository;

import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/**
 * Repository pro entitu {@link User}.
 * Obsahuje základní CRUD operace a specifické dotazy podle uživatelského jména a emailu.
 */
@Repository
public interface UserRepository extends JpaRepository <User, Long> {
    /**
     * Najde uživatele podle jeho uživatelského jména.
     *
     * @param username uživatelské jméno
     * @return {@link Optional} obsahující uživatele, pokud existuje
     */
    Optional<User> findByUsername(String username);

    /**
     * Najde uživatele podle jeho emailu.
     *
     * @param email emailová adresa
          * @return {@link Optional} obsahující uživatele, pokud existuje
          */
         Optional<User> findByEmail(String email);
     
         /**
          * Najde uživatele podle jeho resetovacího tokenu.
          *
          * @param token resetovací token
          * @return {@link Optional} obsahující uživatele, pokud existuje
          */
         Optional<User> findByPasswordResetToken(String token);
     
         /**
          * Zkontroluje, zda uživatel s daným uživatelským jménem existuje.
          *
          * @param username uživatelské jméno
     * @return true pokud uživatel existuje, jinak false
     */
    boolean existsByUsername(String username);
    /**
     * Zkontroluje, zda uživatel s daným emailovým adresou existuje.
     *
     * @param email emailová adresa
     * @return true pokud uživatel existuje, jinak false
     */
    boolean existsByEmail(String email);
}
