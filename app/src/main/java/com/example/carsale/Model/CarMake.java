package com.example.carsale.Model;

public class CarMake {
    private String id;
    private String name;
    private String logoPath;

    public CarMake() {}  // Required for Firestore

    public CarMake(String id, String name, String logoPath) {
        this.id = id;
        this.name = name;
        this.logoPath = logoPath;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLogoPath() { return logoPath; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    @Override
    public String toString() {
        return name; // Hiển thị tên trong Spinner
    }
}
