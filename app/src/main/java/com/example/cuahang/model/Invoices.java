package com.example.cuahang.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

import java.util.Date;

@IgnoreExtraProperties
public class Invoices {

    private String id;              // Mã hóa đơn (Firestore document ID)
    private Date createdAt;         // Ngày giờ tạo (Firestore field: dateTime)
    private double totalAmount;     // Tổng tiền thanh toán cuối cùng
    private String createdBy;       // Nhân viên tạo (UID hoặc tên)
    private int totalQuantity;      // Tổng số lượng sản phẩm
    private double totalPrice;      // Tổng giá trước thuế/giảm giá
    private double totalTax;        // Tổng thuế (Firestore field: tax)
    private double totalDiscount;   // Tổng giảm giá
    private String status;          // Trạng thái hóa đơn (Đã thanh toán/Chưa thanh toán)
    private String userId;

    public Invoices() {
        // Firestore yêu cầu constructor rỗng
    }

    public Invoices(String id, Date createdAt, double totalAmount, String createdBy,
                    int totalQuantity, double totalPrice, double totalTax, double totalDiscount) {
        this.id = id;
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
        this.createdBy = createdBy;
        this.totalQuantity = totalQuantity;
        this.totalPrice = totalPrice;
        this.totalTax = totalTax;
        this.totalDiscount = totalDiscount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("dateTime")
    public Date getCreatedAt() {
        return createdAt;
    }

    @PropertyName("dateTime")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @PropertyName("tax")
    public double getTotalTax() {
        return totalTax;
    }

    @PropertyName("tax")
    public void setTotalTax(double totalTax) {
        this.totalTax = totalTax;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
