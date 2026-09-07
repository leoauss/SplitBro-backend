package com.zetta.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zetta.dto.DashboardSummaryDTO;
import com.zetta.entity.Transaction;
import com.zetta.entity.TransactionLifecycleState;
import com.zetta.repository.TransactionRepository;
import com.zetta.service.FriendshipService;
import com.zetta.service.NettingService;

/**
 * Controller for handling dashboard summary operations.
 * Provides high-level financial overview for the user.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    private final NettingService nettingService;
    private final FriendshipService friendshipService;
    private final TransactionRepository transactionRepository;
    
    @Autowired
    public DashboardController(NettingService nettingService,
                               FriendshipService friendshipService,
                               TransactionRepository transactionRepository) {
        this.nettingService = nettingService;
        this.friendshipService = friendshipService;
        this.transactionRepository = transactionRepository;
    }
    
    /**
     * Extracts the current user's ID from the JWT token in SecurityContext.
     * The 'sub' claim in Supabase JWT contains the user's UUID.
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // 'sub' claim from JWT
        logger.debug("Extracted User ID: {}", userId);
        return UUID.fromString(userId);
    }
    
    /**
     * GET endpoint to retrieve dashboard summary with total to pay and total to receive.
     * User ID is extracted from JWT token.
     * 
     * @return Dashboard summary DTO
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        UUID userId = getCurrentUserId();
        
        // Get all active friendships for the user
        List<com.zetta.entity.Friendship> friendships = friendshipService.getActiveFriendshipsByUserId(userId);
        
        BigDecimal totalToPay = BigDecimal.ZERO;
        BigDecimal totalToReceive = BigDecimal.ZERO;
        
        // Calculate totals by summing net balances with each friend
        for (com.zetta.entity.Friendship friendship : friendships) {
            UUID friendId;
            if (friendship.getUserAId().equals(userId)) {
                friendId = friendship.getUserBId();
            } else {
                friendId = friendship.getUserAId();
            }
            
            // Calculate net difference with this friend
            com.zetta.dto.NetDifferenceDTO netDifference = nettingService.calculateNetDifference(userId, friendId);
            
            if (netDifference.getMasterSum().compareTo(BigDecimal.ZERO) > 0) {
                if (netDifference.getOwesUserId() != null && netDifference.getOwesUserId().equals(userId)) {
                    // User owes this friend
                    totalToPay = totalToPay.add(netDifference.getMasterSum());
                } else {
                    // Friend owes user
                    totalToReceive = totalToReceive.add(netDifference.getMasterSum());
                }
            }
        }
        
        // Count pending transactions
        List<Transaction> pendingTransactions = transactionRepository.findByCreatorId(userId).stream()
            .filter(t -> t.getLifecycleState() == TransactionLifecycleState.PENDING)
            .toList();
        
        List<Transaction> pendingAsTarget = transactionRepository.findByTargetFriendId(userId).stream()
            .filter(t -> t.getLifecycleState() == TransactionLifecycleState.PENDING)
            .toList();
        
        int pendingCount = pendingTransactions.size() + pendingAsTarget.size();
        
        DashboardSummaryDTO summary = new DashboardSummaryDTO(userId, totalToPay, totalToReceive);
        summary.setActiveFriendsCount(friendships.size());
        summary.setPendingTransactionsCount(pendingCount);
        
        return ResponseEntity.ok(summary);
    }
}

