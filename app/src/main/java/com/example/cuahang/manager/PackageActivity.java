package com.example.cuahang.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.PackageAdapter;
import com.example.cuahang.model.Category;
import com.example.cuahang.model.Package;
import com.example.cuahang.model.SubCategory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PackageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtEmpty;
    private FloatingActionButton btnAddPackage;

    private List<Package> packageList = new ArrayList<>();
    private PackageAdapter adapter;
    private FirebaseFirestore db;

    private String currentCategoryId;
    private List<Category> categoryList = new ArrayList<>();
    private List<SubCategory> allSubCategories = new ArrayList<>();
    private List<SubCategory> filteredSubCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);

        recyclerView = findViewById(R.id.recyclerPackage);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnAddPackage = findViewById(R.id.btnAddPackage);

        db = FirebaseFirestore.getInstance();

        adapter = new PackageAdapter(this, packageList, new PackageAdapter.OnPackageClickListener() {
            @Override
            public void onEdit(Package pkg) {
                // TODO: Implement edit dialog with spinner support
            }

            @Override
            public void onDelete(Package pkg) {
                deletePackage(pkg);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        currentCategoryId = getIntent().getStringExtra("categoryId");
        btnAddPackage.setOnClickListener(v -> showAddPackageDialog());

        if (currentCategoryId != null) {
            loadPackagesByCategory(currentCategoryId);
        } else {
            loadAllPackages();
        }

        loadCategories();
        loadSubCategories();
    }

    private void loadPackagesByCategory(String categoryId) {
        db.collection("Package")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    packageList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Package pkg = doc.toObject(Package.class);
                        packageList.add(pkg);
                    }
                    adapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(packageList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void loadAllPackages() {
        db.collection("Package")
                .get()
                .addOnSuccessListener(snapshots -> {
                    packageList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Package pkg = doc.toObject(Package.class);
                        packageList.add(pkg);
                    }
                    adapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(packageList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void showAddPackageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm gói đăng ký");

        View view = getLayoutInflater().inflate(R.layout.add_package_layout, null);
        builder.setView(view);

        EditText edtPackageName = view.findViewById(R.id.edtPackageName);
        EditText edtPackageDesc = view.findViewById(R.id.edtPackageDesc);
        EditText edtPrice = view.findViewById(R.id.edtPrice);
        EditText edtDiscount = view.findViewById(R.id.edtDiscount);
        EditText edtVAT = view.findViewById(R.id.edtVAT);
        EditText edtUnit = view.findViewById(R.id.edtUnit);
        EditText edtQuantity = view.findViewById(R.id.edtQuantity);
        EditText edtStatus = view.findViewById(R.id.edtStatus);
        EditText edtNote = view.findViewById(R.id.edtNote);

        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Spinner spinnerSubCategory = view.findViewById(R.id.spinnerSubCategory);

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<SubCategory> subCategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredSubCategories);
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubCategory.setAdapter(subCategoryAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category selected = categoryList.get(position);
                filterSubCategoriesByCategory(selected.getId());
                subCategoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            try {
                Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
                SubCategory selectedSubCategory = (SubCategory) spinnerSubCategory.getSelectedItem();

                Package pkg = new Package();
                pkg.setId(generateNextId());
                pkg.setTenGoi(edtPackageName.getText().toString().trim());
                pkg.setMoTa(edtPackageDesc.getText().toString().trim());
                pkg.setGiaGoc(Double.parseDouble(edtPrice.getText().toString().trim()));
                pkg.setGiaGiam(Double.parseDouble(edtDiscount.getText().toString().trim()));
                pkg.setVat(Double.parseDouble(edtVAT.getText().toString().trim()));
                pkg.setDonViTinh(edtUnit.getText().toString().trim());
                pkg.setSoLuong(Integer.parseInt(edtQuantity.getText().toString().trim()));
                pkg.setStatus(edtStatus.getText().toString().trim());
                pkg.setNote(edtNote.getText().toString().trim());
                pkg.setCategoryId(selectedCategory != null ? selectedCategory.getId() : "");
                pkg.setSubcategoryId(selectedSubCategory != null ? selectedSubCategory.getId() : "");

                db.collection("Package").document(pkg.getId())
                        .set(pkg)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã thêm", Toast.LENGTH_SHORT).show();
                            reload();
                        });
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void reload() {
        if (currentCategoryId != null) {
            loadPackagesByCategory(currentCategoryId);
        } else {
            loadAllPackages();
        }
    }

    private void deletePackage(Package pkg) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa gói")
                .setMessage("Bạn có chắc muốn xóa '" + pkg.getTenGoi() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("Package").document(pkg.getId())
                            .delete()
                            .addOnSuccessListener(unused -> reload());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void filterSubCategoriesByCategory(String categoryId) {
        filteredSubCategories.clear();
        for (SubCategory sub : allSubCategories) {
            if (sub.getCategoryId().equals(categoryId)) {
                filteredSubCategories.add(sub);
            }
        }
    }

    private void loadCategories() {
        db.collection("Category").get().addOnSuccessListener(snapshot -> {
            categoryList.clear();
            for (QueryDocumentSnapshot doc : snapshot) {
                Category cat = doc.toObject(Category.class);
                categoryList.add(cat);
            }
        });
    }

    private void loadSubCategories() {
        db.collection("SubCategory").get().addOnSuccessListener(snapshot -> {
            allSubCategories.clear();
            for (QueryDocumentSnapshot doc : snapshot) {
                SubCategory sub = doc.toObject(SubCategory.class);
                allSubCategories.add(sub);
            }
        });
    }

    private String generateNextId() {
        int max = 0;
        for (Package pkg : packageList) {
            try {
                int num = Integer.parseInt(pkg.getId().replace("PK", ""));
                if (num > max) max = num;
            } catch (Exception ignored) {}
        }
        return String.format("PK%02d", max + 1);
    }
}
