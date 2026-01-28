package com.kiit.campus_auction.service;

import com.kiit.campus_auction.model.User;
import com.kiit.campus_auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(User user) {
        if (!user.getEmail().endsWith("@kiit.ac.in")) {
            throw new IllegalArgumentException("Only KIIT email addresses allowed");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new IllegalArgumentException("Phone number already registered");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setTrustScore(0.0);
        user.setTotalAuctions(0);
        user.setCompletedSales(0);
        
        return userRepository.save(user);
    }
    
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        
        return user;
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getUsersByHostel(String hostel) {
        return userRepository.findByHostel(hostel);
    }
    
    public void updateTrustScore(Long userId, double newScore) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setTrustScore(newScore);
        userRepository.save(user);
    }
    
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
        
        if (user.getTotalAuctions() > 0) {
            double trustScore = ((double) user.getCompletedSales() / user.getTotalAuctions()) * 100;
            user.setTrustScore(trustScore);
        }
        
        userRepository.save(user);
    }
}
