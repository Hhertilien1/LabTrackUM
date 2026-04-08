package com.um.labtrack.service;

import com.um.labtrack.entity.Transaction;
import com.um.labtrack.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Transaction business logic.
 * Handles transaction history queries and reporting.
 */
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Constructor-based dependency injection.
     *
     * @param transactionRepository Repository for transaction data access
     */
    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieve all transactions from the database, ordered by timestamp descending.
     *
     * @return List of all transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Find transaction by ID.
     *
     * @param id The transaction ID
     * @return Optional containing the Transaction if found
     */
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));
    }

    /**
     * Find all transactions for a specific equipment item.
     *
     * @param equipmentId The equipment ID
     * @return List of transactions for the equipment
     */
    public List<Transaction> getTransactionsByEquipment(Long equipmentId) {
        return transactionRepository.findByEquipmentIdOrderByTimestampDesc(equipmentId);
    }

    /**
     * Find all transactions by a specific user.
     *
     * @param userId The user ID
     * @return List of transactions performed by the user
     */
    public List<Transaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Find all transactions by action type.
     *
     * @param action The transaction action (CHECKIN or CHECKOUT)
     * @return List of transactions with the specified action
     */
    public List<Transaction> getTransactionsByAction(Transaction.TransactionAction action) {
        return transactionRepository.findByActionOrderByTimestampDesc(action);
    }
}
