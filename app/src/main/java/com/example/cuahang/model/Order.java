package com.example.cuahang.model;

import java.util.List;

public class Order {
    private String id;
    private String idKhach;
    private String ngayDat;
    private long tongTien;
    private long tongSoLuong;
    private String statusXuLy;
    private String statusThanhToan;
    private String note;
    private List<OrderPackage> packages; // Danh sách các gói trong đơn hàng

    public Order() {}

    public Order(String id, String idKhach, String ngayDat, long tongTien, long tongSoLuong,
                 String statusXuLy, String statusThanhToan, String note, List<OrderPackage> packages) {
        this.id = id;
        this.idKhach = idKhach;
        this.ngayDat = ngayDat;
        this.tongTien = tongTien;
        this.tongSoLuong = tongSoLuong;
        this.statusXuLy = statusXuLy;
        this.statusThanhToan = statusThanhToan;
        this.note = note;
        this.packages = packages;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdKhach() { return idKhach; }
    public void setIdKhach(String idKhach) { this.idKhach = idKhach; }

    public String getNgayDat() { return ngayDat; }
    public void setNgayDat(String ngayDat) { this.ngayDat = ngayDat; }

    public long getTongTien() { return tongTien; }
    public void setTongTien(long tongTien) { this.tongTien = tongTien; }

    public long getTongSoLuong() { return tongSoLuong; }
    public void setTongSoLuong(long tongSoLuong) { this.tongSoLuong = tongSoLuong; }

    public String getStatusXuLy() { return statusXuLy; }
    public void setStatusXuLy(String statusXuLy) { this.statusXuLy = statusXuLy; }

    public String getStatusThanhToan() { return statusThanhToan; }
    public void setStatusThanhToan(String statusThanhToan) { this.statusThanhToan = statusThanhToan; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<OrderPackage> getPackages() { return packages; }
    public void setPackages(List<OrderPackage> packages) { this.packages = packages; }
}
