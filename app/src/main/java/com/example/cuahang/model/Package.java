package com.example.cuahang.model;

import java.util.List;

public class Package {
    private String id;
    private String tenGoi;
    private String moTa;
    private String categoryId;
    private String subcategoryId;
    private String donViTinh;
    private double giaGoc;
    private double giaGiam;
    private double vat;
    private List<String> hinhAnh; // Danh sách ảnh (URL)
    private String status;
    private String note;
    private int soLuong;

    public Package() {
        // Constructor rỗng cần cho Firestore
    }

    public Package(String id, String tenGoi, String moTa, String categoryId, String subcategoryId,
                   String donViTinh, double giaGoc, double giaGiam, double vat,
                   List<String> hinhAnh, String status, String note, int soLuong) {
        this.id = id;
        this.tenGoi = tenGoi;
        this.moTa = moTa;
        this.categoryId = categoryId;
        this.subcategoryId = subcategoryId;
        this.donViTinh = donViTinh;
        this.giaGoc = giaGoc;
        this.giaGiam = giaGiam;
        this.vat = vat;
        this.hinhAnh = hinhAnh;
        this.status = status;
        this.note = note;
        this.soLuong = soLuong;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenGoi() {
        return tenGoi;
    }

    public void setTenGoi(String tenGoi) {
        this.tenGoi = tenGoi;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(String subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public double getGiaGoc() {
        return giaGoc;
    }

    public void setGiaGoc(double giaGoc) {
        this.giaGoc = giaGoc;
    }

    public double getGiaGiam() {
        return giaGiam;
    }

    public void setGiaGiam(double giaGiam) {
        this.giaGiam = giaGiam;
    }

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

    public List<String> getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(List<String> hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }
}
