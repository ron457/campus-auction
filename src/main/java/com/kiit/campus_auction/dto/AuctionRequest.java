package com.kiit.campus_auction.dto;

public class AuctionRequest {
    
    private String title;
    private String description;
    private String category;
    private String condition;
    private Double startingPrice;
    private String hostelPreference;
    private String sellerEmail;  // ✅ Changed from sellerId to sellerEmail
    private boolean isQuickAuction;
    private Integer durationDays;  // ✅ Add this for custom duration
    
    // Constructors
    public AuctionRequest() {}
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public Double getStartingPrice() {
        return startingPrice;
    }
    
    public void setStartingPrice(Double startingPrice) {
        this.startingPrice = startingPrice;
    }
    
    public String getHostelPreference() {
        return hostelPreference;
    }
    
    public void setHostelPreference(String hostelPreference) {
        this.hostelPreference = hostelPreference;
    }
    
    // ✅ Changed from getSellerId to getSellerEmail
    public String getSellerEmail() {
        return sellerEmail;
    }
    
    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }
    
    public boolean isQuickAuction() {
        return isQuickAuction;
    }
    
    public void setQuickAuction(boolean quickAuction) {
        isQuickAuction = quickAuction;
    }
    
    // ✅ Add duration support
    public Integer getDurationDays() {
        return durationDays;
    }
    
    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }
    
    @Override
    public String toString() {
        return "AuctionRequest{" +
                "title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", startingPrice=" + startingPrice +
                ", sellerEmail='" + sellerEmail + '\'' +
                ", isQuickAuction=" + isQuickAuction +
                '}';
    }
}
