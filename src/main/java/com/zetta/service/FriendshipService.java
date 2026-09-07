package com.zetta.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zetta.entity.Friendship;
import com.zetta.entity.FriendshipStatus;
import com.zetta.repository.FriendshipRepository;

@Service
@Transactional
public class FriendshipService {
    
    private final FriendshipRepository friendshipRepository;
    private final NettingService nettingService;
    
    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository, NettingService nettingService) {
        this.friendshipRepository = friendshipRepository;
        this.nettingService = nettingService;
    }
    
    /**
     * Creates a new friendship ensuring user_a_id is always the smaller UUID.
     * This method handles the ID sorting logic before saving to satisfy the database constraint.
     * 
     * @param userId1 First user ID (can be in any order)
     * @param userId2 Second user ID (can be in any order)
     * @param status Friendship status
     * @return Created friendship with properly sorted IDs
     */
    public Friendship createFriendship(UUID userId1, UUID userId2, FriendshipStatus status) {
        // Sort IDs first: smaller UUID goes to userA, larger to userB
        // This satisfies the database constraint: user_a_id < user_b_id
        // Use string comparison to match PostgreSQL's lexicographic UUID ordering
        UUID userA = userId1.toString().compareTo(userId2.toString()) < 0 ? userId1 : userId2;
        UUID userB = userId1.toString().compareTo(userId2.toString()) < 0 ? userId2 : userId1;
        
        // Check if friendship already exists using sorted IDs
        Optional<Friendship> existing = friendshipRepository.findFriendshipBetweenUsers(userA, userB);
        if (existing.isPresent()) {
            throw new RuntimeException("Friendship already exists between these users");
        }
        
        // Initialize friendship with sorted IDs
        // Auto-activate friendship for minimalist UX - no approval needed
        Friendship friendship = new Friendship();
        friendship.setUserAId(userA);
        friendship.setUserBId(userB);
        friendship.setStatus(FriendshipStatus.ACCEPTED);  // Use ACCEPTED to match the query filter
        friendship.setCurrentNetBalance(BigDecimal.ZERO);
        return friendshipRepository.save(friendship);
    }
    
    /**
     * Saves a friendship ensuring ID sorting is maintained.
     */
    public Friendship save(Friendship friendship) {
        // Ensure ID sorting before saving
        if (friendship.getUserAId() != null && friendship.getUserBId() != null) {
            friendship.setUserIds(friendship.getUserAId(), friendship.getUserBId());
        }
        return friendshipRepository.save(friendship);
    }
    
    /**
     * Finds friendship between two users.
     */
    public Optional<Friendship> findFriendshipBetweenUsers(UUID userId1, UUID userId2) {
        return friendshipRepository.findFriendshipBetweenUsers(userId1, userId2);
    }
    
    /**
     * Gets all friendships for a user.
     */
    public List<Friendship> getAllFriendshipsByUserId(UUID userId) {
        return friendshipRepository.findAllFriendshipsByUserId(userId);
    }
    
    /**
     * Gets all active friendships for a user.
     */
    public List<Friendship> getActiveFriendshipsByUserId(UUID userId) {
        return friendshipRepository.findFriendshipsByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED);
    }
    
    /**
     * Gets all friendships for a user with a specific status.
     */
    public List<Friendship> getFriendshipsByUserIdAndStatus(UUID userId, FriendshipStatus status) {
        return friendshipRepository.findFriendshipsByUserIdAndStatus(userId, status);
    }
    
    /**
     * Updates friendship status.
     */
    public Friendship updateFriendshipStatus(UUID connectionId, FriendshipStatus newStatus) {
        Friendship friendship = friendshipRepository.findById(connectionId)
            .orElseThrow(() -> new RuntimeException("Friendship not found"));
        friendship.setStatus(newStatus);
        return friendshipRepository.save(friendship);
    }
    
    /**
     * Updates the net balance for a friendship by recalculating from transactions.
     */
    public Friendship updateNetBalance(UUID userId1, UUID userId2) {
        Optional<Friendship> friendshipOpt = friendshipRepository.findFriendshipBetweenUsers(userId1, userId2);
        if (friendshipOpt.isEmpty()) {
            throw new RuntimeException("Friendship not found");
        }
        
        Friendship friendship = friendshipOpt.get();
        
        // Calculate net difference using NettingService
        com.zetta.dto.NetDifferenceDTO netDifference = nettingService.calculateNetDifference(userId1, userId2);
        
        // Determine the net balance from the perspective of user1
        // If user1 owes user2, balance is positive (user1 owes)
        // If user2 owes user1, balance is negative (user1 is owed)
        BigDecimal netBalance;
        if (netDifference.getMasterSum().compareTo(BigDecimal.ZERO) == 0) {
            netBalance = BigDecimal.ZERO;
        } else if (netDifference.getOwesUserId() != null && netDifference.getOwesUserId().equals(userId1)) {
            // User1 owes user2
            netBalance = netDifference.getMasterSum();
        } else {
            // User2 owes user1 (negative from user1's perspective)
            netBalance = netDifference.getMasterSum().negate();
        }
        
        friendship.setCurrentNetBalance(netBalance);
        friendship.setLastTransactionDate(LocalDateTime.now());
        
        return friendshipRepository.save(friendship);
    }
}

