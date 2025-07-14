package com.example.carsale.Model;

public class User {
    private boolean isAdmin;
    private String id;
    private String username;
    private String fullname;
    private String email;
    private String address;
    private String gender;
    private int age;
    private int cccd;
    private long createdAt;
    private boolean isActive;
    private String phone;

    // Constructor rỗng (bắt buộc cho Firebase)
    public User() {}

    // Constructor với tham số
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    // Constructor với đầy đủ tham số
    public User(String username,String fullname, String email, String address, String gender, int age, int cccd) {
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.address = address;
        this.gender = gender;
        this.age = age;
        this.cccd = cccd;
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

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getCccd() {
        return cccd;
    }

    public void setCccd(int cccd) {
        this.cccd = cccd;
    }
}
