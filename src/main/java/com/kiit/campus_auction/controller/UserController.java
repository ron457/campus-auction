package com.kiit.campus_auction.controller;

import com.kiit.campus_auction.dto.LoginRequest;
import com.kiit.campus_auction.model.User;
import com.kiit.campus_auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500", "http://127.0.0.1:3000"})
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    // ✅ 1. REGISTER USER
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                        "success", false,
                        "message", "Email already registered"
                    ));
            }

            // Save user
            User savedUser = userRepository.save(user);
            System.out.println("✅ User registered: " + savedUser.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Registration successful",
                "user", savedUser
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Registration failed: " + e.getMessage()
                ));
        }
    }

   // ✅ UPDATE USER PROFILE BY EMAIL
    @PutMapping("/email/{email}")
    public ResponseEntity<?> updateUserByEmail(
            @PathVariable String email, 
            @RequestBody Map<String, String> updates) {
        try {
            System.out.println("Updating user with email: " + email);
            
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "User not found"
                    ));
            }
            
            User user = userOptional.get();
            
            // Update fields if present in request
            if (updates.containsKey("name") && updates.get("name") != null) {
                user.setName(updates.get("name"));
            }
            if (updates.containsKey("phone") && updates.get("phone") != null) {
                user.setPhone(updates.get("phone"));
            }
            if (updates.containsKey("branch") && updates.get("branch") != null) {
                user.setBranch(updates.get("branch"));
            }
            if (updates.containsKey("batch") && updates.get("batch") != null) {
                user.setBatch(updates.get("batch"));
            }
            if (updates.containsKey("hostel") && updates.get("hostel") != null) {
                user.setHostel(updates.get("hostel"));
            }
            
            User updatedUser = userRepository.save(user);
            System.out.println("✅ User profile updated: " + updatedUser.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated successfully",
                "user", updatedUser
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Update failed: " + e.getMessage()
                ));
        }
    }

        
    // ✅ 2. LOGIN USER
    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    try {
        System.out.println("Login attempt for email: " + loginRequest.getEmail());
        
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        
        if (!userOptional.isPresent()) {
            System.out.println("❌ User not found with email: " + loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND) // ← 404 (not 401!)
                .body(Map.of(
                    "success", false,
                    "message", "No account found with this email"
                ));
        }
        
        User user = userOptional.get();
        System.out.println("User found: " + user.getName());

        // Check password
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            System.out.println("❌ Incorrect password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // ← 401
                .body(Map.of(
                    "success", false,
                    "message", "Incorrect password"
                ));
        }

        System.out.println("✅ Login successful for: " + user.getName());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Login successful",
            "user", user
        ));

    } catch (Exception e) {
        System.err.println("❌ Login error: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "success", false,
                "message", "Server error. Please try again later."
            ));
    }
}


    // ✅ 3. GET USER BY ID (For profile page)
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            System.out.println("Fetching user with ID: " + id);
            
            Optional<User> userOptional = userRepository.findById(id);
            
            if (!userOptional.isPresent()) {
                System.out.println("❌ User not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "User not found with ID: " + id
                    ));
            }
            
            User user = userOptional.get();
            System.out.println("✅ User fetched by ID: " + user.getEmail());
            
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error fetching user: " + e.getMessage()
                ));
        }
    }

    // 3.5 ✅ ADD THIS METHOD - GET USER BY EMAIL
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            System.out.println("🔍 Fetching user by email: " + email);
            
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (!userOptional.isPresent()) {
                System.out.println("❌ User not found with email: " + email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "User not found"
                    ));
            }
            
            User user = userOptional.get();
            System.out.println("✅ User found: " + user.getName());
            
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
                ));
        }
    }


    // ✅ 4. UPDATE USER PROFILE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id, 
            @RequestBody Map<String, String> updates) {
        try {
            System.out.println("Updating user with ID: " + id);
            
            Optional<User> userOptional = userRepository.findById(id);
            
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "User not found"
                    ));
            }
            
            User user = userOptional.get();
            
            // Update fields if present in request
            if (updates.containsKey("name") && updates.get("name") != null) {
                user.setName(updates.get("name"));
            }
            if (updates.containsKey("phone") && updates.get("phone") != null) {
                user.setPhone(updates.get("phone"));
            }
            if (updates.containsKey("branch") && updates.get("branch") != null) {
                user.setBranch(updates.get("branch"));
            }
            if (updates.containsKey("batch") && updates.get("batch") != null) {
                user.setBatch(updates.get("batch"));
            }
            if (updates.containsKey("hostel") && updates.get("hostel") != null) {
                user.setHostel(updates.get("hostel"));
            }
            
            User updatedUser = userRepository.save(user);
            System.out.println("✅ User profile updated: " + updatedUser.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated successfully",
                "user", updatedUser
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Update failed: " + e.getMessage()
                ));
        }
    }

    // ✅ 5. CHANGE PASSWORD
    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id, 
            @RequestBody Map<String, String> passwordData) {
        try {
            System.out.println("Password change request for user ID: " + id);
            
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            // Validate inputs
            if (currentPassword == null || currentPassword.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "message", "Current password is required"
                    ));
            }
            
            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "message", "New password must be at least 6 characters"
                    ));
            }
            
            // Find user
            Optional<User> userOptional = userRepository.findById(id);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "User not found"
                    ));
            }
            
            User user = userOptional.get();
            
            // Verify current password
            if (!user.getPassword().equals(currentPassword)) {
                System.out.println("❌ Current password incorrect for user: " + user.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "message", "Current password is incorrect"
                    ));
            }
            
            // Update password
            user.setPassword(newPassword);
            userRepository.save(user);
            
            System.out.println("✅ Password changed successfully for user: " + user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully"
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Failed to change password: " + e.getMessage()
                ));
        }
    }

    // ✅ 6. GET ALL USERS (Optional - for admin)
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error fetching users: " + e.getMessage()
                ));
        }
    }
}
