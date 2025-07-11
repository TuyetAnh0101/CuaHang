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
import android.widget.CheckBox;
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
import com.example.cuahang.adapter.PackageAdapter;
import com.example.cuahang.model.Category;
import com.example.cuahang.model.Order;
import com.example.cuahang.model.OrderPackage;
import com.example.cuahang.model.Package;
import com.example.cuahang.model.SubCategory;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PackageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtEmpty;
    private View btnAddPackage;

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
    private long imageUploadLimitMB = 2; // Mặc định 2MB

    private boolean categoriesLoaded = false;
    private boolean subCategoriesLoaded = false;
    private boolean systemConfigLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);

        recyclerView = findViewById(R.id.recyclerPackage);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnAddPackage = findViewById(R.id.btnAddPackage);
        btnAddPackage.setEnabled(false);

        db = FirebaseFirestore.getInstance();

        adapter = new PackageAdapter(this, packageList, new PackageAdapter.OnPackageClickListener() {
            @Override
            public void onEdit(Package pkg) { showEditPackageDialog(pkg); }
            @Override
            public void onDelete(Package pkg) { deletePackage(pkg); }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        currentCategoryId = getIntent().getStringExtra("categoryId");

        btnAddPackage.setOnClickListener(v -> showAddPackageDialog());

        loadCategories();
        loadSubCategories();
        loadSystemConfig();

        if (currentCategoryId != null) {
            loadPackagesByCategory(currentCategoryId);
        } else {
            loadAllPackages();
        }
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
                    if (!uriList.isEmpty()) {
                        selectedImageUris = uriList;
                    } else {
                        Toast.makeText(this, "Không có ảnh hợp lệ được chọn", Toast.LENGTH_SHORT).show();
                    }
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
            Toast.makeText(this, "Lỗi đọc ảnh", Toast.LENGTH_SHORT).show();
            return false;
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
        CheckBox checkboxFree3Posts = view.findViewById(R.id.checkboxIsFree3Post);
        Spinner spinnerBillingCycle = view.findViewById(R.id.spinnerBillingCycle);
        EditText edtMaxPosts = view.findViewById(R.id.edtMaxPosts);
        EditText edtMaxCharacters = view.findViewById(R.id.edtMaxCharacters);
        EditText edtMaxImages = view.findViewById(R.id.edtMaxImages);
        EditText edtStartDate = view.findViewById(R.id.edtStartDate);
        EditText edtEndDate = view.findViewById(R.id.edtEndDate);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Spinner spinnerSubCategory = view.findViewById(R.id.spinnerSubCategory);
        ImageView imgPreview = view.findViewById(R.id.imgPreview);
        TextView btnChooseImage = view.findViewById(R.id.btnChooseImage);
        Spinner spinnerPackageType = view.findViewById(R.id.spinnerPackageType);

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        filteredSubCategories.clear();
        ArrayAdapter<SubCategory> subCategoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filteredSubCategories);
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubCategory.setAdapter(subCategoryAdapter);

        String[] billingCycles = {"Theo tuần", "Theo tháng", "Theo quý", "Theo năm"};
        ArrayAdapter<String> billingCycleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, billingCycles);
        billingCycleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBillingCycle.setAdapter(billingCycleAdapter);

        ArrayAdapter<String> packageTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Gói thường", "VIP", "VVIP"});
        packageTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPackageType.setAdapter(packageTypeAdapter);

        edtVAT.setText(String.valueOf(defaultVat));
        edtUnit.setText(defaultUnit);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category selectedCat = categoryList.get(position);
                filterSubCategoriesByCategory(selectedCat.getId());
                subCategoryAdapter.notifyDataSetChanged();
                if (!filteredSubCategories.isEmpty()) spinnerSubCategory.setSelection(0);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(intent);
        });

        imgPreview.setImageResource(R.drawable.user);
        if (!selectedImageUris.isEmpty()) {
            imgPreview.setImageURI(selectedImageUris.get(0));
        }

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            try {
                Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
                SubCategory selectedSubCategory = (SubCategory) spinnerSubCategory.getSelectedItem();

                Package pkg = new Package();
                pkg.setId(generateNextId());
                pkg.setTenGoi(edtPackageName.getText().toString().trim());
                pkg.setMoTa(edtPackageDesc.getText().toString().trim());
                pkg.setGiaGoc(parseDoubleSafe(edtPrice.getText().toString()));
                pkg.setGiaGiam(parseDoubleSafe(edtDiscount.getText().toString()));
                pkg.setVat(parseDoubleSafe(edtVAT.getText().toString()));
                pkg.setDonViTinh(edtUnit.getText().toString().trim());
                pkg.setSoLuong(parseIntSafe(edtQuantity.getText().toString()));
                pkg.setStatus(edtStatus.getText().toString().trim());
                pkg.setNote(edtNote.getText().toString().trim());
                pkg.setCategoryId(selectedCategory != null ? selectedCategory.getId() : "");
                pkg.setSubcategoryId(selectedSubCategory != null ? selectedSubCategory.getId() : "");
                pkg.setFree3Posts(checkboxFree3Posts.isChecked());
                pkg.setBillingCycle(spinnerBillingCycle.getSelectedItem().toString());
                pkg.setMaxPosts(parseIntSafe(edtMaxPosts.getText().toString()));
                pkg.setMaxCharacters(parseIntSafe(edtMaxCharacters.getText().toString()));
                pkg.setMaxImages(parseIntSafe(edtMaxImages.getText().toString()));
                pkg.setStartDate(edtStartDate.getText().toString().trim());
                pkg.setEndDate(edtEndDate.getText().toString().trim());
                pkg.setPackageType(spinnerPackageType.getSelectedItem().toString());

                if (!selectedImageUris.isEmpty()) {
                    List<String> base64Images = new ArrayList<>();
                    for (Uri uri : selectedImageUris) {
                        String base64 = encodeImageToBase64(this, uri);
                        if (base64 != null) base64Images.add(base64);
                    }
                    pkg.setHinhAnh(base64Images);
                    selectedImageUris.clear();
                }

                savePackage(pkg);
            } catch (Exception e) {
                Toast.makeText(this, "❌ Vui lòng nhập đúng dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
    private void showEditPackageDialog(Package pkg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.add_package_layout, null);
        builder.setView(view);
        builder.setTitle("Chỉnh sửa gói");

        EditText edtPackageName = view.findViewById(R.id.edtPackageName);
        EditText edtPackageDesc = view.findViewById(R.id.edtPackageDesc);
        EditText edtPrice = view.findViewById(R.id.edtPrice);
        EditText edtDiscount = view.findViewById(R.id.edtDiscount);
        EditText edtVAT = view.findViewById(R.id.edtVAT);
        EditText edtUnit = view.findViewById(R.id.edtUnit);
        EditText edtQuantity = view.findViewById(R.id.edtQuantity);
        EditText edtStatus = view.findViewById(R.id.edtStatus);
        EditText edtNote = view.findViewById(R.id.edtNote);
        CheckBox checkboxFree3Posts = view.findViewById(R.id.checkboxIsFree3Post);
        Spinner spinnerBillingCycle = view.findViewById(R.id.spinnerBillingCycle);
        EditText edtMaxPosts = view.findViewById(R.id.edtMaxPosts);
        EditText edtMaxCharacters = view.findViewById(R.id.edtMaxCharacters);
        EditText edtMaxImages = view.findViewById(R.id.edtMaxImages);
        EditText edtStartDate = view.findViewById(R.id.edtStartDate);
        EditText edtEndDate = view.findViewById(R.id.edtEndDate);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Spinner spinnerSubCategory = view.findViewById(R.id.spinnerSubCategory);
        ImageView imgPreview = view.findViewById(R.id.imgPreview);
        TextView btnChooseImage = view.findViewById(R.id.btnChooseImage);
        Spinner spinnerPackageType = view.findViewById(R.id.spinnerPackageType);

        edtPackageName.setText(pkg.getTenGoi());
        edtPackageDesc.setText(pkg.getMoTa());
        edtPrice.setText(String.valueOf(pkg.getGiaGoc()));
        edtDiscount.setText(String.valueOf(pkg.getGiaGiam()));
        edtVAT.setText(String.valueOf(pkg.getVat()));
        edtUnit.setText(pkg.getDonViTinh());
        edtQuantity.setText(String.valueOf(pkg.getSoLuong()));
        edtStatus.setText(pkg.getStatus());
        edtNote.setText(pkg.getNote());
        checkboxFree3Posts.setChecked(pkg.isFree3Posts());

        String[] billingCycles = {"Theo tuần", "Theo tháng", "Theo quý", "Theo năm"};
        ArrayAdapter<String> billingCycleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, billingCycles);
        billingCycleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBillingCycle.setAdapter(billingCycleAdapter);

        if (pkg.getBillingCycle() != null) {
            int index = Arrays.asList(billingCycles).indexOf(pkg.getBillingCycle());
            if (index >= 0) spinnerBillingCycle.setSelection(index);
        }

        edtMaxPosts.setText(String.valueOf(pkg.getMaxPosts()));
        edtMaxCharacters.setText(String.valueOf(pkg.getMaxCharacters()));
        edtMaxImages.setText(String.valueOf(pkg.getMaxImages()));
        edtStartDate.setText(pkg.getStartDate());
        edtEndDate.setText(pkg.getEndDate());

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        filteredSubCategories.clear();
        ArrayAdapter<SubCategory> subCategoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filteredSubCategories);
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubCategory.setAdapter(subCategoryAdapter);

        ArrayAdapter<String> packageTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Gói thường", "VIP", "VVIP"});
        packageTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPackageType.setAdapter(packageTypeAdapter);

        if (pkg.getPackageType() != null) {
            for (int i = 0; i < packageTypeAdapter.getCount(); i++) {
                if (pkg.getPackageType().equals(packageTypeAdapter.getItem(i))) {
                    spinnerPackageType.setSelection(i);
                    break;
                }
            }
        }

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategoryId = categoryList.get(position).getId();
                filterSubCategoriesByCategory(selectedCategoryId);
                subCategoryAdapter.notifyDataSetChanged();
                if (!filteredSubCategories.isEmpty()) spinnerSubCategory.setSelection(0);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId().equals(pkg.getCategoryId())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        filterSubCategoriesByCategory(pkg.getCategoryId());
        subCategoryAdapter.notifyDataSetChanged();

        for (int i = 0; i < filteredSubCategories.size(); i++) {
            if (filteredSubCategories.get(i).getId().equals(pkg.getSubcategoryId())) {
                spinnerSubCategory.setSelection(i);
                break;
            }
        }

        if (pkg.getHinhAnh() != null && !pkg.getHinhAnh().isEmpty()) {
            String base64First = pkg.getHinhAnh().get(0);
            byte[] decodedBytes = Base64.decode(base64First, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imgPreview.setImageBitmap(bitmap);
        } else {
            imgPreview.setImageResource(R.drawable.user);
        }

        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(intent);
        });

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            try {
                Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
                SubCategory selectedSubCategory = (SubCategory) spinnerSubCategory.getSelectedItem();

                pkg.setTenGoi(edtPackageName.getText().toString().trim());
                pkg.setMoTa(edtPackageDesc.getText().toString().trim());
                pkg.setGiaGoc(parseDoubleSafe(edtPrice.getText().toString()));
                pkg.setGiaGiam(parseDoubleSafe(edtDiscount.getText().toString()));
                pkg.setVat(parseDoubleSafe(edtVAT.getText().toString()));
                pkg.setDonViTinh(edtUnit.getText().toString().trim());
                pkg.setSoLuong(parseIntSafe(edtQuantity.getText().toString()));
                pkg.setStatus(edtStatus.getText().toString().trim());
                pkg.setNote(edtNote.getText().toString().trim());
                pkg.setCategoryId(selectedCategory != null ? selectedCategory.getId() : "");
                pkg.setSubcategoryId(selectedSubCategory != null ? selectedSubCategory.getId() : "");
                pkg.setFree3Posts(checkboxFree3Posts.isChecked());
                pkg.setBillingCycle(spinnerBillingCycle.getSelectedItem().toString());
                pkg.setMaxPosts(parseIntSafe(edtMaxPosts.getText().toString()));
                pkg.setMaxCharacters(parseIntSafe(edtMaxCharacters.getText().toString()));
                pkg.setMaxImages(parseIntSafe(edtMaxImages.getText().toString()));
                pkg.setStartDate(edtStartDate.getText().toString().trim());
                pkg.setEndDate(edtEndDate.getText().toString().trim());
                pkg.setPackageType(spinnerPackageType.getSelectedItem().toString());

                if (!selectedImageUris.isEmpty()) {
                    List<String> base64Images = new ArrayList<>();
                    for (Uri uri : selectedImageUris) {
                        String base64 = encodeImageToBase64(this, uri);
                        if (base64 != null) base64Images.add(base64);
                    }
                    pkg.setHinhAnh(base64Images);
                    selectedImageUris.clear();
                }

                db.collection("Package").document(pkg.getId())
                        .set(pkg)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                            reload();
                        });
            } catch (Exception e) {
                Toast.makeText(this, "❌ Vui lòng nhập đúng dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
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
            e.printStackTrace();
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
                .addOnSuccessListener(packageSnapshots -> {
                    packageList.clear();
                    for (QueryDocumentSnapshot packageDoc : packageSnapshots) {
                        Package pkg = packageDoc.toObject(Package.class);
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
            categoriesLoaded = true;
            checkLoadComplete();
        });
    }

    private void loadSubCategories() {
        db.collection("SubCategory").get().addOnSuccessListener(snapshot -> {
            allSubCategories.clear();
            for (QueryDocumentSnapshot doc : snapshot) {
                SubCategory sub = doc.toObject(SubCategory.class);
                allSubCategories.add(sub);
            }
            subCategoriesLoaded = true;
            checkLoadComplete();
        });
    }

    private void loadSystemConfig() {
        db.collection("SystemConfig")
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
                    systemConfigLoaded = true;
                    checkLoadComplete();
                });
    }

    private void checkLoadComplete() {
        // Enable nút add khi đã load đủ dữ liệu config, category, subcategory
        if (categoriesLoaded && subCategoriesLoaded && systemConfigLoaded) {
            btnAddPackage.setEnabled(true);
        }
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
