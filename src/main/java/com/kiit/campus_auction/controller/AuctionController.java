package com.kiit.campus_auction.controller;

import com.kiit.campus_auction.dto.AuctionRequest;
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
import java.util.stream.Collectors;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500", "http://127.0.0.1:3000"})
@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;  // ✅ Add this

    @Autowired
    private BidRepository bidRepository;

    // ✅ 1. CREATE AUCTION
    @PostMapping
    public ResponseEntity<?> createAuction(@RequestBody AuctionRequest request) {
        try {
            System.out.println("📥 Received auction request: " + request);
            
            // ✅ Validate seller exists by EMAIL (not ID)
            Optional<User> sellerOpt = userRepository.findByEmail(request.getSellerEmail());
            if (!sellerOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "Seller not found with email: " + request.getSellerEmail()
                    ));
            }
            
            User seller = sellerOpt.get();
            System.out.println("✅ Seller found: " + seller.getName() + " (" + seller.getEmail() + ")");
            
            // Create new auction
            Auction auction = new Auction();
            auction.setSeller(seller);
            auction.setSellerEmail(seller.getEmail());
            auction.setTitle(request.getTitle());
            auction.setDescription(request.getDescription());
            auction.setCategory(request.getCategory());
            auction.setCondition(request.getCondition());
            auction.setStartingPrice(request.getStartingPrice());
            auction.setCurrentPrice(request.getStartingPrice());
            auction.setHostelPreference(request.getHostelPreference());
            auction.setIsQuickAuction(request.isQuickAuction());
            
            // Set auction times
            LocalDateTime now = LocalDateTime.now();
            auction.setStartTime(now);
            
            // ✅ Set end time based on duration or quick auction
            if (request.getDurationDays() != null && request.getDurationDays() > 0) {
                auction.setEndTime(now.plusDays(request.getDurationDays()));
            } else if (request.isQuickAuction()) {
                auction.setEndTime(now.plusHours(24)); // 24 hours for quick auction
            } else {
                auction.setEndTime(now.plusDays(7)); // Default 7 days
            }
            
            auction.setStatus("ACTIVE");
            
            // Save auction
            Auction savedAuction = auctionRepository.save(auction);
            System.out.println("✅ Auction created successfully!");
            System.out.println("   ID: " + savedAuction.getId());
            System.out.println("   Title: " + savedAuction.getTitle());
            System.out.println("   Seller: " + savedAuction.getSellerEmail());
            System.out.println("   End Time: " + savedAuction.getEndTime());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Auction created successfully",
                "auction", savedAuction
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error creating auction: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error creating auction: " + e.getMessage()
                ));
        }
    }
    
    // Fix null seller auction
    // ✅ Change from @PutMapping to @GetMapping
    @GetMapping("/fix-null-seller/{auctionId}")
    public ResponseEntity<?> fixNullSeller(
            @PathVariable Long auctionId,
            @RequestParam String email) {
        try {
            System.out.println("🔧 Fixing auction " + auctionId + " with seller: " + email);
            
            // Find auction
            Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
            if (!auctionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Auction auction = auctionOpt.get();
            System.out.println("📦 Found auction: " + auction.getTitle());
            
            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "User not found with email: " + email));
            }
            
            User user = userOpt.get();
            System.out.println("👤 Found user: " + user.getName());
            
            // ✅ Set both seller relationship AND email
            auction.setSeller(user);
            auction.setSellerEmail(email);
            
            Auction updated = auctionRepository.save(auction);
            
            System.out.println("✅ Auction fixed! Seller set to: " + user.getName() + " (" + email + ")");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Auction seller updated successfully",
                "auctionId", updated.getId(),
                "title", updated.getTitle(),
                "seller", updated.getSellerEmail()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fixing auction: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }
    // ✅ GET ALL AUCTIONS INCLUDING ENDED ONES
    @GetMapping("/all-with-ended")
    public ResponseEntity<?> getAllAuctionsIncludingEnded() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Auction> allAuctions = auctionRepository.findAll();
            
            // Add isExpired flag to each auction
            List<Map<String, Object>> auctionsWithStatus = allAuctions.stream()
                .map(auction -> {
                    Map<String, Object> auctionMap = new HashMap<>();
                    auctionMap.put("id", auction.getId());
                    auctionMap.put("title", auction.getTitle());
                    auctionMap.put("description", auction.getDescription());
                    auctionMap.put("category", auction.getCategory());
                    auctionMap.put("condition", auction.getCondition());
                    auctionMap.put("startingPrice", auction.getStartingPrice());
                    auctionMap.put("currentPrice", auction.getCurrentPrice());
                    auctionMap.put("status", auction.getStatus());
                    auctionMap.put("startTime", auction.getStartTime());
                    auctionMap.put("endTime", auction.getEndTime());
                    auctionMap.put("sellerEmail", auction.getSellerEmail());
                    auctionMap.put("isExpired", auction.getEndTime().isBefore(now) || auction.getEndTime().isEqual(now));
                    return auctionMap;
                })
                .collect(Collectors.toList());
            
            System.out.println("✅ Retrieved all " + auctionsWithStatus.size() + " auctions");
            return ResponseEntity.ok(auctionsWithStatus);
        } catch (Exception e) {
            System.err.println("❌ Error fetching all auctions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch auctions"));
        }
    }




    // ✅ 2. GET ALL ACTIVE AUCTIONS
    // ✅ 2. GET ALL ACTIVE AUCTIONS (Filter by end time)
    @GetMapping("/active")
    public ResponseEntity<?> getActiveAuctions() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Get all ACTIVE status auctions
            List<Auction> activeAuctions = auctionRepository.findByStatus("ACTIVE");
            
            // Filter by end time (only future auctions)
            List<Auction> validAuctions = activeAuctions.stream()
                .filter(auction -> auction.getEndTime().isAfter(now))
                .collect(Collectors.toList());
            
            System.out.println("✅ Found " + validAuctions.size() + " truly active auctions (out of " + activeAuctions.size() + " ACTIVE status)");
            
            // Auto-update status for expired auctions
            activeAuctions.stream()
                .filter(auction -> auction.getEndTime().isBefore(now) || auction.getEndTime().isEqual(now))
                .forEach(auction -> {
                    auction.setStatus("ENDED");
                    auctionRepository.save(auction);
                    System.out.println("⏰ Auto-updated auction " + auction.getId() + " status to ENDED");
                });
            
            return ResponseEntity.ok(validAuctions);
        } catch (Exception e) {
            System.err.println("❌ Error fetching auctions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch auctions"));
        }
    }

    
    // ✅ 3. GET AUCTION BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getAuctionById(@PathVariable Long id) {
        try {
            Optional<Auction> auctionOpt = auctionRepository.findById(id);
            
            if (!auctionOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Auction not found"));
            }
            
            return ResponseEntity.ok(auctionOpt.get());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch auction"));
        }
    }

    // ✅ 4. GET WINNING BID FOR AUCTION
    @GetMapping("/{id}/winning")
    public ResponseEntity<?> getWinningBid(@PathVariable Long id) {
        try {
            System.out.println("🏆 Fetching winning bid for auction: " + id);
            
            // Verify auction exists
            Optional<Auction> auctionOpt = auctionRepository.findById(id);
            if (!auctionOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Auction not found"));
            }
            
            // Get highest bid for this auction
            Optional<Bid> winningBidOpt = bidRepository.findFirstByAuctionIdOrderByAmountDesc(id);
            
            if (!winningBidOpt.isPresent()) {
                // Return empty object instead of empty body - prevents JSON parse errors
                System.out.println("ℹ️ No bids found for auction " + id);
                return ResponseEntity.ok(Collections.emptyMap());
            }
            
            Bid winningBid = winningBidOpt.get();
            User bidder = winningBid.getBidder();
            
            System.out.println("✅ Winning bid: ₹" + winningBid.getAmount() + " by " + bidder.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "id", winningBid.getId(),
                "auctionId", winningBid.getAuction().getId(),
                "bidderEmail", bidder.getEmail(),
                "bidderName", bidder.getName(),
                "amount", winningBid.getAmount(),
                "bidTime", winningBid.getBidTime(),
                "isWinning", winningBid.getIsWinning()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching winning bid: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch winning bid"));
        }
    }
    
    // ✅ 5. GET BID COUNT FOR AUCTION
    @GetMapping("/{id}/bid-count")
    public ResponseEntity<?> getBidCount(@PathVariable Long id) {
        try {
            long count = bidRepository.countByAuctionId(id);
            System.out.println("📊 Bid count for auction " + id + ": " + count);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            System.err.println("❌ Error counting bids: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("count", 0));
        }
    }

    // ✅ 6. SEARCH AUCTIONS BY KEYWORD (Filter expired)
    @GetMapping("/search")
    public ResponseEntity<?> searchAuctions(@RequestParam String keyword) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Auction> results = auctionRepository.searchAuctions(keyword);
            
            // Filter out expired auctions
            List<Auction> activeResults = results.stream()
                .filter(auction -> auction.getEndTime().isAfter(now))
                .collect(Collectors.toList());
            
            System.out.println("🔍 Search results for '" + keyword + "': " + activeResults.size() + " active auctions (filtered from " + results.size() + " total)");
            return ResponseEntity.ok(activeResults);
        } catch (Exception e) {
            System.err.println("❌ Search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Search failed"));
        }
    }


    // ✅ 7. GET AUCTIONS BY CATEGORY (Filter expired)
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getAuctionsByCategory(@PathVariable String category) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Auction> auctions = auctionRepository.findByCategoryAndStatus(category, "ACTIVE");
            
            // Filter by end time
            List<Auction> activeAuctions = auctions.stream()
                .filter(auction -> auction.getEndTime().isAfter(now))
                .collect(Collectors.toList());
            
            System.out.println("📁 Category '" + category + "': " + activeAuctions.size() + " active auctions");
            return ResponseEntity.ok(activeAuctions);
        } catch (Exception e) {
            System.err.println("❌ Category fetch failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch category auctions"));
        }
    }


    // ✅ 8. GET AUCTIONS BY SELLER
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<?> getAuctionsBySeller(@PathVariable Long sellerId) {
        try {
            List<Auction> auctions = auctionRepository.findBySellerId(sellerId);
            System.out.println("👤 Seller " + sellerId + " has " + auctions.size() + " auctions");
            return ResponseEntity.ok(auctions);
        } catch (Exception e) {
            System.err.println("❌ Seller auctions fetch failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch seller auctions"));
        }
    }

    // ✅ 9. DELETE AUCTION (Only by seller)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAuction(@PathVariable Long id) {
        try {
            Optional<Auction> auctionOpt = auctionRepository.findById(id);
            
            if (!auctionOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Auction not found"));
            }
            
            auctionRepository.deleteById(id);
            System.out.println("🗑️ Auction " + id + " deleted successfully");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Auction deleted successfully"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Delete failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete auction"));
        }
    }

    // ✅ 10. UPDATE AUCTION STATUS (for ending auctions, marking as sold, etc.)
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateAuctionStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Optional<Auction> auctionOpt = auctionRepository.findById(id);
            
            if (!auctionOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Auction not found"));
            }
            
            Auction auction = auctionOpt.get();
            auction.setStatus(status);
            auctionRepository.save(auction);
            
            System.out.println("✅ Auction " + id + " status updated to: " + status);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Auction status updated",
                "auction", auction
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Status update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update auction status"));
        }
    }

    // ✅ 11. GET ALL AUCTIONS (for admin/testing)
    @GetMapping
    public ResponseEntity<?> getAllAuctions() {
        try {
            List<Auction> allAuctions = auctionRepository.findAll();
            System.out.println("✅ Retrieved all " + allAuctions.size() + " auctions");
            return ResponseEntity.ok(allAuctions);
        } catch (Exception e) {
            System.err.println("❌ Error fetching all auctions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch auctions"));
        }
    }
    // ✅ GET AUCTIONS BY SELLER EMAIL (for My Auctions page)
