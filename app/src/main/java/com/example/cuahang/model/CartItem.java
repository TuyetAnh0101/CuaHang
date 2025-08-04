package com.example.cuahang.model;

public class CartItem {

    private String packageId;
    private String packageName;
    private String packageType;     // Ví dụ: "Tin VIP"
    private double originalPrice;   // Giá gốc (giaGoc)
    private double packagePrice;    // Giá bán thực tế (giaGiam)
    private int soLuong;
    private double tax;             // % VAT
    private double thanhTien;
    private String firestoreId;

    // Firestore requires default constructor
    public CartItem() {}

    // Constructor đầy đủ thông tin
    public CartItem(String packageId, String packageName, String packageType,
                    double originalPrice, double packagePrice, int soLuong, double tax) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.packageType = packageType;
        this.originalPrice = originalPrice;
        this.packagePrice = packagePrice;
        this.soLuong = soLuong;
        this.tax = tax;
        recalculateThanhTien();
    }

    // Constructor tạo từ một Package object
    public CartItem(Package pkg, int soLuong) {
        this.packageId = pkg.getId();
        this.packageName = pkg.getTenGoi();
        this.packageType = pkg.getPackageType();
        this.originalPrice = pkg.getGiaGoc();
        this.packagePrice = pkg.getGiaGiam();
        this.tax = pkg.getVat(); // Đồng bộ VAT từ Package
        this.soLuong = soLuong;
        recalculateThanhTien();
    }

    // Tính lại thành tiền khi có thay đổi về số lượng, giá, hoặc thuế
    public void recalculateThanhTien() {
        double total = packagePrice * soLuong;
        this.thanhTien = total + (total * tax / 100.0);
    }

    // ========== Getter & Setter ==========

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
        recalculateThanhTien();
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        recalculateThanhTien();
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
        recalculateThanhTien();
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
