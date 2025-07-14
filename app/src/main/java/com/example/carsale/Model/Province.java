package com.example.carsale.Model;

public class Province {
    private int code;
    private String name;
    private String name_en;
    private String full_name;
    private String full_name_en;
    private String code_name;
    private int administrative_unit_id;
    private int administrative_region_id;
    private String id;

    public Province() {}

    public Province(int code, String name, String name_en, String full_name, 
                   String full_name_en, String code_name, 
                   int administrative_unit_id, int administrative_region_id) {
        this.code = code;
        this.name = name;
        this.name_en = name_en;
        this.full_name = full_name;
        this.full_name_en = full_name_en;
        this.code_name = code_name;
        this.administrative_unit_id = administrative_unit_id;
        this.administrative_region_id = administrative_region_id;
    }

    // Getters and Setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getName_en() { return name_en; }
    public void setName_en(String name_en) { this.name_en = name_en; }

    public String getFull_name() { return full_name; }
    public void setFull_name(String full_name) { this.full_name = full_name; }

    public String getFull_name_en() { return full_name_en; }
    public void setFull_name_en(String full_name_en) { this.full_name_en = full_name_en; }

    public String getCode_name() { return code_name; }
    public void setCode_name(String code_name) { this.code_name = code_name; }

    public int getAdministrative_unit_id() { return administrative_unit_id; }
    public void setAdministrative_unit_id(int administrative_unit_id) { 
        this.administrative_unit_id = administrative_unit_id; 
    }

    public int getAdministrative_region_id() { return administrative_region_id; }
    public void setAdministrative_region_id(int administrative_region_id) { 
        this.administrative_region_id = administrative_region_id; 
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Override
    public String toString() {
        return name;
    }
} 