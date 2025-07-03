package com.example.cuahang.model;

public class User {

    private String id;      // Firebase UID
    private String email;
    private String name;
    private String role;  // "admin", "manager", "staff"
    private boolean active = true; // đề xuất thêm trạng thái hoạt động

    public User() {
    }

    public User(String email, String name, Role role) {
        this.email = email;
        this.name = name;
        this.role = role.toStringValue();
    }

    // Getter & Setter cho ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter & Setter cho email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter & Setter cho name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter & Setter cho role
    public Role getRole() {
        return Role.fromString(role);
    }

    public void setRole(Role role) {
        this.role = role.toStringValue();
    }

    // Getter & Setter cho active
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
