package com.example.carsale.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Car implements Serializable {
    private String id;
    private String make;               // Hãng xe (Toyota, Honda, BMW…)
    private String model;              // Tên xe (Camry, Civic…)
    private Integer year;                  // Năm sản xuất
    private double price;              // Giá bán
    private String condition;          // "Mới" hoặc "Đã qua sử dụng"
    private String carType;            // "Sedan", "SUV", "Bán tải", ...
    private String transmission;       // "Tự động", "Số sàn"
    private String fuelType;           // "Xăng", "Dầu", "Điện", "Hybrid"
    private String engineCapacity;     // Dung tích động cơ (VD: "2.0L")
    private String description;        // Mô tả chi tiết
    private String location;           // Địa điểm bán (TP.HCM, Hà Nội…)
    private String status;             // available / sold / reserved
    private long createdAt;            // Thời gian tạo (timestamp)
    private long updatedAt;            // Thời gian cập nhật (timestamp)
    private String userId;             // ID người đăng (ẩn với user thường)
    private double depositPrice;     // Giá đặt cọc
    private int quantity;            // Số lượng xe
    private Map<String, List<String>> colorImages; // Màu sắc và danh sách ảnh cho từng màu
    private String etBH; // Thời gian bảo hành

    // Constructor mặc định bắt buộc cho Firestore
    public Car() {
    }

    // Constructor đầy đủ (tuỳ chọn)
    public Car(String id, String make, String model, Integer year, double price, String condition,
               String carType, String transmission, String fuelType, String engineCapacity,
               String description, String location, String status,
               long createdAt, long updatedAt, String userId,
               double depositPrice, int quantity, Map<String, List<String>> colorImages, String etBH) {
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
        this.description = description;
        this.location = location;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.depositPrice = depositPrice;
        this.quantity = quantity;
        this.colorImages = colorImages;
        this.etBH = etBH;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
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

    public double getDepositPrice() {
        return depositPrice;
    }

    public void setDepositPrice(double depositPrice) {
        this.depositPrice = depositPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Map<String, List<String>> getColorImages() {
        return colorImages;
    }

    public void setColorImages(Map<String, List<String>> colorImages) {
        this.colorImages = colorImages;
    }
    public List<String> getAllImageUrls() {
        List<String> allUrls = new ArrayList<>();
        if (colorImages != null) {
            for (List<String> images : colorImages.values()) {
                allUrls.addAll(images);
            }
        }
        return allUrls;
    }

    public String getEtBH() {
        return etBH;
    }
    public void setEtBH(String etBH) {
        this.etBH = etBH;
    }
}
