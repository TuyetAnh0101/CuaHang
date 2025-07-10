package com.example.cuahang.model;

import java.util.Map;

public class Statistics {
    private String date; // ví dụ: "2025-07-04"
    private int totalRevenue;
    private int totalOrders;
    private int packagesSold;
    private int newUsers;
    private Map<String, Integer> topCategories;
    private Map<String, Integer> topPostTypes; // thêm trường này để thống kê Loại tin

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

    // getters và setters cho tất cả trường

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
