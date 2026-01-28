package com.kiit.campus_auction.controller;

import com.kiit.campus_auction.model.User;
import com.kiit.campus_auction.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")  // Update with your Netlify URL for production
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // ✅ 1. REGISTER USER
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // Let UserService handle ALL validations (email + phone + KIIT email)
            User savedUser = userService.registerUser(user);
            
            System.out.println("✅ User registered: " + savedUser.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Registration successful",
                "user", savedUser
            ));
            
        } catch (IllegalArgumentException e) {
            // Catches validation errors:
            // - Email already registered
            // - Phone number already registered
            // - Only KIIT email addresses allowed
            System.out.println("❌ Registration failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
            
        } catch (Exception e) {
            // Catches unexpected errors
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Registration failed: " + e.getMessage()
                ));
        }
    }
    
    // ✅ 2. LOGIN USER
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            
            User user = userService.loginUser(email, password);
            
            System.out.println("✅ User logged in: " + user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "user", user
            ));
            
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Login failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
                
        } catch (Exception e) {
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Login failed: " + e.getMessage()
                ));
        }
    }
    
    // ✅ 3. GET ALL USERS
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    // ✅ 4. GET USER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
        }
    }
    
    // ✅ 5. GET USER BY EMAIL
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
        }
    }
    
    // ✅ 6. GET USER BY PHONE
    @GetMapping("/phone/{phone}")
    public ResponseEntity<?> getUserByPhone(@PathVariable String phone) {
        Optional<User> user = userService.getUserByPhone(phone);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
        }
    }
    
    // ✅ 7. GET USERS BY HOSTEL
    @GetMapping("/hostel/{hostel}")
    public ResponseEntity<List<User>> getUsersByHostel(@PathVariable String hostel) {
        List<User> users = userService.getUsersByHostel(hostel);
        return ResponseEntity.ok(users);
    }
    
    // ✅ 8. UPDATE TRUST SCORE
    @PutMapping("/{id}/trust-score")
    public ResponseEntity<?> updateTrustScore(
            @PathVariable Long id, 
            @RequestBody Map<String, Double> request) {
        try {
            double newScore = request.get("trustScore");
            userService.updateTrustScore(id, newScore);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Trust score updated"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        }
    }
    
    // ✅ 9. INCREMENT AUCTION COUNT
    @PutMapping("/{id}/increment-auctions")
    public ResponseEntity<?> incrementAuctionCount(@PathVariable Long id) {
        try {
            userService.incrementAuctionCount(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Auction count incremented"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        }
    }
    
    // ✅ 10. INCREMENT COMPLETED SALES
    @PutMapping("/{id}/increment-sales")
    public ResponseEntity<?> incrementCompletedSales(@PathVariable Long id) {
        try {
            userService.incrementCompletedSales(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Completed sales incremented"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        }
    }
}