// ✅ GET AUCTIONS BY SELLER EMAIL (for My Auctions page)
@GetMapping("/seller/email/{email}")
public ResponseEntity<?> getAuctionsBySellerEmail(@PathVariable String email) {
    try {
        System.out.println("📋 Fetching auctions for seller: " + email);
        
        // Get user
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        User seller = userOpt.get();
        List<Auction> auctions = auctionRepository.findBySellerId(seller.getId());
        
        // Add bid statistics for each auction
        List<Map<String, Object>> auctionDetails = new ArrayList<>();
        
        for (Auction auction : auctions) {
            long bidCount = bidRepository.countByAuctionId(auction.getId());
            
            Optional<Bid> winningBidOpt = bidRepository.findFirstByAuctionIdOrderByAmountDesc(auction.getId());
            
            // ✅ Use HashMap for more than 10 entries
            Map<String, Object> auctionData = new HashMap<>();
            auctionData.put("id", auction.getId());
            auctionData.put("title", auction.getTitle());
            auctionData.put("description", auction.getDescription());
            auctionData.put("category", auction.getCategory());
            auctionData.put("condition", auction.getCondition());
            auctionData.put("startingPrice", auction.getStartingPrice());
            auctionData.put("currentPrice", auction.getCurrentPrice());
            auctionData.put("status", auction.getStatus());
            auctionData.put("startTime", auction.getStartTime());
            auctionData.put("endTime", auction.getEndTime());
            auctionData.put("bidCount", bidCount);
            auctionData.put("hasWinner", winningBidOpt.isPresent());
            auctionData.put("winnerEmail", winningBidOpt.isPresent() ? 
                winningBidOpt.get().getBidder().getEmail() : null);
            
            auctionDetails.add(auctionData);
        }
        
        System.out.println("✅ Found " + auctionDetails.size() + " auctions for " + email);
        return ResponseEntity.ok(auctionDetails);
        
    } catch (Exception e) {
        System.err.println("❌ Error fetching seller auctions: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Failed to fetch auctions"));
    }
}

}
