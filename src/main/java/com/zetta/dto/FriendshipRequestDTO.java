package com.zetta.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new friendship connection.
 * Note: userId is optional since it's extracted from the JWT token.
 */
public class FriendshipRequestDTO {
    
    // userId is optional - the server extracts it from the JWT token
    @JsonProperty("user_id")
    private UUID userId;
    
    @NotNull(message = "Friend ID is required")
    @JsonProperty("friend_id") // This forces Postman's "friend_id" to map to this field
    private UUID friendId;
    
    public FriendshipRequestDTO() {
    }
    
    public FriendshipRequestDTO(UUID userId, UUID friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }
    
    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public UUID getFriendId() {
        return friendId;
    }
    
    public void setFriendId(UUID friendId) {
        this.friendId = friendId;
    }
}