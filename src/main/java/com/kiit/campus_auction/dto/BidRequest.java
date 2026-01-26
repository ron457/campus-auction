package com.kiit.campus_auction.dto;

public class BidRequest {
    private Long auctionId;
    private String bidderEmail;
    private Double amount;

    // Constructors
    public BidRequest() {}

    public BidRequest(Long auctionId, String bidderEmail, Double amount) {
        this.auctionId = auctionId;
        this.bidderEmail = bidderEmail;
        this.amount = amount;
    }

    // Getters and Setters
    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidderEmail() {
        return bidderEmail;
    }

    public void setBidderEmail(String bidderEmail) {
        this.bidderEmail = bidderEmail;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "BidRequest{" +
                "auctionId=" + auctionId +
                ", bidderEmail='" + bidderEmail + '\'' +
                ", amount=" + amount +
                '}';
    }
}
