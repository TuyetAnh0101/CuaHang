package com.example.cuahang.model;

public enum Role {
    ADMIN,
    MANAGER,
    STAFF;

    // Chuyển từ String (Firestore) thành Role enum
    public static Role fromString(String roleStr) {
        if (roleStr == null) return STAFF;
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return STAFF;
        }
    }

    // Chuyển từ enum về String để lưu Firestore
    public String toStringValue() {
        return this.name();
    }
}
