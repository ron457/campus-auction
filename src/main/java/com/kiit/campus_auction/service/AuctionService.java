package com.kiit.campus_auction.service;

import com.kiit.campus_auction.model.Auction;
import com.kiit.campus_auction.model.Bid;
import com.kiit.campus_auction.repository.AuctionRepository;
import com.kiit.campus_auction.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuctionService {
    
    @Autowired
    private AuctionRepository auctionRepository;
    
    @Autowired
    private BidRepository bidRepository;
    
    @Autowired
    private UserService userService;
    
    // Create new auction
    public Auction createAuction(Auction auction) {
        if (auction.getStartingPrice() <= 0) {
            throw new IllegalArgumentException("Starting price must be greater than 0");
        }
        
        if (auction.getSeller() == null || auction.getSeller().getId() == null) {
            throw new IllegalArgumentException("Seller is required");
        }
        
        Auction saved = auctionRepository.save(auction);
        
        if (userService != null) {
            userService.incrementAuctionCount(auction.getSeller().getId());
        }
        
        return saved;
    }
    
    // Get auction by ID
    public Optional<Auction> getAuctionById(Long id) {
        return auctionRepository.findById(id);
    }
    
    // ✅ Changed from enum to String
    public List<Auction> getActiveAuctions() {
        return auctionRepository.findByStatus("ACTIVE");
    }
    
    // ✅ Changed from Category enum to String
    public List<Auction> getAuctionsByCategory(String category) {
        return auctionRepository.findByCategory(category);
    }
    
    // Get auctions by seller
    public List<Auction> getAuctionsBySeller(Long sellerId) {
        return auctionRepository.findBySellerId(sellerId);
    }
    
    // Search auctions by keyword
    public List<Auction> searchAuctions(String keyword) {
        return auctionRepository.searchAuctions(keyword);
    }
    
    // Get all auctions
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }
    
    // Cancel auction
    public Auction cancelAuction(Long auctionId, Long sellerId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new IllegalArgumentException("Auction not found"));
        
        if (!auction.getSeller().getId().equals(sellerId)) {
            throw new IllegalArgumentException("Only the seller can cancel this auction");
        }
        
        if (!"ACTIVE".equals(auction.getStatus())) {
            throw new IllegalArgumentException("Can only cancel active auctions");
        }
        
        auction.setStatus("CANCELLED");
        
        return auctionRepository.save(auction);
    }
    
    // Close expired auctions (scheduler)
    @Scheduled(fixedRate = 300000)
    public void closeExpiredAuctions() {
        List<Auction> expiredAuctions = auctionRepository
            .findByStatusAndEndTimeBefore("ACTIVE", LocalDateTime.now());
        
        for (Auction auction : expiredAuctions) {
            closeAuction(auction);
        }
    }
    
    // Close auction and determine winner
    private void closeAuction(Auction auction) {
        auction.setStatus("ENDED");
        
        Optional<Bid> winningBid = bidRepository.findByAuctionIdAndIsWinningTrue(auction.getId());
        
        if (winningBid.isPresent()) {
            Bid winner = winningBid.get();
            auction.setStatus("COMPLETED");
            
            if (userService != null) {
                userService.incrementCompletedSales(auction.getSeller().getId());
            }
        }
        
        auctionRepository.save(auction);
    }
    
    // Get auction statistics
    public AuctionStats getAuctionStats(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new IllegalArgumentException("Auction not found"));
        
        long bidCount = bidRepository.countByAuctionId(auctionId);
        Double maxBid = bidRepository.findMaxBidAmount(auctionId);
        
        return new AuctionStats(
            auction.getTitle(),
            auction.getCurrentPrice(),
            bidCount,
            maxBid,
            auction.getStatus()
        );
    }
    
    // Stats DTO
    public static class AuctionStats {
        private String title;
        private Double currentPrice;
        private long totalBids;
        private Double highestBid;
        private String status;
        
        public AuctionStats(String title, Double currentPrice, long totalBids, 
                           Double highestBid, String status) {
            this.title = title;
            this.currentPrice = currentPrice;
            this.totalBids = totalBids;
            this.highestBid = highestBid;
            this.status = status;
        }
        
        // Getters
        public String getTitle() { return title; }
        public Double getCurrentPrice() { return currentPrice; }
        public long getTotalBids() { return totalBids; }
        public Double getHighestBid() { return highestBid; }
        public String getStatus() { return status; }
    }
}
