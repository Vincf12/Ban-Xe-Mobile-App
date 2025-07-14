package com.example.carsale.Model;

import java.util.List;

public class BaseResponse<T> {
    private int total;
    private List<T> data;
    private String code;
    private String message;

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
} 