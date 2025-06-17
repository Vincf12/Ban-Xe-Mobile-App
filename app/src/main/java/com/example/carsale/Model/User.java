package com.example.carsale.Model;

public class User {
    private boolean isAdmin;
    private String id;
    private String username;
    private String email;
    private long createdAt;
    private boolean isActive;

    // Constructor rỗng (bắt buộc cho Firebase)
    public User() {}

    // Constructor với tham số
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    // Getters và Setters
    public boolean isAdmin(){
        return isAdmin;
    }
    public void setAdmin(boolean admin){
        isAdmin = admin;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
