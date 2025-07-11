package com.example.cuahang.model;

import java.util.Date;

public class UserStatistics {
    private String idThongKe;
    private Role role;               // Vai trò user: ADMIN, USER, STAFF
    private double tongDoanhThu;
    private int soDonHang;
    private int soGoiTin;
    private Date ngayThongKe;
    private String loaiThongKe;      // VD: Theo ngày, theo tháng...

    public UserStatistics() {
        this.role = Role.STAFF; // Mặc định
    }

    public UserStatistics(String idThongKe, Role role, double tongDoanhThu, int soDonHang, int soGoiTin, Date ngayThongKe, String loaiThongKe) {
        this.idThongKe = idThongKe;
        this.role = role;
        this.tongDoanhThu = tongDoanhThu;
        this.soDonHang = soDonHang;
        this.soGoiTin = soGoiTin;
        this.ngayThongKe = ngayThongKe;
        this.loaiThongKe = loaiThongKe;
    }

    public String getIdThongKe() {
        return idThongKe;
    }

    public void setIdThongKe(String idThongKe) {
        this.idThongKe = idThongKe;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public double getTongDoanhThu() {
        return tongDoanhThu;
    }

    public void setTongDoanhThu(double tongDoanhThu) {
        this.tongDoanhThu = tongDoanhThu;
    }

    public int getSoDonHang() {
        return soDonHang;
    }

    public void setSoDonHang(int soDonHang) {
        this.soDonHang = soDonHang;
    }

    public int getSoGoiTin() {
        return soGoiTin;
    }

    public void setSoGoiTin(int soGoiTin) {
        this.soGoiTin = soGoiTin;
    }

    public Date getNgayThongKe() {
        return ngayThongKe;
    }

    public void setNgayThongKe(Date ngayThongKe) {
        this.ngayThongKe = ngayThongKe;
    }

    public String getLoaiThongKe() {
        return loaiThongKe;
    }

    public void setLoaiThongKe(String loaiThongKe) {
        this.loaiThongKe = loaiThongKe;
    }

    // Phương thức tính tổng doanh thu (ví dụ placeholder)
    public void tinhTongDoanhThu(double giaTriMoi) {
        this.tongDoanhThu += giaTriMoi;
    }

    // Phương thức tính tổng số đơn hàng
    public void tinhSoDonHang() {
        this.soDonHang++;
    }

    // Phương thức tính tổng số gói tin
    public void tinhSoGoiTin() {
        this.soGoiTin++;
    }

    // Phương thức cập nhật thống kê tổng hợp lại
    public void capNhatThongKe(double giaTriMoi) {
        tinhTongDoanhThu(giaTriMoi);
        tinhSoDonHang();
        tinhSoGoiTin();
    }

    // Placeholder cho việc vẽ biểu đồ (logic sẽ xử lý ở Activity/Fragment)
    public void veBieuDo() {
        // Tuỳ vào thư viện, code sẽ viết ở phần xử lý giao diện
    }
}
