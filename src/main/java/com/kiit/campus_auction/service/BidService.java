package com.kiit.campus_auction.service;

import com.kiit.campus_auction.model.Auction;
import com.kiit.campus_auction.model.Bid;
import com.kiit.campus_auction.model.User;
import com.kiit.campus_auction.repository.AuctionRepository;
import com.kiit.campus_auction.repository.BidRepository;
import com.kiit.campus_auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BidService {
    
    @Autowired
    private BidRepository bidRepository;
    
    @Autowired
    private AuctionRepository auctionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${auction.min.increment:50}")
    private Double minBidIncrement;
    
    // Place a new bid
    public Bid placeBid(Long auctionId, Long bidderId, Double amount) {
        // Validate auction exists
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new IllegalArgumentException("Auction not found"));
        
        // ✅ Changed from enum to String
        if (!"ACTIVE".equals(auction.getStatus())) {
            throw new IllegalArgumentException("Auction is not active. Status: " + auction.getStatus());
        }
        
        // Check if auction expired
        if (auction.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Auction has ended");
        }
        
        // Seller cannot bid on own auction
        if (auction.getSeller().getId().equals(bidderId)) {
            throw new IllegalArgumentException("Sellers cannot bid on their own auctions");
        }
        
        // Validate bid amount
        Double currentPrice = auction.getCurrentPrice() != null ? 
                              auction.getCurrentPrice() : auction.getStartingPrice();
        Double minimumBid = currentPrice + minBidIncrement;
        
        if (amount < minimumBid) {
            throw new IllegalArgumentException(
                String.format("Bid must be at least ₹%.2f (current price ₹%.2f + minimum increment ₹%.2f)", 
                    minimumBid, currentPrice, minBidIncrement)
            );
        }
        
        // Mark old winning bid as not winning
        Optional<Bid> currentWinningBid = bidRepository.findByAuctionIdAndIsWinningTrue(auctionId);
        if (currentWinningBid.isPresent()) {
            Bid oldWinner = currentWinningBid.get();
            oldWinner.setIsWinning(false);
            bidRepository.save(oldWinner);
        }
        
        // Create new bid
        Bid newBid = new Bid();
        newBid.setAuction(auction);
        newBid.setBidder(findUserById(bidderId));
        newBid.setAmount(amount);
        newBid.setIsWinning(true);
        
        Bid savedBid = bidRepository.save(newBid);
        
        // Update auction's current highest
        auction.setCurrentPrice(amount);
        auctionRepository.save(auction);
        
        return savedBid;
    }
    
    // Get all bids for auction
    public List<Bid> getBidsForAuction(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByAmountDesc(auctionId);
    }
    
    // Get bids by user
    public List<Bid> getBidsByUser(Long bidderId) {
        return bidRepository.findByBidderId(bidderId);
    }
    
    // Get winning bid
    public Optional<Bid> getWinningBid(Long auctionId) {
        return bidRepository.findByAuctionIdAndIsWinningTrue(auctionId);
    }
    
    // Get bid count
    public long getBidCount(Long auctionId) {
        return bidRepository.countByAuctionId(auctionId);
    }
    
    // Check if user is winning
    public boolean isUserWinning(Long auctionId, Long bidderId) {
        Optional<Bid> winningBid = bidRepository.findByAuctionIdAndIsWinningTrue(auctionId);
        return winningBid.isPresent() && winningBid.get().getBidder().getId().equals(bidderId);
    }
    
    // Get highest bid amount
    public Double getHighestBidAmount(Long auctionId) {
        return bidRepository.findMaxBidAmount(auctionId);
    }
    
    // Helper method to find user
    private User findUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }
}
