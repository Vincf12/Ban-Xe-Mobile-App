package com.example.carsale.Model;

public class Payment {
    private String id;
    private String userId;
    private String carId;
    private double amount;
    private long timestamp;
    private String content;
    private boolean confirmed = false;

    public Payment() {}

    public Payment(String id, String userId, String carId, double amount, long timestamp, String content, boolean confirmed) {
        this.id = id;
        this.userId = userId;
        this.carId = carId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.content = content;
        this.confirmed = confirmed;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }

    private void savePaymentToFirestore() {
        // Implementation of savePaymentToFirestore method
    }
} 