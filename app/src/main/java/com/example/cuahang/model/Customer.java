package com.example.cuahang.model;

public class Customer {
    private String id;       // Firestore document ID
    private String name;     // Tên khách hàng
    private String phone;    // Số điện thoại
    private String address;  // Địa chỉ

    public Customer() {
        // Bắt buộc với Firebase
    }

    public Customer(String id, String name, String phone, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
