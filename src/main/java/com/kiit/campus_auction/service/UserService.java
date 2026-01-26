package com.kiit.campus_auction.service;

import com.kiit.campus_auction.model.User;
import com.kiit.campus_auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Register new user
    public User registerUser(User user) {
        // Business Rule 1: Email must be @kiit.ac.in
        if (!user.getEmail().endsWith("@kiit.ac.in")) {
            throw new IllegalArgumentException("Only KIIT email addresses allowed");
        }
        
        // Business Rule 2: Email must be unique
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Business Rule 3: Initialize trust score and counters
        user.setTrustScore(0.0);
        user.setTotalAuctions(0);
        user.setCompletedSales(0);
        
        return userRepository.save(user);
    }
    
    // Find user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    // Find user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Get users by hostel
    public List<User> getUsersByHostel(String hostel) {
        return userRepository.findByHostel(hostel);
    }
    
    // Update trust score (called when auction completes)
    public void updateTrustScore(Long userId, double newScore) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setTrustScore(newScore);
        userRepository.save(user);
    }
    
    // Increment auction counters
    public void incrementAuctionCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setTotalAuctions(user.getTotalAuctions() + 1);
        userRepository.save(user);
    }
    
    public void incrementCompletedSales(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setCompletedSales(user.getCompletedSales() + 1);
        
        // Calculate new trust score: (completed sales / total auctions) * 100
        if (user.getTotalAuctions() > 0) {
            double trustScore = ((double) user.getCompletedSales() / user.getTotalAuctions()) * 100;
            user.setTrustScore(trustScore);
        }
        
        userRepository.save(user);
    }
}
