package com.zetta.entity;

/**
 * Lifecycle states for transactions in the Smart Request Engine.
 * Represents the current state of a transaction request.
 */
public enum TransactionLifecycleState {
    PENDING,        // Request is pending confirmation
    COMPLETED,      // Transaction has been completed
    CANCELLED,      // Transaction has been cancelled
    FAILED,         // Transaction failed
    REJECTED        // Transaction was rejected
}

