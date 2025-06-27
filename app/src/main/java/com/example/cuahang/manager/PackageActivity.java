package com.example.cuahang.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.ImageAdapter;
import com.example.cuahang.adapter.PackageAdapter;
import com.example.cuahang.model.Category;
import com.example.cuahang.model.Package;
import com.example.cuahang.model.SubCategory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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

    private List<Uri> selectedImageUris = new ArrayList<>();

    private double defaultVat = 0.0;
    private String defaultUnit = "";
    private long imageUploadLimitMB = 2; // mặc định 2MB nếu chưa có trong Firebase


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);

        recyclerView = findViewById(R.id.recyclerPackage);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnAddPackage = findViewById(R.id.btnAddPackage);
        btnAddPackage.setEnabled(false); // Disable trước khi load config

        db = FirebaseFirestore.getInstance();

        adapter = new PackageAdapter(this, packageList, new PackageAdapter.OnPackageClickListener() {
            @Override public void onEdit(Package pkg) { showEditPackageDialog(pkg); }
            @Override public void onDelete(Package pkg) { deletePackage(pkg); }
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
        loadSystemConfig();
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    List<Uri> uriList = new ArrayList<>();
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            if (checkImageSize(imageUri)) uriList.add(imageUri);
                        }
                    } else {
                        Uri imageUri = result.getData().getData();
                        if (checkImageSize(imageUri)) uriList.add(imageUri);
                    }
                    selectedImageUris = uriList;
                }
            }
    );

    private boolean checkImageSize(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                int sizeInBytes = inputStream.available();
                long sizeInMB = sizeInBytes / (1024 * 1024);
                if (sizeInMB > imageUploadLimitMB) {
                    Toast.makeText(this, "Ảnh vượt quá giới hạn " + imageUploadLimitMB + "MB", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void showAddPackageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        ImageView imgPreview = view.findViewById(R.id.imgPreview);
        TextView btnChooseImage = view.findViewById(R.id.btnChooseImage);

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<SubCategory> subCategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredSubCategories);
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubCategory.setAdapter(subCategoryAdapter);

        edtVAT.setText(String.valueOf(defaultVat));
        edtUnit.setText(defaultUnit);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Category selected = categoryList.get(pos);
                filterSubCategoriesByCategory(selected.getId());
                subCategoryAdapter.notifyDataSetChanged();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(intent);
        });

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
            SubCategory selectedSubCategory = (SubCategory) spinnerSubCategory.getSelectedItem();

            String id = generateNextId();
            Package pkg = new Package();
            pkg.setId(id);
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

            if (!selectedImageUris.isEmpty()) {
                List<String> base64Images = new ArrayList<>();
                for (Uri uri : selectedImageUris) {
                    String base64 = encodeImageToBase64(this, uri);
                    if (base64 != null) base64Images.add(base64);
                }
                pkg.setHinhAnh(base64Images);
                imgPreview.setImageURI(selectedImageUris.get(0));
                selectedImageUris.clear(); // Reset sau khi thêm
            }

            savePackage(pkg);
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showEditPackageDialog(Package pkg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.add_package_layout, null);
        builder.setView(view);
        builder.setTitle("Chỉnh sửa gói");

        // Khai báo view
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
        TextView btnChooseImage = view.findViewById(R.id.btnChooseImage);
        RecyclerView recyclerImages = view.findViewById(R.id.recyclerImages);

        // Set dữ liệu
        edtPackageName.setText(pkg.getTenGoi());
        edtPackageDesc.setText(pkg.getMoTa());
        edtPrice.setText(String.valueOf(pkg.getGiaGoc()));
        edtDiscount.setText(String.valueOf(pkg.getGiaGiam()));
        edtVAT.setText(String.valueOf(pkg.getVat()));
        edtUnit.setText(pkg.getDonViTinh());
        edtQuantity.setText(String.valueOf(pkg.getSoLuong()));
        edtStatus.setText(pkg.getStatus());
        edtNote.setText(pkg.getNote());

        // Setup spinner danh mục
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<SubCategory> subCategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredSubCategories);
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubCategory.setAdapter(subCategoryAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedId = categoryList.get(position).getId();
                filterSubCategoriesByCategory(selectedId);
                subCategoryAdapter.notifyDataSetChanged();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Chọn đúng vị trí
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId().equals(pkg.getCategoryId())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
        filterSubCategoriesByCategory(pkg.getCategoryId());
        for (int i = 0; i < filteredSubCategories.size(); i++) {
            if (filteredSubCategories.get(i).getId().equals(pkg.getSubcategoryId())) {
                spinnerSubCategory.setSelection(i);
                break;
            }
        }

        // Hiển thị ảnh đã có
        List<String> currentImages = pkg.getHinhAnh() != null ? pkg.getHinhAnh() : new ArrayList<>();
        ImageAdapter imageAdapter = new ImageAdapter(this, currentImages);
        recyclerImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerImages.setAdapter(imageAdapter);

        // Chọn ảnh mới
        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(intent);
        });

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
            SubCategory selectedSubCategory = (SubCategory) spinnerSubCategory.getSelectedItem();

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

            // Nếu người dùng chọn ảnh mới thì cập nhật
            if (!selectedImageUris.isEmpty()) {
                List<String> base64Images = new ArrayList<>();
                for (Uri uri : selectedImageUris) {
                    String base64 = encodeImageToBase64(this, uri);
                    if (base64 != null) base64Images.add(base64);
                }
                pkg.setHinhAnh(base64Images);
            }

            db.collection("Package").document(pkg.getId())
                    .set(pkg)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                        reload();
                    });
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private String encodeImageToBase64(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    private void savePackage(Package pkg) {
        db.collection("Package").document(pkg.getId())
                .set(pkg)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã thêm", Toast.LENGTH_SHORT).show();
                    reload();
                });
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
    private void loadSystemConfig() {
        FirebaseFirestore.getInstance()
                .collection("SystemConfig")
                .document("default")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Double vat = snapshot.getDouble("vatPercent");
                        String unit = snapshot.getString("defaultUnit");
                        Long limitMB = snapshot.getLong("imageUploadLimitMB");

                        if (vat != null) defaultVat = vat;
                        if (unit != null) defaultUnit = unit;
                        if (limitMB != null) imageUploadLimitMB = limitMB;
                    }
                    btnAddPackage.setEnabled(true);
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