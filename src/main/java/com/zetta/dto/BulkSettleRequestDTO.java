package com.zetta.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for the Single Click Settlement feature.
 * Contains the list of transaction IDs to settle and the user performing the settlement.
 */
public class BulkSettleRequestDTO {
    
    @NotEmpty(message = "Request IDs list cannot be empty")
    private List<UUID> requestIds;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    public BulkSettleRequestDTO() {
    }
    
    public BulkSettleRequestDTO(List<UUID> requestIds, UUID userId) {
        this.requestIds = requestIds;
        this.userId = userId;
    }
    
    public List<UUID> getRequestIds() {
        return requestIds;
    }
    
    public void setRequestIds(List<UUID> requestIds) {
        this.requestIds = requestIds;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
