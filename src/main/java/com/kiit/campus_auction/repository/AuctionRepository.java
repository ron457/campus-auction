package com.kiit.campus_auction.repository;

import com.kiit.campus_auction.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    
    // ✅ All use String, not enum
    List<Auction> findByStatus(String status);
    
    List<Auction> findByCategoryAndStatus(String category, String status);
    
    List<Auction> findBySellerId(Long sellerId);
    
    List<Auction> findBySellerEmail(String sellerEmail);
    
    List<Auction> findByCategory(String category);
    
    @Query("SELECT a FROM Auction a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Auction> searchAuctions(@Param("keyword") String keyword);
    
    List<Auction> findByStatusAndEndTimeBefore(String status, LocalDateTime endTime);
    
    @Query("SELECT COUNT(a) FROM Auction a WHERE a.seller.id = :sellerId")
    Long countAuctionsBySeller(@Param("sellerId") Long sellerId);
}
