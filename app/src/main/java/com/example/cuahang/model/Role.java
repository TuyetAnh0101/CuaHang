package com.example.cuahang.model;

public enum Role {
    ADMIN,
    MANAGER,
    STAFF;

    // Chuyển từ String (Firestore) thành Role enum, có xử lý mặc định an toàn
    public static Role fromString(String roleStr) {
        if (roleStr == null) return STAFF; // hoặc mặc định null, hoặc throw
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return STAFF; // hoặc mặc định khác nếu muốn
        }
    }

    // Chuyển từ Role enum sang String lưu Firestore
    public String toStringValue() {
        return this.name();
    }
}
