package com.example.cuahang.model;

public class SystemConfig {
    private double vatPercent;
    private String defaultUnit;
    private int imageUploadLimitMB;

    public SystemConfig() {}

    public double getVatPercent() { return vatPercent; }
    public void setVatPercent(double vatPercent) { this.vatPercent = vatPercent; }

    public String getDefaultUnit() { return defaultUnit; }
    public void setDefaultUnit(String defaultUnit) { this.defaultUnit = defaultUnit; }

    public int getImageUploadLimitMB() { return imageUploadLimitMB; }
    public void setImageUploadLimitMB(int imageUploadLimitMB) { this.imageUploadLimitMB = imageUploadLimitMB; }
}
