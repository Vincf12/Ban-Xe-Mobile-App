package com.example.carsale.Model;

public class Car {
    private String id;
    private String make;         // Hãng xe
    private String model;        // Mẫu xe
    private int year;            // Năm sản xuất
    private double price;        // Giá xe
    private String status;       // Trạng thái: available, sold, reserved
    private long createdAt;      // Thời gian tạo
    private long updatedAt;      // Thời gian cập nhật

    private String imageUrl;

    // Bắt buộc phải có constructor rỗng cho Firestore
    public Car() {
    }

    public Car(String id, String make, String model, int year, double price, String status, long createdAt, long updatedAt) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.year = year;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter và Setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
