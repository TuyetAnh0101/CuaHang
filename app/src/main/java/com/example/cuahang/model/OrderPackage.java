package com.example.cuahang.model;

public class OrderPackage {
    private String packageId;
    private String tenGoi;
    private double giaGiam;
    private int soLuong;
    private double thanhTien;

    public OrderPackage() {}

    public OrderPackage(String packageId, String tenGoi, double giaGiam, int soLuong) {
        this.packageId = packageId;
        this.tenGoi = tenGoi;
        this.giaGiam = giaGiam;
        this.soLuong = soLuong;
        this.thanhTien = giaGiam * soLuong;
    }

    // Getters & Setters
    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getTenGoi() {
        return tenGoi;
    }

    public void setTenGoi(String tenGoi) {
        this.tenGoi = tenGoi;
    }

    public double getGiaGiam() {
        return giaGiam;
    }

    public void setGiaGiam(double giaGiam) {
        this.giaGiam = giaGiam;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }
}
