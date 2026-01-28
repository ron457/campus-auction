package com.kiit.campus_auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiit.campus_auction.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
    
    // Spring generates: SELECT * FROM users WHERE email = ? (returns boolean)
    boolean existsByEmail(String email);
    
    // Spring generates: SELECT * FROM users WHERE hostel = ?
    java.util.List<User> findByHostel(String hostel);

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);
}

