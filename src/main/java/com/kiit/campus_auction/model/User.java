package com.kiit.campus_auction.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

//import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
   @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(unique = true, nullable = false)  // ← ADD THIS!
    private String phone;
    
    @Column(nullable = false)
    private String hostel;
    
    @Column(nullable = false)
    private String batch;
    
    @Column(nullable = false)
    private String branch;
    
    private Double trustScore = 0.0;
    private Integer totalAuctions = 0;
    private Integer completedSales = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // No-args constructor
    public User() {}
    
    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getHostel() { return hostel; }
    public String getBatch() { return batch; }
    public String getBranch() { return branch; }
    public Double getTrustScore() { return trustScore; }
    public Integer getTotalAuctions() { return totalAuctions; }
    public Integer getCompletedSales() { return completedSales; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setHostel(String hostel) { this.hostel = hostel; }
    public void setBatch(String batch) { this.batch = batch; }
    public void setBranch(String branch) { this.branch = branch; }
    public void setTrustScore(Double trustScore) { this.trustScore = trustScore; }
    public void setTotalAuctions(Integer totalAuctions) { this.totalAuctions = totalAuctions; }
    public void setCompletedSales(Integer completedSales) { this.completedSales = completedSales; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
