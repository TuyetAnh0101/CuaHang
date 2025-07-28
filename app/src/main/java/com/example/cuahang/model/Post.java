package com.example.cuahang.model;
import java.util.List;

public class Post {
    private String id;               // Mã bài đăng
    private String idNguoiDung;      // UID người dùng
    private String idGoiTin;         // Mã gói tin sử dụng
    private String idDanhMuc;        // Mã danh mục
    private String tieuDe;           // Tiêu đề bài
    private String moTa;             // Mô tả ngắn
    private String noiDung;          // Nội dung chi tiết
    private List<String> listImageBase64;
    private long gia;                // Giá sản phẩm/dịch vụ
    private String ngayDang;         // Ngày đăng bài
    private String trangThai;        // VD: "chờ duyệt", "hiển thị"

    public Post() {
        // Firestore cần constructor rỗng
    }

    public Post(String id, String idNguoiDung, String idGoiTin, String idDanhMuc, String tieuDe,
                String moTa, String noiDung, List<String> listImageBase64, long gia, String ngayDang, String trangThai) {
        this.id = id;
        this.idNguoiDung = idNguoiDung;
        this.idGoiTin = idGoiTin;
        this.idDanhMuc = idDanhMuc;
        this.tieuDe = tieuDe;
        this.moTa = moTa;
        this.noiDung = noiDung;
        this.listImageBase64 = listImageBase64;
        this.gia = gia;
        this.ngayDang = ngayDang;
        this.trangThai = trangThai;
    }
    // Getters và Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdNguoiDung() { return idNguoiDung; }
    public void setIdNguoiDung(String idNguoiDung) { this.idNguoiDung = idNguoiDung; }

    public String getIdGoiTin() { return idGoiTin; }
    public void setIdGoiTin(String idGoiTin) { this.idGoiTin = idGoiTin; }

    public String getIdDanhMuc() { return idDanhMuc; }
    public void setIdDanhMuc(String idDanhMuc) { this.idDanhMuc = idDanhMuc; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public List<String> getListImageBase64() {
        return listImageBase64;
    }

    public void setListImageBase64(List<String> listImageBase64) {
        this.listImageBase64 = listImageBase64;
    }
    public long getGia() { return gia; }
    public void setGia(long gia) { this.gia = gia; }

    public String getNgayDang() { return ngayDang; }
    public void setNgayDang(String ngayDang) { this.ngayDang = ngayDang; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
