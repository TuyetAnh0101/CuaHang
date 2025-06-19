package com.example.cuahang.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.CategoryAdapter;
import com.example.cuahang.model.Category;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView rcvCategory;
    private CategoryAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private View btnAddCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        rcvCategory = findViewById(R.id.rcvCategory);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        adapter = new CategoryAdapter(this, categoryList, this::showEditCategoryDialog, this::deleteCategory);
        rcvCategory.setLayoutManager(new LinearLayoutManager(this));
        rcvCategory.setAdapter(adapter);

        loadCategories();

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        db.collection("Category")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    categoryList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Category category = doc.toObject(Category.class);
                        categoryList.add(category);
                    }

                    // Sắp xếp theo ID để sinh tiếp đúng thứ tự
                    Collections.sort(categoryList, Comparator.comparing(Category::getId));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm danh mục");

        View dialogView = getLayoutInflater().inflate(R.layout.add_category_layout, null);
        EditText edtCategoryName = dialogView.findViewById(R.id.edtCategoryName);
        builder.setView(dialogView);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String name = edtCategoryName.getText().toString().trim();
            if (!name.isEmpty()) {
                String id = generateNextCategoryId();
                Category category = new Category(id, name);
                db.collection("Category").document(id)
                        .set(category)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã thêm danh mục", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEditCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa danh mục");

        View dialogView = getLayoutInflater().inflate(R.layout.add_category_layout, null);
        EditText edtCategoryName = dialogView.findViewById(R.id.edtCategoryName);
        edtCategoryName.setText(category.getName());
        builder.setView(dialogView);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = edtCategoryName.getText().toString().trim();
            if (!newName.isEmpty()) {
                category.setName(newName);
                db.collection("Category").document(category.getId())
                        .set(category)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteCategory(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa '" + category.getName() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("Category").document(category.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                loadCategories(); // ← Tải lại danh sách sau khi xóa
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String generateNextCategoryId() {
        int maxId = 0;
        for (Category cat : categoryList) {
            try {
                int num = Integer.parseInt(cat.getId().replace("CT", ""));
                if (num > maxId) maxId = num;
            } catch (Exception ignored) {}
        }
        return String.format("CT%02d", maxId + 1);
    }
}
