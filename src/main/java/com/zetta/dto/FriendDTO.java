package com.zetta.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO representing a friend with their current net balance.
 */
public class FriendDTO {
    
    private UUID friendId;
    private String username;
    private String email;
    private BigDecimal currentNetBalance; // Positive = user owes friend, Negative = friend owes user
    private UUID friendshipId;
    
    public FriendDTO() {
    }
    
    public FriendDTO(UUID friendId, String username, String email, BigDecimal currentNetBalance, UUID friendshipId) {
        this.friendId = friendId;
        this.username = username;
        this.email = email;
        this.currentNetBalance = currentNetBalance;
        this.friendshipId = friendshipId;
    }
    
    // Getters and Setters
    public UUID getFriendId() {
        return friendId;
    }
    
    public void setFriendId(UUID friendId) {
        this.friendId = friendId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public BigDecimal getCurrentNetBalance() {
        return currentNetBalance;
    }
    
    public void setCurrentNetBalance(BigDecimal currentNetBalance) {
        this.currentNetBalance = currentNetBalance;
    }
    
    public UUID getFriendshipId() {
        return friendshipId;
    }
    
    public void setFriendshipId(UUID friendshipId) {
        this.friendshipId = friendshipId;
    }
}

