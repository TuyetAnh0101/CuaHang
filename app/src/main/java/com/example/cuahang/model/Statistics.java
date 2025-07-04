package com.example.cuahang.model;

import java.util.Map;

public class Statistics {
    private String date; // ví dụ: "2025-07-04"
    private int totalRevenue;
    private int totalOrders;
    private int packagesSold;
    private int newUsers;
    private Map<String, Integer> topCategories;

    public Statistics() {
        // Firebase cần constructor rỗng
    }

    public Statistics(String date, int totalRevenue, int totalOrders, int packagesSold, int newUsers, Map<String, Integer> topCategories) {
        this.date = date;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.packagesSold = packagesSold;
        this.newUsers = newUsers;
        this.topCategories = topCategories;
    }

    // Getters
    public String getDate() {
        return date;
    }

    public int getTotalRevenue() {
        return totalRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public int getPackagesSold() {
        return packagesSold;
    }

    public int getNewUsers() {
        return newUsers;
    }

    public Map<String, Integer> getTopCategories() {
        return topCategories;
    }

    // Setters
    public void setDate(String date) {
        this.date = date;
    }

    public void setTotalRevenue(int totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public void setPackagesSold(int packagesSold) {
        this.packagesSold = packagesSold;
    }

    public void setNewUsers(int newUsers) {
        this.newUsers = newUsers;
    }

    public void setTopCategories(Map<String, Integer> topCategories) {
        this.topCategories = topCategories;
    }
}
