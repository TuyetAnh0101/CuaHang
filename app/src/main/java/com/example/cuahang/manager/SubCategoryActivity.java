package com.example.cuahang.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.SubCategoryAdapter;
import com.example.cuahang.model.Category;
import com.example.cuahang.model.SubCategory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SubCategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;
    private FirebaseFirestore db;
    private List<SubCategory> subCategoryList = new ArrayList<>();
    private SubCategoryAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();
    private ArrayAdapter<Category> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sub_category);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.subcategory), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerSubCategory);
        btnAdd = findViewById(R.id.fabAddSubCategory);
        db = FirebaseFirestore.getInstance();

        adapter = new SubCategoryAdapter(this, subCategoryList, new SubCategoryAdapter.OnSubCategoryClickListener() {
            @Override
            public void onEdit(SubCategory subCategory) {
                showEditDialog(subCategory);
            }

            @Override
            public void onDelete(SubCategory subCategory) {
                db.collection("SubCategory").document(subCategory.getId()).delete()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(SubCategoryActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                            loadData();
                        });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(subCategory -> {
            Intent intent = new Intent(SubCategoryActivity.this, PackageActivity.class);
            intent.putExtra("subCategoryId", subCategory.getId());
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v -> showAddDialog());

        loadCategories();
        loadData();
    }

    private void loadCategories() {
        db.collection("Category").get().addOnSuccessListener(snapshots -> {
            categoryList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                Category cat = doc.toObject(Category.class);
                categoryList.add(cat);
            }
        });
    }

    private void loadData() {
        db.collection("SubCategory").get().addOnSuccessListener(snapshots -> {
            subCategoryList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                SubCategory sub = doc.toObject(SubCategory.class);
                subCategoryList.add(sub);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm SubCategory");

        View view = LayoutInflater.from(this).inflate(R.layout.add_subcategory_layout, null);
        builder.setView(view);

        EditText edtName = view.findViewById(R.id.edtSubCategoryName);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
        spinnerCategory.setAdapter(categoryAdapter);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            Category selectedCat = (Category) spinnerCategory.getSelectedItem();

            if (name.isEmpty() || selectedCat == null) {
                Toast.makeText(this, "Vui lòng nhập tên và chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = generateNextId();
            SubCategory sub = new SubCategory(id, name, selectedCat.getId());
            db.collection("SubCategory").document(id).set(sub)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã thêm", Toast.LENGTH_SHORT).show();
                        loadData();
                    });
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showEditDialog(SubCategory sub) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa SubCategory");

        View view = LayoutInflater.from(this).inflate(R.layout.add_subcategory_layout, null);
        builder.setView(view);

        EditText edtName = view.findViewById(R.id.edtSubCategoryName);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
        spinnerCategory.setAdapter(categoryAdapter);

        edtName.setText(sub.getName());

        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId().equals(sub.getCategoryId())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            Category selectedCat = (Category) spinnerCategory.getSelectedItem();

            if (name.isEmpty() || selectedCat == null) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }

            sub.setName(name);
            sub.setCategoryId(selectedCat.getId());

            db.collection("SubCategory").document(sub.getId()).set(sub)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                        loadData();
                    });
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private String generateNextId() {
        int max = 0;
        for (SubCategory sub : subCategoryList) {
            try {
                int num = Integer.parseInt(sub.getId().replace("SC", ""));
                if (num > max) max = num;
            } catch (Exception ignored) {}
        }
        return String.format("SC%02d", max + 1);
    }
}
