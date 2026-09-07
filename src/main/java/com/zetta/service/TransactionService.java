package com.zetta.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zetta.dto.TransactionResponseDTO;
import com.zetta.entity.Transaction;
import com.zetta.entity.TransactionLifecycleState;
import com.zetta.entity.User;
import com.zetta.repository.TransactionRepository;
import com.zetta.repository.UserRepository;

/**
 * Service handling transaction business logic including dual-confirmation workflow.
 * Implements the Smart Request Engine from the PRD.
 */
@Service
@Transactional
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final FriendshipService friendshipService;
    private final UserRepository userRepository;
    
    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              FriendshipService friendshipService,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.friendshipService = friendshipService;
        this.userRepository = userRepository;
    }
    
    /**
     * Creates a new transaction request.
     * Sets initial state to PENDING with both confirmations as false.
     * Updates the friendship net balance for quick dashboard rendering.
     */
    public Transaction createTransaction(UUID creatorId, UUID targetFriendId, 
                                       java.math.BigDecimal amount, String title) {
        Transaction transaction = new Transaction();
        transaction.setCreatorId(creatorId);
        transaction.setTargetFriendId(targetFriendId);
        transaction.setAmount(amount);
        transaction.setTitle(title);
        transaction.setLifecycleState(TransactionLifecycleState.PENDING);
        transaction.setIsSenderConfirmed(false);
        transaction.setIsReceiverConfirmed(false);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Update friendship net balance for quick dashboard rendering
        friendshipService.updateNetBalance(creatorId, targetFriendId);
        
        return savedTransaction;
    }
    
    /**
     * Gets all transactions for a user (both as creator and as target).
     */
    public List<Transaction> getAllTransactionsForUser(UUID userId) {
        List<Transaction> asCreator = transactionRepository.findByCreatorId(userId);
        List<Transaction> asTarget = transactionRepository.findByTargetFriendId(userId);
        
        // Combine and return (could be optimized with a single query)
        java.util.ArrayList<Transaction> allTransactions = new java.util.ArrayList<>(asCreator);
        allTransactions.addAll(asTarget);
        
        return allTransactions;
    }
    
    /**
     * Gets all transactions for a user ordered by creation date (newest first).
     */
    public List<Transaction> getTransactionsForUserOrdered(UUID userId) {
        List<Transaction> allTransactions = getAllTransactionsForUser(userId);
        return allTransactions.stream()
            .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
            .toList();
    }
    
    /**
     * Confirms the sender side (creator) of the transaction.
     * Updates lifecycle state to COMPLETED if both sides are confirmed.
     */
    public Transaction confirmSender(UUID requestId, UUID userId) {
        Transaction transaction = transactionRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        // Verify user is the creator
        if (!transaction.getCreatorId().equals(userId)) {
            throw new RuntimeException("User is not authorized to confirm as sender");
        }
        
        transaction.setIsSenderConfirmed(true);
        updateLifecycleState(transaction);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Confirms the receiver side (target friend) of the transaction.
     * Updates lifecycle state to COMPLETED if both sides are confirmed.
     */
    public Transaction confirmReceiver(UUID requestId, UUID userId) {
        Transaction transaction = transactionRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        // Verify user is the target friend
        if (transaction.getTargetFriendId() == null || 
            !transaction.getTargetFriendId().equals(userId)) {
            throw new RuntimeException("User is not authorized to confirm as receiver");
        }
        
        transaction.setIsReceiverConfirmed(true);
        updateLifecycleState(transaction);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Updates the lifecycle state based on dual-confirmation status.
     * Sets to COMPLETED only when both sender and receiver have confirmed.
     */
    private void updateLifecycleState(Transaction transaction) {
        Boolean senderConfirmed = transaction.getIsSenderConfirmed();
        Boolean receiverConfirmed = transaction.getIsReceiverConfirmed();
        
        if (Boolean.TRUE.equals(senderConfirmed) && Boolean.TRUE.equals(receiverConfirmed)) {
            // Both confirmed - transaction is completed
            transaction.setLifecycleState(TransactionLifecycleState.COMPLETED);
        } else if (Boolean.TRUE.equals(senderConfirmed) || Boolean.TRUE.equals(receiverConfirmed)) {
            // One side confirmed - still pending (could be PENDING or a new state)
            // Keeping as PENDING per PRD requirements
            transaction.setLifecycleState(TransactionLifecycleState.PENDING);
        } else {
            // Neither confirmed - definitely pending
            transaction.setLifecycleState(TransactionLifecycleState.PENDING);
        }
    }
    
    /**
     * Gets a transaction by ID.
     */
    public Transaction getTransactionById(UUID requestId) {
        return transactionRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
    
    /**
     * Gets all pending transactions for a user.
     */
    public List<Transaction> getPendingTransactionsForUser(UUID userId) {
        List<Transaction> allTransactions = getAllTransactionsForUser(userId);
        return allTransactions.stream()
            .filter(t -> t.getLifecycleState() == TransactionLifecycleState.PENDING)
            .toList();
    }
    
    /**
     * Bulk settlement of transactions - Single Click Settlement feature.
     * For each transaction:
     * - If userId matches creatorId, set isSenderConfirmed = true
     * - If userId matches targetFriendId, set isReceiverConfirmed = true
     * - If BOTH confirmations are true, set lifecycleState to COMPLETED
     * 
     * After processing, updates the net balance for the friendship.
     * 
     * @param requestIds List of transaction IDs to settle
     * @param userId The user performing the settlement
     * @return List of updated transactions
     */
    public List<Transaction> settleBulk(List<UUID> requestIds, UUID userId) {
        if (requestIds == null || requestIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        java.util.List<Transaction> updatedTransactions = new java.util.ArrayList<>();
        java.util.Set<UUID> friendIds = new java.util.HashSet<>();
        
        for (UUID requestId : requestIds) {
            Transaction transaction = transactionRepository.findById(requestId)
                .orElse(null);
            
            if (transaction == null) {
                // Skip transactions that don't exist
                continue;
            }
            
            // Check if user is the creator (sender)
            if (transaction.getCreatorId().equals(userId)) {
                transaction.setIsSenderConfirmed(true);
                // Track the friend (target) for net balance update
                if (transaction.getTargetFriendId() != null) {
                    friendIds.add(transaction.getTargetFriendId());
                }
            }
            
            // Check if user is the target friend (receiver)
            if (transaction.getTargetFriendId() != null && 
                transaction.getTargetFriendId().equals(userId)) {
                transaction.setIsReceiverConfirmed(true);
                // Track the friend (creator) for net balance update
                friendIds.add(transaction.getCreatorId());
            }
            
            // Update lifecycle state based on dual-confirmation
            updateLifecycleState(transaction);
            
            Transaction savedTransaction = transactionRepository.save(transaction);
            updatedTransactions.add(savedTransaction);
        }
        
        // After processing all transactions, update net balance for each friend involved
        for (UUID friendId : friendIds) {
            try {
                friendshipService.updateNetBalance(userId, friendId);
            } catch (RuntimeException e) {
                // Log but don't fail if friendship update fails
                System.err.println("Warning: Could not update net balance for friendship between " 
                    + userId + " and " + friendId + ": " + e.getMessage());
            }
        }
        
        return updatedTransactions;
    }
    
    /**
     * Gets all transactions for a user with user names included.
     * Returns DTOs with creatorName and targetFriendName populated.
     */
    public List<TransactionResponseDTO> getTransactionsWithNames(UUID userId) {
        List<Transaction> transactions = getTransactionsForUserOrdered(userId);
        
        // Collect all unique user IDs
        Set<UUID> userIds = transactions.stream()
            .flatMap(t -> java.util.stream.Stream.of(t.getCreatorId(), t.getTargetFriendId()))
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        
        // Fetch all users in one query
        Map<UUID, String> userNameMap = userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, user -> user.getUsername() != null ? user.getUsername() : "Unknown"));
        
        // Convert to DTOs with names
        return transactions.stream()
            .map(t -> new TransactionResponseDTO(
                t,
                userNameMap.getOrDefault(t.getCreatorId(), "Unknown"),
                userNameMap.getOrDefault(t.getTargetFriendId(), "Unknown")
            ))
            .toList();
    }
    
    /**
     * Converts a single transaction to DTO with user names.
     */
    public TransactionResponseDTO toResponseDTO(Transaction transaction) {
        String creatorName = userRepository.findById(transaction.getCreatorId())
            .map(User::getUsername)
            .orElse("Unknown");
        String targetFriendName = transaction.getTargetFriendId() != null
            ? userRepository.findById(transaction.getTargetFriendId())
                .map(User::getUsername)
                .orElse("Unknown")
            : "Unknown";
        return new TransactionResponseDTO(transaction, creatorName, targetFriendName);
    }
}

