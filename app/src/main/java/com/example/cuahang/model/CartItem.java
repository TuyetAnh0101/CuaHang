package com.example.cuahang.model;

public class CartItem {
    private String packageId;
    private String packageName;
    private String packageType;     // Ví dụ: "Tin VIP"
    private double originalPrice;   // giá gốc (giaGoc)
    private double packagePrice;    // giá sau giảm (giaGiam) - là giá bán thực tế
    private int soLuong;
    private double tax;             // % VAT
    private double thanhTien;
    private String firestoreId;

    public CartItem() {
        // Firestore requires default constructor
    }

    public CartItem(String packageId, String packageName, String packageType,
                    double originalPrice, double packagePrice, int soLuong, double tax) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.packageType = packageType;
        this.originalPrice = originalPrice;
        this.packagePrice = packagePrice;
        this.soLuong = soLuong;
        this.tax = tax;
        this.thanhTien = calculateThanhTien();
    }

    private double calculateThanhTien() {
        double total = packagePrice * soLuong;
        return total + (total * tax / 100.0);
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

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getPackagePrice() {
        return packagePrice;
    }

    public void setPackagePrice(double packagePrice) {
        this.packagePrice = packagePrice;
        this.thanhTien = calculateThanhTien();
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        this.thanhTien = calculateThanhTien();
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
        this.thanhTien = calculateThanhTien();
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public String getFirestoreId() {
        return firestoreId;
    }

    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }

    public double getTotalBeforeTax() {
        return packagePrice * soLuong;
    }
}
