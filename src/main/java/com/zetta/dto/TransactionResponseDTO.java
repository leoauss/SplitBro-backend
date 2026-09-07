package com.zetta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.zetta.entity.Transaction;
import com.zetta.entity.TransactionLifecycleState;

/**
 * DTO for returning transaction data with user names.
 */
public class TransactionResponseDTO {
    
    private UUID requestId;
    private UUID creatorId;
    private String creatorName;
    private UUID targetFriendId;
    private String targetFriendName;
    private BigDecimal amount;
    private String title;
    private TransactionLifecycleState lifecycleState;
    private Boolean isSenderConfirmed;
    private Boolean isReceiverConfirmed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public TransactionResponseDTO() {
    }
    
    public TransactionResponseDTO(Transaction transaction, String creatorName, String targetFriendName) {
        this.requestId = transaction.getRequestId();
        this.creatorId = transaction.getCreatorId();
        this.creatorName = creatorName;
        this.targetFriendId = transaction.getTargetFriendId();
        this.targetFriendName = targetFriendName;
        this.amount = transaction.getAmount();
        this.title = transaction.getTitle();
        this.lifecycleState = transaction.getLifecycleState();
        this.isSenderConfirmed = transaction.getIsSenderConfirmed();
        this.isReceiverConfirmed = transaction.getIsReceiverConfirmed();
        this.createdAt = transaction.getCreatedAt();
        this.updatedAt = transaction.getUpdatedAt();
    }
    
    // Getters and Setters
    public UUID getRequestId() {
        return requestId;
    }
    
    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }
    
    public UUID getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getCreatorName() {
        return creatorName;
    }
    
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
    
    public UUID getTargetFriendId() {
        return targetFriendId;
    }
    
    public void setTargetFriendId(UUID targetFriendId) {
        this.targetFriendId = targetFriendId;
    }
    
    public String getTargetFriendName() {
        return targetFriendName;
    }
    
    public void setTargetFriendName(String targetFriendName) {
        this.targetFriendName = targetFriendName;
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
    
    public TransactionLifecycleState getLifecycleState() {
        return lifecycleState;
    }
    
    public void setLifecycleState(TransactionLifecycleState lifecycleState) {
        this.lifecycleState = lifecycleState;
    }
    
    public Boolean getIsSenderConfirmed() {
        return isSenderConfirmed;
    }
    
    public void setIsSenderConfirmed(Boolean isSenderConfirmed) {
        this.isSenderConfirmed = isSenderConfirmed;
    }
    
    public Boolean getIsReceiverConfirmed() {
        return isReceiverConfirmed;
    }
    
    public void setIsReceiverConfirmed(Boolean isReceiverConfirmed) {
        this.isReceiverConfirmed = isReceiverConfirmed;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
