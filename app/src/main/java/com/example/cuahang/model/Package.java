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
    private List<String> hinhAnh;
    private String status;
    private String note;
    private int soLuong;
    private List<Package> packages;

    private boolean isFree3Posts;
    private String billingCycle;
    private int maxPosts;
    private int maxCharacters;
    private int maxImages;
    private String startDate;
    private String endDate;
    private boolean isSelected;
    private String packageType;

    private double thanhTien; // Thêm trường này để đồng bộ với Firestore

    public Package() {}

    public Package(String id, String tenGoi, String moTa, String categoryId, String subcategoryId,
                   String donViTinh, double giaGoc, double giaGiam, double vat,
                   List<String> hinhAnh, String status, String note, int soLuong,
                   boolean isFree3Posts, String billingCycle, int maxPosts,
                   int maxCharacters, int maxImages, String startDate, String endDate,
                   String packageType, double thanhTien) {
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
        this.isFree3Posts = isFree3Posts;
        this.billingCycle = billingCycle;
        this.maxPosts = maxPosts;
        this.maxCharacters = maxCharacters;
        this.maxImages = maxImages;
        this.startDate = startDate;
        this.endDate = endDate;
        this.packageType = packageType;
        this.thanhTien = thanhTien;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenGoi() { return tenGoi; }
    public void setTenGoi(String tenGoi) { this.tenGoi = tenGoi; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getSubcategoryId() { return subcategoryId; }
    public void setSubcategoryId(String subcategoryId) { this.subcategoryId = subcategoryId; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public double getGiaGoc() { return giaGoc; }
    public void setGiaGoc(double giaGoc) { this.giaGoc = giaGoc; }

    public double getGiaGiam() { return giaGiam; }
    public void setGiaGiam(double giaGiam) { this.giaGiam = giaGiam; }

    public double getVat() { return vat; }
    public void setVat(double vat) { this.vat = vat; }

    public List<String> getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(List<String> hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public boolean isFree3Posts() { return isFree3Posts; }
    public void setFree3Posts(boolean free3Posts) { isFree3Posts = free3Posts; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public int getMaxPosts() { return maxPosts; }
    public void setMaxPosts(int maxPosts) { this.maxPosts = maxPosts; }

    public int getMaxCharacters() { return maxCharacters; }
    public void setMaxCharacters(int maxCharacters) { this.maxCharacters = maxCharacters; }

    public int getMaxImages() { return maxImages; }
    public void setMaxImages(int maxImages) { this.maxImages = maxImages; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    public double tinhThanhTien() {
        return giaGiam * soLuong;
    }
    public List<Package> getPackages() {
        return packages;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    public enum BillingCycle {
        DAY(1),
        MONTH(2),
        YEAR(3);

        private final int value;

        BillingCycle(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static BillingCycle fromInt(int val) {
            for (BillingCycle cycle : BillingCycle.values()) {
                if (cycle.getValue() == val) return cycle;
            }
            return null;
        }
    }
}
