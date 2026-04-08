package com.um.labtrack.controller;

import com.um.labtrack.entity.Transaction;
import com.um.labtrack.service.AuthService;
import com.um.labtrack.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Transaction history operations.
 * Provides endpoints for retrieving transaction logs.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthService authService;

    /**
     * Constructor-based dependency injection.
     *
     * @param transactionService Service for transaction business logic
     * @param authService        Service for authentication
     */
    @Autowired
    public TransactionController(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    private ResponseEntity<?> checkAuth() {
        if (!authService.isAuthenticated()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        return null;
    }

    /**
     * Get all transactions, optionally filtered by user or equipment.
     *
     * @param userId      Optional user ID to filter transactions by user
     * @param equipmentId Optional equipment ID to filter transactions by equipment
     * @return ResponseEntity containing list of transactions
     */
    @GetMapping
    public ResponseEntity<?> getAllTransactions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long equipmentId) {
        ResponseEntity<?> authCheck = checkAuth();
        if (authCheck != null) return authCheck;
        List<Transaction> transactions;
        if (userId != null) {
            transactions = transactionService.getTransactionsByUser(userId);
        } else if (equipmentId != null) {
            transactions = transactionService.getTransactionsByEquipment(equipmentId);
        } else {
            transactions = transactionService.getAllTransactions();
        }
        return ResponseEntity.ok(transactions);
    }
}
