package com.example.cuahang.manager;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cuahang.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PackageDetailActivity extends AppCompatActivity {

    private static final String TAG = "PackageDetailActivity";

    private TextView tvTenGoi, tvMoTa, tvGia, tvVat, tvSoLuong, tvBillingCycle, tvGioiHan, tvThoiGian, tvNote;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_detail);

        db = FirebaseFirestore.getInstance();
        initViews();

        String packageId = getIntent().getStringExtra("id");
        if (packageId == null || packageId.isEmpty()) {
            Log.e(TAG, "Không có packageId được truyền vào!");
            return;
        }

        fetchPackageFromFirestore(packageId);
    }

    private void initViews() {
        tvTenGoi = findViewById(R.id.tvTenGoi);
        tvMoTa = findViewById(R.id.tvMoTa);
        tvGia = findViewById(R.id.tvGia);
        tvVat = findViewById(R.id.tvVat);
        tvSoLuong = findViewById(R.id.tvSoLuong);
        tvBillingCycle = findViewById(R.id.tvBillingCycle);
        tvGioiHan = findViewById(R.id.tvGioiHan);
        tvThoiGian = findViewById(R.id.tvThoiGian);
        tvNote = findViewById(R.id.tvNote);
    }

    private void fetchPackageFromFirestore(String packageId) {
        db.collection("Package").document(packageId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        displayPackage(documentSnapshot);
                    } else {
                        Log.e(TAG, "Không tìm thấy tài liệu với ID: " + packageId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi lấy dữ liệu gói tin: ", e);
                });
    }

    private void displayPackage(DocumentSnapshot doc) {
        String tenGoi = doc.getString("packageType");
        String moTa = doc.getString("moTa");
        Double gia = doc.getDouble("gia");
        Double giaGiam = doc.getDouble("giaGiam");
        Double vat = doc.getDouble("vat");
        Long soLuong = doc.getLong("soLuong");
        String chuKy = doc.getString("billingCycle");
        Long gioiHan = doc.getLong("maxPosts");
        String thoiGian = doc.getString("thoiGian");
        String note = doc.getString("note");

        Log.d(TAG, "tenGoi: " + tenGoi);
        Log.d(TAG, "gia: " + gia + ", vat: " + vat + ", gioiHan: " + gioiHan);

        tvTenGoi.setText(tenGoi != null ? tenGoi : "");
        tvMoTa.setText(moTa != null ? moTa : "");
        tvGia.setText("Giá: " + (gia != null ? gia : 0) + "đ | Giảm: " + (giaGiam != null ? giaGiam : 0) + "đ");
        tvVat.setText("VAT: " + (vat != null ? vat : 0) + "%");
        tvSoLuong.setText("Số lượng: " + (soLuong != null ? soLuong : 0));
        tvBillingCycle.setText("Chu kỳ: " + (chuKy != null ? chuKy : ""));
        tvGioiHan.setText("Giới hạn bài viết: " + (gioiHan != null ? gioiHan : 0));
        tvThoiGian.setText("Thời gian sử dụng: " + (thoiGian != null ? thoiGian : ""));
        tvNote.setText("Ghi chú: " + (note != null ? note : ""));
    }
}
