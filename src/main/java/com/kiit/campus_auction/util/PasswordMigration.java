package com.kiit.campus_auction.util;

import com.kiit.campus_auction.model.User;
import com.kiit.campus_auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PasswordMigration implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔒 Checking for plain text passwords...");
        
        List<User> users = userRepository.findAll();
        int updated = 0;
        
        for (User user : users) {
            String password = user.getPassword();
            
            // BCrypt hashes start with "$2a$" or "$2b$"
            if (!password.startsWith("$2a$") && !password.startsWith("$2b$")) {
                System.out.println("⚠️  Found plain text password for: " + user.getEmail());
                
                // Hash the plain text password
                String hashedPassword = passwordEncoder.encode(password);
                user.setPassword(hashedPassword);
                userRepository.save(user);
                
                System.out.println("✅ Updated password for: " + user.getEmail());
                updated++;
            }
        }
        
        if (updated == 0) {
            System.out.println("✅ All passwords are already hashed!");
        } else {
            System.out.println("✅ Updated " + updated + " passwords!");
        }
    }
}
