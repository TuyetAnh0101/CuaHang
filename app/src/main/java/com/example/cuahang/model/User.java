package com.example.cuahang.model;

public class User {

    private String id;      // Thêm ID người dùng
    private String email;
    private String name;
    private String role;  // lưu dạng String

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

    // Các getter/setter sẵn có
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return Role.fromString(role);
    }

    public void setRole(Role role) {
        this.role = role.toStringValue();
    }
}
