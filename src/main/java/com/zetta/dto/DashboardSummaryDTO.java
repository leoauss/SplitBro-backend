package com.zetta.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO representing the dashboard summary with total amounts to pay and receive.
 */
public class DashboardSummaryDTO {
    
    private UUID userId;
    private BigDecimal totalToPay;      // Total amount the user owes to all friends
    private BigDecimal totalToReceive;  // Total amount all friends owe to the user
    private int activeFriendsCount;     // Number of active friends
    private int pendingTransactionsCount; // Number of pending transactions
    
    public DashboardSummaryDTO() {
    }
    
    public DashboardSummaryDTO(UUID userId, BigDecimal totalToPay, BigDecimal totalToReceive) {
        this.userId = userId;
        this.totalToPay = totalToPay != null ? totalToPay : BigDecimal.ZERO;
        this.totalToReceive = totalToReceive != null ? totalToReceive : BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public BigDecimal getTotalToPay() {
        return totalToPay != null ? totalToPay : BigDecimal.ZERO;
    }
    
    public void setTotalToPay(BigDecimal totalToPay) {
        this.totalToPay = totalToPay;
    }
    
    public BigDecimal getTotalToReceive() {
        return totalToReceive != null ? totalToReceive : BigDecimal.ZERO;
    }
    
    public void setTotalToReceive(BigDecimal totalToReceive) {
        this.totalToReceive = totalToReceive;
    }
    
    public int getActiveFriendsCount() {
        return activeFriendsCount;
    }
    
    public void setActiveFriendsCount(int activeFriendsCount) {
        this.activeFriendsCount = activeFriendsCount;
    }
    
    public int getPendingTransactionsCount() {
        return pendingTransactionsCount;
    }
    
    public void setPendingTransactionsCount(int pendingTransactionsCount) {
        this.pendingTransactionsCount = pendingTransactionsCount;
    }
}

