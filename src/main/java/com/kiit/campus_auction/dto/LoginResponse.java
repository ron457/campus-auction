package com.kiit.campus_auction.dto;

public class LoginResponse {
    private Long id;
    private String name;      // ← ADD THIS
    private String email;
    private String message;
    
    // Constructors
    public LoginResponse(Long id, String name, String email, String message) {
        this.id = id;
        this.name = name;     // ← ADD THIS
        this.email = email;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }      // ← ADD THIS
    public void setName(String name) { this.name = name; }  // ← ADD THIS
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
