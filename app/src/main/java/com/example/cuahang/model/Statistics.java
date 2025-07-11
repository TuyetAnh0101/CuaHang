package com.example.cuahang.model;

import java.util.HashMap;
import java.util.Map;

public class Statistics {
    private String date; // ví dụ: "2025-07-04"
    private int totalRevenue;
    private int totalOrders;
    private int packagesSold;
    private int newUsers;
    private Map<String, Integer> topCategories;
    private Map<String, Integer> topPostTypes;

    public Statistics() {
        // Firebase cần constructor rỗng
    }

    public Statistics(String date, int totalRevenue, int totalOrders, int packagesSold, int newUsers,
                      Map<String, Integer> topCategories, Map<String, Integer> topPostTypes) {
        this.date = date;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.packagesSold = packagesSold;
        this.newUsers = newUsers;
        this.topCategories = topCategories;
        this.topPostTypes = topPostTypes;
    }

    // ✅ Constructor thêm để fix lỗi "Cannot resolve constructor 'Statistics(String)'"
    public Statistics(String date) {
        this.date = date;
        this.totalRevenue = 0;
        this.totalOrders = 0;
        this.packagesSold = 0;
        this.newUsers = 0;
        this.topCategories = new HashMap<>();
        this.topPostTypes = new HashMap<>();
    }

    // Getters và Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(int totalRevenue) { this.totalRevenue = totalRevenue; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public int getPackagesSold() { return packagesSold; }
    public void setPackagesSold(int packagesSold) { this.packagesSold = packagesSold; }

    public int getNewUsers() { return newUsers; }
    public void setNewUsers(int newUsers) { this.newUsers = newUsers; }

    public Map<String, Integer> getTopCategories() { return topCategories; }
    public void setTopCategories(Map<String, Integer> topCategories) { this.topCategories = topCategories; }

    public Map<String, Integer> getTopPostTypes() { return topPostTypes; }
    public void setTopPostTypes(Map<String, Integer> topPostTypes) { this.topPostTypes = topPostTypes; }
}
