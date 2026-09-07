package com.zetta.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zetta.dto.NetDifferenceDTO;
import com.zetta.entity.Transaction;
import com.zetta.repository.TransactionRepository;

/**
 * Service implementing the Algebraic Smart Netting System.
 * Calculates the net difference between two users by summing all
 * completed transactions between them.
 * 
 * The netting algorithm:
 * - If user1 is the creator and user2 is the target: user1 owes user2 (negative for user1)
 * - If user2 is the creator and user1 is the target: user2 owes user1 (positive for user1)
 */
@Service
@Transactional
public class NettingService {

    private static final Logger logger = LoggerFactory.getLogger(NettingService.class);
    
    private final TransactionRepository transactionRepository;
    
    @Autowired
    public NettingService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    /**
     * Calculates the net difference between two users.
     * 
     * The netting algorithm:
     * 1. Finds all ACTIVE (PENDING) transactions between user1 and user2
     * 2. Excludes COMPLETED, CANCELLED, FAILED, REJECTED transactions
     * 3. For each transaction:
     *    - If user1 is creator and user2 is target: user1 owes user2 (subtract amount)
     *    - If user2 is creator and user1 is target: user2 owes user1 (add amount)
     * 4. Sums all amounts to get the master sum
     * 5. If master sum > 0: user1 owes user2
     *    If master sum < 0: user2 owes user1
     *    If master sum = 0: no outstanding balance
     * 
     * @param userId1 The first user ID
     * @param userId2 The second user ID (friend)
     * @return NetDifferenceDTO containing the master sum and who owes whom
     */
    public NetDifferenceDTO calculateNetDifference(UUID userId1, UUID userId2) {
        // Get all ACTIVE (PENDING) transactions between the two users
        // This excludes COMPLETED, CANCELLED, FAILED, REJECTED transactions
        List<Transaction> activeTransactions = transactionRepository.findActiveTransactionsBetweenUsers(userId1, userId2);
        
        logger.debug("Found {} active transactions between {} and {}", activeTransactions.size(), userId1, userId2);
        
        BigDecimal masterSum = BigDecimal.ZERO;
        
        // Calculate the net sum
        for (Transaction transaction : activeTransactions) {
            BigDecimal amount = transaction.getAmount();
            
            if (transaction.getCreatorId().equals(userId1) && 
                transaction.getTargetFriendId() != null && 
                transaction.getTargetFriendId().equals(userId2)) {
                // User1 created transaction with user2 as target: user1 owes user2 (negative for user1)
                masterSum = masterSum.subtract(amount);
            } else if (transaction.getCreatorId().equals(userId2) && 
                       transaction.getTargetFriendId() != null && 
                       transaction.getTargetFriendId().equals(userId1)) {
                // User2 created transaction with user1 as target: user2 owes user1 (positive for user1)
                masterSum = masterSum.add(amount);
            }
        }
        
        // Collect IDs of all active transactions for Single Click Settlement
        List<UUID> activeTransactionIds = activeTransactions.stream()
            .map(Transaction::getRequestId)
            .toList();
        
        // Determine who owes whom
        UUID owesUserId;
        UUID owedToUserId;
        
        if (masterSum.compareTo(BigDecimal.ZERO) > 0) {
            // Positive sum means user1 owes user2
            owesUserId = userId1;
            owedToUserId = userId2;
        } else if (masterSum.compareTo(BigDecimal.ZERO) < 0) {
            // Negative sum means user2 owes user1 (flip the sign)
            owesUserId = userId2;
            owedToUserId = userId1;
            masterSum = masterSum.abs(); // Make it positive for clarity
        } else {
            // Zero balance
            owesUserId = null;
            owedToUserId = null;
        }
        
        return new NetDifferenceDTO(userId1, userId2, masterSum, owesUserId, owedToUserId, activeTransactionIds);
    }
}

