package com.zetta.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new transaction request.
 * Note: creatorId is optional since it's extracted from the JWT token.
 */
public class TransactionRequestDTO {
    
    // creatorId is optional - the server extracts it from the JWT token
    private UUID creatorId;
    
    private UUID targetFriendId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    private String title;
    
    public TransactionRequestDTO() {
    }
    
    public TransactionRequestDTO(UUID creatorId, UUID targetFriendId, BigDecimal amount, String title) {
        this.creatorId = creatorId;
        this.targetFriendId = targetFriendId;
        this.amount = amount;
        this.title = title;
    }
    
    // Getters and Setters
    public UUID getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }
    
    public UUID getTargetFriendId() {
        return targetFriendId;
    }
    
    public void setTargetFriendId(UUID targetFriendId) {
        this.targetFriendId = targetFriendId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
}

