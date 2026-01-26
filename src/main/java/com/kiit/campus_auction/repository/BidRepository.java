package com.kiit.campus_auction.repository;

import com.kiit.campus_auction.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    
    // Find all bids for an auction
    List<Bid> findByAuctionId(Long auctionId);
    
    // Find all bids by a user
    List<Bid> findByBidderId(Long bidderId);
    
    // Find current winning bid for an auction
    Optional<Bid> findByAuctionIdAndIsWinningTrue(Long auctionId);
    
    // ✅ NEW: Find highest bid for an auction (by amount)
    Optional<Bid> findFirstByAuctionIdOrderByAmountDesc(Long auctionId);
    
    // Find highest bid amount for an auction
    @Query("SELECT MAX(b.amount) FROM Bid b WHERE b.auction.id = ?1")
    Double findMaxBidAmount(Long auctionId);
    
    // Count bids on an auction
    long countByAuctionId(Long auctionId);
    
    // Get all bids ordered by amount (for leaderboard)
    List<Bid> findByAuctionIdOrderByAmountDesc(Long auctionId);
}
