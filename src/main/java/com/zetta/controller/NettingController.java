package com.zetta.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zetta.dto.NetDifferenceDTO;
import com.zetta.service.NettingService;

/**
 * Controller for handling netting calculations between users.
 */
@RestController
@RequestMapping("/api/netting")
public class NettingController {

    private static final Logger logger = LoggerFactory.getLogger(NettingController.class);
    
    private final NettingService nettingService;
    
    @Autowired
    public NettingController(NettingService nettingService) {
        this.nettingService = nettingService;
    }
    
    /**
     * Extracts the current user's ID from the JWT token in SecurityContext.
     * The 'sub' claim in Supabase JWT contains the user's UUID.
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // 'sub' claim from JWT
        logger.debug("Extracted user ID from JWT: {}", userId);
        return UUID.fromString(userId);
    }
    
    /**
     * GET endpoint to calculate net difference between the authenticated user and a friend.
     * User ID is extracted from JWT token.
     * 
     * Usage: GET /api/netting/{friendId}
     * 
     * @param friendId The ID of the friend to calculate netting with
     * @return NetDifferenceDTO containing the master sum and who owes whom
     */
    @GetMapping("/{friendId}")
    public ResponseEntity<NetDifferenceDTO> getNetting(@PathVariable UUID friendId) {
        UUID currentUserId = getCurrentUserId();
        NetDifferenceDTO result = nettingService.calculateNetDifference(currentUserId, friendId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Alternative endpoint that takes both user IDs explicitly.
     * Kept for admin/testing purposes.
     * 
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return NetDifferenceDTO containing the master sum and who owes whom
     */
    @GetMapping("/between")
    public ResponseEntity<NetDifferenceDTO> getNettingBetween(
            @RequestParam UUID userId1,
            @RequestParam UUID userId2) {
        
        NetDifferenceDTO result = nettingService.calculateNetDifference(userId1, userId2);
        return ResponseEntity.ok(result);
    }
}

