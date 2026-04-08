package com.um.labtrack.repository;

import com.um.labtrack.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Transaction entity operations.
 * Provides CRUD operations and custom query methods for transaction history.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a specific equipment item.
     *
     * @param equipmentId The equipment ID
     * @return List of transactions for the equipment
     */
    List<Transaction> findByEquipmentIdOrderByTimestampDesc(Long equipmentId);

    /**
     * Find all transactions by a specific user.
     *
     * @param userId The user ID
     * @return List of transactions performed by the user
     */
    List<Transaction> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find all transactions by action type.
     *
     * @param action The transaction action (CHECKIN or CHECKOUT)
     * @return List of transactions with the specified action
     */
    List<Transaction> findByActionOrderByTimestampDesc(Transaction.TransactionAction action);

    /**
     * Delete all transactions for a given user (e.g. before deleting the user).
     *
     * @param userId The user ID
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Transaction t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
