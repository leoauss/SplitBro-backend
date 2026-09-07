package com.zetta.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zetta.dto.FriendDTO;
import com.zetta.dto.FriendshipRequestDTO;
import com.zetta.entity.Friendship;
import com.zetta.entity.FriendshipStatus;
import com.zetta.entity.User;
import com.zetta.repository.UserRepository;
import com.zetta.service.FriendshipService;
import com.zetta.service.NettingService;

import jakarta.validation.Valid;

/**
 * Controller for handling friend (friendship) operations.
 */
@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private static final Logger logger = LoggerFactory.getLogger(FriendController.class);
    
    private final FriendshipService friendshipService;
    private final NettingService nettingService;
    private final UserRepository userRepository;
    
    @Autowired
    public FriendController(FriendshipService friendshipService, 
                           NettingService nettingService,
                           UserRepository userRepository) {
        this.friendshipService = friendshipService;
        this.nettingService = nettingService;
        this.userRepository = userRepository;
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
     * POST endpoint to create a new friend connection.
     * Uses the authenticated user's ID from JWT as the requester.
     * 
     * @param requestDTO Friendship request data (only friendId needed)
     * @return Created friendship
     */
    @PostMapping
    public ResponseEntity<Friendship> createFriendship(@Valid @RequestBody FriendshipRequestDTO requestDTO) {
        try {
            UUID currentUserId = getCurrentUserId();
            logger.debug("Creating friendship - User: {}, Friend: {}", currentUserId, requestDTO.getFriendId());
            Friendship friendship = friendshipService.createFriendship(
                currentUserId,
                requestDTO.getFriendId(),
                FriendshipStatus.PENDING
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(friendship);
        } catch (RuntimeException e) {
            logger.warn("Friendship creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    /**
     * GET endpoint to retrieve all friends for the authenticated user with their current net balances.
     * User ID is extracted from JWT token.
     * 
     * @return List of friends with net balances
     */
    @GetMapping
    public ResponseEntity<List<FriendDTO>> getFriends() {
        UUID userId = getCurrentUserId();
        logger.debug("getFriends called for User ID: {}", userId);
        
        // Get all active friendships for the user
        List<Friendship> friendships = friendshipService.getActiveFriendshipsByUserId(userId);
        logger.debug("Found {} active friendships", friendships.size());
        
        List<FriendDTO> friendDTOs = new ArrayList<>();
        
        for (Friendship friendship : friendships) {
            // Determine which user is the friend (not the current user)
            UUID friendId;
            if (friendship.getUserAId().equals(userId)) {
                friendId = friendship.getUserBId();
            } else {
                friendId = friendship.getUserAId();
            }
            
            // Get friend user details
            User friend = userRepository.findById(friendId)
                .orElse(null);
            
            if (friend != null) {
                logger.debug("Found friend: {} (ID: {})", friend.getUsername(), friendId);
                // Calculate current net balance
                // Positive = user owes friend, Negative = friend owes user
                com.zetta.dto.NetDifferenceDTO netDifference = nettingService.calculateNetDifference(userId, friendId);
                
                BigDecimal currentNetBalance;
                if (netDifference.getMasterSum().compareTo(BigDecimal.ZERO) == 0) {
                    currentNetBalance = BigDecimal.ZERO;
                } else if (netDifference.getOwesUserId() != null && netDifference.getOwesUserId().equals(userId)) {
                    // User owes friend - positive balance
                    currentNetBalance = netDifference.getMasterSum();
                } else {
                    // Friend owes user - negative balance (user is owed money)
                    currentNetBalance = netDifference.getMasterSum().negate();
                }
                
                FriendDTO friendDTO = new FriendDTO(
                    friendId,
                    friend.getUsername(),
                    friend.getEmail(),
                    currentNetBalance,
                    friendship.getConnectionId()
                );
                
                friendDTOs.add(friendDTO);
            }
        }
        
        logger.debug("Returning {} friends", friendDTOs.size());
        return ResponseEntity.ok(friendDTOs);
    }
    
    /**
     * GET endpoint to retrieve a specific friendship between two users.
     * Uses authenticated user as one of the parties.
     * 
     * @return Friendship relationship
     */
    @GetMapping("/relationship")
    public ResponseEntity<Friendship> getFriendship() {
        // For relationship lookup, use authenticated user
        // This endpoint may need additional parameters in the future
        return ResponseEntity.notFound().build();
    }
}

