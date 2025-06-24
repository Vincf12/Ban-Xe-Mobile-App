package com.example.carsale.Model;

import java.io.Serializable;
import java.util.List;

public class Car implements Serializable {
    private String id;
    private String make;               // Hãng xe (Toyota, Honda, BMW…)
    private String model;              // Tên xe (Camry, Civic…)
    private int year;                  // Năm sản xuất
    private double price;              // Giá bán
    private String condition;          // "Mới" hoặc "Đã qua sử dụng"
    private String carType;            // "Sedan", "SUV", "Bán tải", ...
    private String transmission;       // "Tự động", "Số sàn"
    private String fuelType;           // "Xăng", "Dầu", "Điện", "Hybrid"
    private String engineCapacity;     // Dung tích động cơ (VD: "2.0L")
    private List<String> imageUrls;    // Danh sách URL ảnh
    private String description;        // Mô tả chi tiết
    private String location;           // Địa điểm bán (TP.HCM, Hà Nội…)
    private String status;             // available / sold / reserved
    private long createdAt;            // Thời gian tạo (timestamp)
    private long updatedAt;            // Thời gian cập nhật (timestamp)
    private String userId;             // ID người đăng (ẩn với user thường)

    // Constructor mặc định bắt buộc cho Firestore
    public Car() {
    }

    // Constructor đầy đủ (tuỳ chọn)
    public Car(String id, String make, String model, int year, double price, String condition,
               String carType, String transmission, String fuelType, String engineCapacity,
               List<String> imageUrls, String description, String location, String status,
               long createdAt, long updatedAt, String userId) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.year = year;
        this.price = price;
        this.condition = condition;
        this.carType = carType;
        this.transmission = transmission;
        this.fuelType = fuelType;
        this.engineCapacity = engineCapacity;
        this.imageUrls = imageUrls;
        this.description = description;
        this.location = location;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
    }

    // Getters và Setters
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

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getEngineCapacity() {
        return engineCapacity;
    }

    public void setEngineCapacity(String engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
