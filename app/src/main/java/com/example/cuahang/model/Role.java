package com.example.cuahang.model;

public enum Role {
    ADMIN,
    USER,
    STAFF;

    public static Role fromString(String roleStr) {
        if (roleStr == null) return STAFF;
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return STAFF;
        }
    }
}
