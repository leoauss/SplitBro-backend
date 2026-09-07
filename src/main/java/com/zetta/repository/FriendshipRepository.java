package com.zetta.repository;

import com.zetta.entity.Friendship;
import com.zetta.entity.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
    
    /**
     * Find friendship between two users.
     * Since user_a_id is always the smaller UUID, we need to check both combinations.
     */
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.userAId = :userId1 AND f.userBId = :userId2) OR " +
           "(f.userAId = :userId2 AND f.userBId = :userId1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("userId1") UUID userId1, 
                                                     @Param("userId2") UUID userId2);
    
    /**
     * Find all friendships for a user (as user A or user B).
     */
    @Query("SELECT f FROM Friendship f WHERE f.userAId = :userId OR f.userBId = :userId")
    List<Friendship> findAllFriendshipsByUserId(@Param("userId") UUID userId);
    
    /**
     * Find all friendships for a user with a specific status.
     */
    @Query("SELECT f FROM Friendship f WHERE (f.userAId = :userId OR f.userBId = :userId) AND f.status = :status")
    List<Friendship> findFriendshipsByUserIdAndStatus(@Param("userId") UUID userId, 
                                                       @Param("status") FriendshipStatus status);
    
    /**
     * Find all friendships where user is user A.
     */
    List<Friendship> findByUserAId(UUID userAId);
    
    /**
     * Find all friendships where user is user B.
     */
    List<Friendship> findByUserBId(UUID userBId);
    
    /**
     * Find all friendships where user is user A with a specific status.
     */
    List<Friendship> findByUserAIdAndStatus(UUID userAId, FriendshipStatus status);
    
    /**
     * Find all friendships where user is user B with a specific status.
     */
    List<Friendship> findByUserBIdAndStatus(UUID userBId, FriendshipStatus status);
}

