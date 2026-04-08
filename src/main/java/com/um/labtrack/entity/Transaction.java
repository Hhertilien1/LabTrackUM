package com.um.labtrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Transaction Entity representing check-in and check-out operations.
 * Tracks who performed the action, which equipment was involved, and when it occurred.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionAction action;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Enumeration for transaction action types.
     */
    public enum TransactionAction {
        CHECKIN, CHECKOUT
    }

    /**
     * Default constructor required by JPA.
     */
    public Transaction() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with required fields.
     *
     * @param equipment The equipment involved in the transaction
     * @param user      The user performing the action
     * @param action    The type of action (CHECKIN or CHECKOUT)
     */
    public Transaction(Equipment equipment, User user, TransactionAction action) {
        this.equipment = equipment;
        this.user = user;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TransactionAction getAction() {
        return action;
    }

    public void setAction(TransactionAction action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
