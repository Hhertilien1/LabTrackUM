package com.um.labtrack.repository;

import com.um.labtrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Spring Data JPA automatically provides CRUD operations and query methods.
 * This interface extends JpaRepository, which includes methods like save, findById, findAll, delete, etc.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username.
     * Spring Data JPA automatically implements this method based on the method name.
     *
     * @param username The username to search for
     * @return Optional containing the User if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists with the given username.
     *
     * @param username The username to check
     * @return true if user exists, false otherwise
     */
    boolean existsByUsername(String username);
}
