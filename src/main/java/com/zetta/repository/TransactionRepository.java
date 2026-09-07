package com.zetta.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zetta.entity.Transaction;
import com.zetta.entity.TransactionLifecycleState;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    List<Transaction> findByCreatorId(UUID creatorId);
    
    List<Transaction> findByCreatorIdAndLifecycleState(UUID creatorId, TransactionLifecycleState lifecycleState);
    
    List<Transaction> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId);
    
    /**
     * Find all transactions between two specific users.
     * This includes transactions where user1 is the creator and user2 is the target friend,
     * or vice versa.
     */
    @Query("SELECT t FROM Transaction t WHERE " +
           "((t.creatorId = :userId1 AND t.targetFriendId = :userId2) OR " +
           "(t.creatorId = :userId2 AND t.targetFriendId = :userId1))")
    List<Transaction> findTransactionsBetweenUsers(@Param("userId1") UUID userId1, 
                                                    @Param("userId2") UUID userId2);
    
    /**
     * Find all ACTIVE transactions between two specific users.
     * Checks both directions and excludes COMPLETED, CANCELLED, FAILED, and REJECTED transactions.
     * Only returns PENDING transactions that should be counted for netting.
     */
    @Query("SELECT t FROM Transaction t WHERE " +
           "((t.creatorId = :userId1 AND t.targetFriendId = :userId2) OR " +
           "(t.creatorId = :userId2 AND t.targetFriendId = :userId1)) " +
           "AND t.lifecycleState = com.zetta.entity.TransactionLifecycleState.PENDING")
    List<Transaction> findActiveTransactionsBetweenUsers(@Param("userId1") UUID userId1, 
                                                          @Param("userId2") UUID userId2);
    
    /**
     * Find transactions where the user is the creator and related to a specific friend.
     */
    List<Transaction> findByCreatorIdAndTargetFriendId(UUID creatorId, UUID targetFriendId);
    
    /**
     * Find transactions where the user is the target friend (reverse relationship).
     */
    List<Transaction> findByTargetFriendId(UUID targetFriendId);
    
    /**
     * Find transactions by lifecycle state.
     */
    List<Transaction> findByLifecycleState(TransactionLifecycleState lifecycleState);
}

