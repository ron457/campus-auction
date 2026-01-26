package com.kiit.campus_auction.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class Bid {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;
    
    @ManyToOne
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(nullable = false)
    private Boolean isWinning = false;
    
    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;
    
    // No-args constructor
    public Bid() {}
    
    // Getters
    public Long getId() { return id; }
    public Auction getAuction() { return auction; }
    public User getBidder() { return bidder; }
    public Double getAmount() { return amount; }
    public Boolean getIsWinning() { return isWinning; }
    public LocalDateTime getBidTime() { return bidTime; }
    
    // ✅ Helper methods for convenience
    public String getBidderEmail() {
        return bidder != null ? bidder.getEmail() : null;
    }
    
    public String getBidderName() {
        return bidder != null ? bidder.getName() : null;
    }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setAuction(Auction auction) { this.auction = auction; }
    public void setBidder(User bidder) { this.bidder = bidder; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setIsWinning(Boolean isWinning) { this.isWinning = isWinning; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }
    
    @PrePersist
    protected void onCreate() {
        bidTime = LocalDateTime.now();
    }
}
