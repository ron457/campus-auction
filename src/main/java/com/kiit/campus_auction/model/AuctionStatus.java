package com.kiit.campus_auction.model;

public enum AuctionStatus {
    ACTIVE,      // Auction is running, accepting bids
    ENDED,       // Time expired, determining winner
    COMPLETED,   // Winner notified, transaction done
    CANCELLED    // Seller cancelled before end
}
