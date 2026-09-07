package com.zetta.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing the net difference calculation between two users.
 * Contains the master sum and information about who owes whom.
 */
public class NetDifferenceDTO {
    
    private UUID userId1;
    private UUID userId2;
    private BigDecimal masterSum;
    private UUID owesUserId;      // The user who owes money
    private UUID owedToUserId;     // The user who is owed money
    private String message;        // Human-readable message about who owes whom
    private List<UUID> activeTransactionIds;  // IDs of all pending transactions for Single Click Settlement
    
    public NetDifferenceDTO() {
    }
    
    public NetDifferenceDTO(UUID userId1, UUID userId2, BigDecimal masterSum, 
                          UUID owesUserId, UUID owedToUserId) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.masterSum = masterSum;
        this.owesUserId = owesUserId;
        this.owedToUserId = owedToUserId;
        this.message = generateMessage();
        this.activeTransactionIds = java.util.Collections.emptyList();
    }
    
    public NetDifferenceDTO(UUID userId1, UUID userId2, BigDecimal masterSum, 
                          UUID owesUserId, UUID owedToUserId, List<UUID> activeTransactionIds) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.masterSum = masterSum;
        this.owesUserId = owesUserId;
        this.owedToUserId = owedToUserId;
        this.message = generateMessage();
        this.activeTransactionIds = activeTransactionIds;
    }
    
    private String generateMessage() {
        if (masterSum.compareTo(BigDecimal.ZERO) == 0) {
            return "No outstanding balance between users";
        } else if (masterSum.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("User %s owes User %s $%.2f", 
                               owesUserId, owedToUserId, masterSum);
        } else {
            return String.format("User %s owes User %s $%.2f", 
                               owedToUserId, owesUserId, masterSum.abs());
        }
    }
    
    // Getters and Setters
    public UUID getUserId1() {
        return userId1;
    }
    
    public void setUserId1(UUID userId1) {
        this.userId1 = userId1;
    }
    
    public UUID getUserId2() {
        return userId2;
    }
    
    public void setUserId2(UUID userId2) {
        this.userId2 = userId2;
    }
    
    public BigDecimal getMasterSum() {
        return masterSum;
    }
    
    public void setMasterSum(BigDecimal masterSum) {
        this.masterSum = masterSum;
        this.message = generateMessage();
    }
    
    public UUID getOwesUserId() {
        return owesUserId;
    }
    
    public void setOwesUserId(UUID owesUserId) {
        this.owesUserId = owesUserId;
        this.message = generateMessage();
    }
    
    public UUID getOwedToUserId() {
        return owedToUserId;
    }
    
    public void setOwedToUserId(UUID owedToUserId) {
        this.owedToUserId = owedToUserId;
        this.message = generateMessage();
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<UUID> getActiveTransactionIds() {
        return activeTransactionIds;
    }
    
    public void setActiveTransactionIds(List<UUID> activeTransactionIds) {
        this.activeTransactionIds = activeTransactionIds;
    }
}

