package com.example.cuahang.model;

public class CartItem {
    private String packageId;
    private String packageName;
    private double packagePrice;
    private int soLuong;
    private String firestoreId;
    private double tax;
    private double discount;
    private double thanhTien;
    private String packageType;

    public CartItem() {
        // Constructor mặc định bắt buộc khi dùng Firebase Firestore
    }

    public CartItem(String packageId, String packageName, double packagePrice, int soLuong) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.packagePrice = packagePrice;
        this.soLuong = soLuong;
    }

    public CartItem(String packageId, String packageName, double packagePrice, int soLuong, String firestoreId) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.packagePrice = packagePrice;
        this.soLuong = soLuong;
        this.firestoreId = firestoreId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public double getPackagePrice() {
        return packagePrice;
    }

    public void setPackagePrice(double packagePrice) {
        this.packagePrice = packagePrice;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getFirestoreId() {
        return firestoreId;
    }

    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }

    public double getTotalPrice() {
        return packagePrice * soLuong;
    }
}
