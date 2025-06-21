package com.example.carsale.Model;

public class Sale {
    private String id;
    private String carId;
    private String buyerId;         // customer_id trong Firestore
    private String sellerId;        // salesperson_id trong Firestore
    private double salePrice;
    private double commissionRate;
    private double commissionEarned;
    private long createdAt;
    private long updatedAt;
    private long completedAt;       // dùng khi giao dịch hoàn tất
    private String status;          // pending, completed, cancelled
    private long date;              // ngày bán (hoặc ngày ký hợp đồng)

    public Sale() {
        // Bắt buộc có constructor rỗng cho Firestore
    }

    public Sale(String id, String carId, String buyerId, String sellerId, double salePrice,
                double commissionRate, double commissionEarned, long createdAt, long updatedAt,
                long completedAt, String status, long date) {
        this.id = id;
        this.carId = carId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.salePrice = salePrice;
        this.commissionRate = commissionRate;
        this.commissionEarned = commissionEarned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.status = status;
        this.date = date;
    }

    // Getter & Setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public double getCommissionEarned() {
        return commissionEarned;
    }

    public void setCommissionEarned(double commissionEarned) {
        this.commissionEarned = commissionEarned;
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

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
