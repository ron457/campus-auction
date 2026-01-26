package com.kiit.campus_auction.controller;
import com.kiit.campus_auction.dto.BidRequest;
import com.kiit.campus_auction.model.Auction;
import com.kiit.campus_auction.model.Bid;
import com.kiit.campus_auction.model.User;
import com.kiit.campus_auction.repository.AuctionRepository;
import com.kiit.campus_auction.repository.BidRepository;
import com.kiit.campus_auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;



@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500", "http://127.0.0.1:3000"})
@RestController
@RequestMapping("/api/bids")
public class BidController {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ 1. PLACE BID
    @PostMapping
    public ResponseEntity<?> placeBid(@RequestBody BidRequest request) {
        try {
            System.out.println("📥 Received bid request: " + request);

            // Validate auction exists
            Optional<Auction> auctionOpt = auctionRepository.findById(request.getAuctionId());
            if (!auctionOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "Auction not found"
                    ));
            }

            Auction auction = auctionOpt.get();
            System.out.println("✅ Auction found: " + auction.getTitle());

            // Check if auction is active
            if (!"ACTIVE".equals(auction.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "message", "Auction is not active"
                    ));
            }

            // Get bidder by email
            Optional<User> bidderOpt = userRepository.findByEmail(request.getBidderEmail());
            if (!bidderOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "Bidder not found. Please login first."
                    ));
            }

            User bidder = bidderOpt.get();
            System.out.println("✅ Bidder found: " + bidder.getName());

            // ✅ CHECK: Seller cannot bid on own auction
            if (auction.getSeller().getId().equals(bidder.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "message", "You cannot bid on your own auction!"
                    ));
            }

            // Get current highest bid
            Optional<Bid> currentWinningBidOpt = bidRepository.findFirstByAuctionIdOrderByAmountDesc(auction.getId());
            
            double currentPrice = auction.getStartingPrice();
            if (currentWinningBidOpt.isPresent()) {
                currentPrice = currentWinningBidOpt.get().getAmount();
            }

            // Validate bid amount (minimum increment: ₹50)
            double minimumBid = currentPrice + 50.0;
            
            if (request.getAmount() < minimumBid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "message", String.format(
                            "Bid must be at least ₹%.2f (current price ₹%.2f + minimum increment ₹50.00)",
                            minimumBid, currentPrice
                        )
                    ));
            }

            // ✅ Mark previous winning bid as not winning
            if (currentWinningBidOpt.isPresent()) {
                Bid previousWinning = currentWinningBidOpt.get();
                previousWinning.setIsWinning(false);
                bidRepository.save(previousWinning);
                System.out.println("🔄 Previous winning bid updated");
            }

            // Create new bid
            Bid newBid = new Bid();
            newBid.setAuction(auction);
            newBid.setBidder(bidder);
            newBid.setAmount(request.getAmount());
            newBid.setIsWinning(true);

            // Save bid
            Bid savedBid = bidRepository.save(newBid);
            System.out.println("✅ Bid saved with ID: " + savedBid.getId());

            // Update auction current price
            auction.setCurrentPrice(request.getAmount());
            auctionRepository.save(auction);
            System.out.println("✅ Auction price updated to: ₹" + request.getAmount());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bid placed successfully! 🎉",
                "bid", Map.of(
                    "id", savedBid.getId(),
                    "amount", savedBid.getAmount(),
                    "bidderEmail", bidder.getEmail(),
                    "bidTime", savedBid.getBidTime()
                )
            ));

        } catch (Exception e) {
            System.err.println("❌ Error placing bid: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error placing bid: " + e.getMessage()
                ));
        }
    }
    // ✅ GET AUCTIONS WON BY USER
    @GetMapping("/user/email/{email}/won")
    public ResponseEntity<?> getAuctionsWonByUser(@PathVariable String email) {
        try {
            System.out.println("🏆 Fetching won auctions for: " + email);
            
            // Find user
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            User user = userOpt.get();
            
            // Get all bids by this user
            List<Bid> userBids = bidRepository.findByBidderId(user.getId());
            
            // Find winning bids
            List<Map<String, Object>> wonAuctions = new ArrayList<>();
            Set<Long> processedAuctions = new HashSet<>();
            
            for (Bid bid : userBids) {
                Auction auction = bid.getAuction();
                
                // Skip if already processed
                if (processedAuctions.contains(auction.getId())) {
                    continue;
                }
                
                // Check if auction has ended
                if (auction.getEndTime().isBefore(LocalDateTime.now())) {
                    // Get winning bid for this auction
                    Optional<Bid> winningBidOpt = bidRepository.findFirstByAuctionIdOrderByAmountDesc(auction.getId());
                    
                    if (winningBidOpt.isPresent() && winningBidOpt.get().getId().equals(bid.getId())) {
                        // This user's bid is the winning bid!
                        Map<String, Object> wonAuction = new HashMap<>();
                        wonAuction.put("auctionId", auction.getId());
                        wonAuction.put("title", auction.getTitle());
                        wonAuction.put("description", auction.getDescription());
                        wonAuction.put("category", auction.getCategory());
                        wonAuction.put("condition", auction.getCondition());
                        wonAuction.put("winningBid", bid.getAmount());
                        wonAuction.put("sellerEmail", auction.getSellerEmail());
                        
                        // Get seller name safely
                        if (auction.getSeller() != null) {
                            wonAuction.put("sellerName", auction.getSeller().getName());
                        } else {
                            wonAuction.put("sellerName", "Unknown");
                        }
                        
                        wonAuction.put("endTime", auction.getEndTime());
                        wonAuction.put("bidCount", bidRepository.countByAuctionId(auction.getId()));
                        wonAuctions.add(wonAuction);
                        processedAuctions.add(auction.getId());
                    }
                }
            }
            
            System.out.println("✅ Found " + wonAuctions.size() + " won auctions for " + email);
            return ResponseEntity.ok(wonAuctions);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching won auctions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.emptyList());
        }
    }

    // ✅ 2. GET ALL BIDS FOR AUCTION
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<?> getAuctionBids(@PathVariable Long auctionId) {
        try {
            List<Bid> bids = bidRepository.findByAuctionIdOrderByAmountDesc(auctionId);
            
            List<Map<String, Object>> bidList = new ArrayList<>();
            for (Bid bid : bids) {
                bidList.add(Map.of(
                    "id", bid.getId(),
                    "amount", bid.getAmount(),
                    "bidderEmail", bid.getBidder().getEmail(),
                    "bidderName", bid.getBidder().getName(),
                    "bidTime", bid.getBidTime(),
                    "isWinning", bid.getIsWinning()
                ));
            }
            
            return ResponseEntity.ok(bidList);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching bids: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch bids"));
        }
    }

    // ✅ 3. GET BIDS BY USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBids(@PathVariable Long userId) {
        try {
            List<Bid> bids = bidRepository.findByBidderId(userId);
            return ResponseEntity.ok(bids);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch user bids"));
        }
    }
    // ✅ GET BIDS BY USER EMAIL (for My Bids page)
    @GetMapping("/user/email/{email}")
    public ResponseEntity<?> getUserBidsByEmail(@PathVariable String email) {
        try {
            System.out.println("📋 Fetching bids for user: " + email);
            
            // Get user
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            User user = userOpt.get();
            List<Bid> userBids = bidRepository.findByBidderId(user.getId());
            
            // Create response with auction details
            List<Map<String, Object>> bidDetails = new ArrayList<>();
            
            for (Bid bid : userBids) {
                Auction auction = bid.getAuction();
                
                // Get current winning bid
                Optional<Bid> currentWinningOpt = bidRepository.findFirstByAuctionIdOrderByAmountDesc(auction.getId());
                boolean isCurrentlyWinning = currentWinningOpt.isPresent() && 
                                            currentWinningOpt.get().getId().equals(bid.getId());
                
                // ✅ Use HashMap for auction data
                Map<String, Object> auctionMap = new HashMap<>();
                auctionMap.put("id", auction.getId());
                auctionMap.put("title", auction.getTitle());
                auctionMap.put("description", auction.getDescription());
                auctionMap.put("category", auction.getCategory());
                auctionMap.put("currentPrice", auction.getCurrentPrice());
                auctionMap.put("status", auction.getStatus());
                auctionMap.put("endTime", auction.getEndTime());
                auctionMap.put("sellerEmail", auction.getSellerEmail());
                
                // ✅ Use HashMap for bid details
                Map<String, Object> bidMap = new HashMap<>();
                bidMap.put("bidId", bid.getId());
                bidMap.put("amount", bid.getAmount());
                bidMap.put("bidTime", bid.getBidTime());
                bidMap.put("isWinning", isCurrentlyWinning);
                bidMap.put("auction", auctionMap);
                
                bidDetails.add(bidMap);
            }
            
            System.out.println("✅ Found " + bidDetails.size() + " bids for " + email);
            return ResponseEntity.ok(bidDetails);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching user bids: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch bids"));
        }
    }


}
