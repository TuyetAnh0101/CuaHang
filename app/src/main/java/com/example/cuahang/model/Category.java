package com.example.cuahang.model;

public class Category {
    private String id;
    private String name;


    public Category() {
        // Constructor rỗng bắt buộc cho Firestore
    }

    public Category(String all, String id, String name) {
        this.id = id;
        this.name = name;

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

    @Override
    public String toString() {
        return name; // Dễ hiển thị trong Spinner hoặc Log
    }
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category c = (Category) o;
        return id.equals(c.id);
    }
}
