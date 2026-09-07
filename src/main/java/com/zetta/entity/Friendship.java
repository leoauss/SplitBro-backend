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
@Table(name = "friendship_relations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Friendship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "connection_id", updatable = false, nullable = false)
    private UUID connectionId;
    
    @Column(name = "user_a_id", nullable = false)
    private UUID userAId;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", insertable = false, updatable = false)
    private User userA;
    
    @Column(name = "user_b_id", nullable = false)
    private UUID userBId;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", insertable = false, updatable = false)
    private User userB;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendshipStatus status;
    
    @Column(name = "current_net_balance", precision = 19, scale = 2)
    private BigDecimal currentNetBalance;
    
    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        sortIds();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        sortIds();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Final guard to ensure the database constraint user_a_id < user_b_id is never violated.
     * Swaps userAId and userBId if userAId is greater than userBId.
     * Uses string comparison to match PostgreSQL's lexicographic UUID comparison.
     */
    private void sortIds() {
        if (userAId != null && userBId != null) {
            // Use string comparison to match PostgreSQL's lexicographic UUID ordering
            int comparison = userAId.toString().compareTo(userBId.toString());
            if (comparison > 0) {
                UUID temp = userAId;
                userAId = userBId;
                userBId = temp;
                
                // Also swap the User entities if they are set
                User tempUser = userA;
                userA = userB;
                userB = tempUser;
            }
        }
    }
    
    /**
     * Sets user A ID directly.
     */
    public void setUserAId(UUID userAId) {
        this.userAId = userAId;
    }
    
    /**
     * Sets user B ID directly.
     */
    public void setUserBId(UUID userBId) {
        this.userBId = userBId;
    }
    
    /**
     * Sets both user IDs with proper sorting.
     * Ensures userA is always the smaller UUID (using string comparison for PostgreSQL compatibility).
     */
    public void setUserIds(UUID userId1, UUID userId2) {
        // Use string comparison to match PostgreSQL's lexicographic UUID ordering
        if (userId1.toString().compareTo(userId2.toString()) < 0) {
            this.userAId = userId1;
            this.userBId = userId2;
        } else {
            this.userAId = userId2;
            this.userBId = userId1;
        }
    }
    
    // Constructors
    public Friendship() {
    }
    
    public Friendship(UUID userAId, UUID userBId, FriendshipStatus status) {
        setUserIds(userAId, userBId);
        this.status = status;
    }
    
    // Getters and Setters
    public UUID getConnectionId() {
        return connectionId;
    }
    
    public void setConnectionId(UUID connectionId) {
        this.connectionId = connectionId;
    }
    
    public UUID getUserAId() {
        return userAId;
    }
    
    public UUID getUserBId() {
        return userBId;
    }
    
    public User getUserA() {
        return userA;
    }
    
    public void setUserA(User userA) {
        this.userA = userA;
    }
    
    public User getUserB() {
        return userB;
    }
    
    public void setUserB(User userB) {
        this.userB = userB;
    }
    
    public FriendshipStatus getStatus() {
        return status;
    }
    
    public void setStatus(FriendshipStatus status) {
        this.status = status;
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
    
    public BigDecimal getCurrentNetBalance() {
        return currentNetBalance;
    }
    
    public void setCurrentNetBalance(BigDecimal currentNetBalance) {
        this.currentNetBalance = currentNetBalance;
    }
    
    public LocalDateTime getLastTransactionDate() {
        return lastTransactionDate;
    }
    
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }
}

