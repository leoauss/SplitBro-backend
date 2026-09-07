package com.zetta.controller;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zetta.dto.TransactionRequestDTO;
import com.zetta.dto.TransactionResponseDTO;
import com.zetta.entity.Transaction;
import com.zetta.service.TransactionService;

import jakarta.validation.Valid;

/**
 * Controller for handling transaction (request) operations.
 * Implements the Smart Request Engine endpoints from the PRD.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    
    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
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
     * POST endpoint to create a new transaction request.
     * Creator ID is extracted from JWT token.
     * 
     * @param requestDTO Transaction request data (targetFriendId, amount, title)
     * @return Created transaction with user names
     */
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO requestDTO) {
        UUID creatorId = getCurrentUserId();
        Transaction transaction = transactionService.createTransaction(
            creatorId,
            requestDTO.getTargetFriendId(),
            requestDTO.getAmount(),
            requestDTO.getTitle()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.toResponseDTO(transaction));
    }
    
    /**
     * GET endpoint to retrieve all transactions for the authenticated user.
     * User ID is extracted from JWT token.
     * 
     * @return List of transactions with user names
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions() {
        UUID userId = getCurrentUserId();
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsWithNames(userId);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * GET endpoint to retrieve a specific transaction by ID.
     * 
     * @param requestId The transaction request ID
     * @return Transaction
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable UUID requestId) {
        Transaction transaction = transactionService.getTransactionById(requestId);
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * PATCH endpoint to confirm the sender (creator) side of the transaction.
     * Updates is_sender_confirmed to true and lifecycle_state to COMPLETED if both sides confirm.
     * User ID is extracted from JWT token.
     * 
     * @param requestId The transaction request ID
     * @return Updated transaction
     */
    @PatchMapping("/{requestId}/confirm-sender")
    public ResponseEntity<Transaction> confirmSender(@PathVariable UUID requestId) {
        try {
            UUID userId = getCurrentUserId();
            Transaction transaction = transactionService.confirmSender(requestId, userId);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    /**
     * PATCH endpoint to confirm the receiver (target friend) side of the transaction.
     * Updates is_receiver_confirmed to true and lifecycle_state to COMPLETED if both sides confirm.
     * User ID is extracted from JWT token.
     * 
     * @param requestId The transaction request ID
     * @return Updated transaction
     */
    @PatchMapping("/{requestId}/confirm-receiver")
    public ResponseEntity<Transaction> confirmReceiver(@PathVariable UUID requestId) {
        try {
            UUID userId = getCurrentUserId();
            Transaction transaction = transactionService.confirmReceiver(requestId, userId);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    /**
     * GET endpoint to retrieve pending transactions for the authenticated user.
     * User ID is extracted from JWT token.
     * 
     * @return List of pending transactions
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Transaction>> getPendingTransactions() {
        UUID userId = getCurrentUserId();
        List<Transaction> transactions = transactionService.getPendingTransactionsForUser(userId);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * PATCH endpoint for Single Click Settlement feature.
     * Bulk settles multiple transactions at once.
     * User ID is extracted from JWT token.
     * 
     * For each transaction:
     * - If userId matches creatorId, confirms the sender side
     * - If userId matches targetFriendId, confirms the receiver side
     * - If both sides are confirmed, marks transaction as COMPLETED
     * 
     * After processing, updates the net balance for the friendship.
     * 
     * @param bulkSettleRequest DTO containing the list of transaction IDs
     * @return List of updated transactions
     */
    @PatchMapping("/settle-bulk")
    public ResponseEntity<List<Transaction>> settleBulk(
            @Valid @RequestBody com.zetta.dto.BulkSettleRequestDTO bulkSettleRequest) {
        try {
            UUID userId = getCurrentUserId();
            List<Transaction> updatedTransactions = transactionService.settleBulk(
                bulkSettleRequest.getRequestIds(),
                userId
            );
            return ResponseEntity.ok(updatedTransactions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}

