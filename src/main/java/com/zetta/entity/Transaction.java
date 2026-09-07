package com.zetta.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_id", updatable = false, nullable = false)
    private UUID requestId;
    
    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", insertable = false, updatable = false)
    private User creator;
    
    @Column(name = "target_friend_id")
    private UUID targetFriendId;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_friend_id", insertable = false, updatable = false)
    private User targetFriend;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_state", nullable = false)
    private TransactionLifecycleState lifecycleState;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "is_sender_confirmed")
    private Boolean isSenderConfirmed;
    
    @Column(name = "is_receiver_confirmed")
    private Boolean isReceiverConfirmed;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Transaction() {
    }
    
    public Transaction(UUID creatorId, BigDecimal amount, TransactionLifecycleState lifecycleState) {
        this.creatorId = creatorId;
        this.amount = amount;
        this.lifecycleState = lifecycleState;
    }
    
    public Transaction(UUID creatorId, UUID targetFriendId, BigDecimal amount, TransactionLifecycleState lifecycleState) {
        this.creatorId = creatorId;
        this.targetFriendId = targetFriendId;
        this.amount = amount;
        this.lifecycleState = lifecycleState;
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
    
    public User getCreator() {
        return creator;
    }
    
    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    public UUID getTargetFriendId() {
        return targetFriendId;
    }
    
    public void setTargetFriendId(UUID targetFriendId) {
        this.targetFriendId = targetFriendId;
    }
    
    public User getTargetFriend() {
        return targetFriend;
    }
    
    public void setTargetFriend(User targetFriend) {
        this.targetFriend = targetFriend;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public TransactionLifecycleState getLifecycleState() {
        return lifecycleState;
    }
    
    public void setLifecycleState(TransactionLifecycleState lifecycleState) {
        this.lifecycleState = lifecycleState;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
