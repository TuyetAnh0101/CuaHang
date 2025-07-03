package com.example.cuahang.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class SystemConfig {

    private double vatPercent;           // Tỷ lệ VAT (ví dụ: 0.1 = 10%)
    private String defaultUnit;         // Đơn vị mặc định (ví dụ: "VNĐ", "USD")
    private int imageUploadLimitMB;     // Giới hạn dung lượng ảnh (MB)

    // Constructor rỗng cho Firestore
    public SystemConfig() {}

    // Constructor đầy đủ nếu bạn cần tạo nhanh đối tượng
    public SystemConfig(double vatPercent, String defaultUnit, int imageUploadLimitMB) {
        this.vatPercent = vatPercent;
        this.defaultUnit = defaultUnit;
        this.imageUploadLimitMB = imageUploadLimitMB;
    }

    // Getter & Setter
    public double getVatPercent() {
        return vatPercent;
    }

    public void setVatPercent(double vatPercent) {
        this.vatPercent = vatPercent;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public void setDefaultUnit(String defaultUnit) {
        this.defaultUnit = defaultUnit;
    }

    public int getImageUploadLimitMB() {
        return imageUploadLimitMB;
    }

    public void setImageUploadLimitMB(int imageUploadLimitMB) {
        this.imageUploadLimitMB = imageUploadLimitMB;
    }
}
