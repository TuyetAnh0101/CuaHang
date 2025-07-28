package com.example.cuahang.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cuahang.R;
import com.example.cuahang.model.CartItem;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PackageDetailActivity extends AppCompatActivity {

    private static final String TAG = "PackageDetailActivity";

    // TextViews
    private TextView tvTenGoi, tvMoTa, tvGiaGoc, tvGiaGiam, tvVat, tvSoLuong,
            tvBillingCycle, tvGioiHanBaiViet, tvMaxCharacters, tvMaxImages,
            tvPackageType, tvNote;

    // Buttons
    private Button btnThemVaoGio, btnMuaNgay;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_detail);

        db = FirebaseFirestore.getInstance();
        initViews();

        // Lấy packageId từ Intent
        String packageId = getIntent().getStringExtra("id");
        if (packageId == null || packageId.isEmpty()) {
            Log.e(TAG, "Không có packageId được truyền vào!");
            Toast.makeText(this, "Thiếu ID gói tin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchPackageFromFirestore(packageId);
        setupListeners();
    }

    // Gán ID cho các view
    private void initViews() {
        tvTenGoi = findViewById(R.id.tvTenGoi);
        tvMoTa = findViewById(R.id.tvMoTa);
        tvGiaGoc = findViewById(R.id.tvGiaGoc);
        tvGiaGiam = findViewById(R.id.tvGiaGiam);
        tvVat = findViewById(R.id.tvVAT);
        tvSoLuong = findViewById(R.id.tvSoLuong);
        tvBillingCycle = findViewById(R.id.tvBillingCycle);
        tvGioiHanBaiViet = findViewById(R.id.tvMaxPosts);
        tvMaxCharacters = findViewById(R.id.tvMaxCharacters);
        tvMaxImages = findViewById(R.id.tvMaxImages);
        tvPackageType = findViewById(R.id.tvPackageType);
        tvNote = findViewById(R.id.tvNote);

        btnThemVaoGio = findViewById(R.id.btnThemVaoGio);
        btnMuaNgay = findViewById(R.id.btnMuaNgay);
    }

    // Thiết lập sự kiện click cho nút
    private void setupListeners() {
        btnThemVaoGio.setOnClickListener(v -> {
            addToCart();
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        });

        btnMuaNgay.setOnClickListener(v -> {
            addToCart();
            Intent intent = new Intent(PackageDetailActivity.this, PaymentActivity.class);
            startActivity(intent);
        });
    }


    // Lấy dữ liệu từ Firestore
    private void fetchPackageFromFirestore(String packageId) {
        db.collection("Package").document(packageId).get()
                .addOnSuccessListener(this::displayPackage)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi lấy dữ liệu gói tin: ", e);
                    Toast.makeText(this, "Không thể tải gói tin", Toast.LENGTH_SHORT).show();
                });
    }

    // Hiển thị dữ liệu lên giao diện
    private void displayPackage(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            Log.e(TAG, "Không tìm thấy gói tin!");
            Toast.makeText(this, "Gói tin không tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        tvTenGoi.setText(getOrEmpty("🔥 " + doc.getString("tenGoi") + " 🔥"));
        tvMoTa.setText("📄 Mô tả: " + getOrEmpty(doc.getString("moTa")));
        tvGiaGoc.setText("💰 Giá gốc: " + formatCurrency(doc.getDouble("giaGoc")));
        tvGiaGiam.setText("🔻 Giá giảm: " + formatCurrency(doc.getDouble("giaGiam")));
        tvVat.setText("VAT: " + formatPercent(doc.getDouble("vat")));
        tvSoLuong.setText("Số lượng: " + formatNumber(doc.getLong("soLuong")));
        tvBillingCycle.setText("Chu kỳ: " + getOrEmpty(doc.getString("billingCycle")));
        tvGioiHanBaiViet.setText("Giới hạn bài viết: " + formatNumber(doc.getLong("maxPosts")));
        tvMaxCharacters.setText("Số ký tự tối đa: " + formatNumber(doc.getLong("maxCharacters")));
        tvMaxImages.setText("Số ảnh tối đa: " + formatNumber(doc.getLong("maxImages")));
        tvPackageType.setText("Loại gói: " + getOrEmpty(doc.getString("packageType")));
        tvNote.setText("📝 Ghi chú: " + getOrEmpty(doc.getString("note")));
    }

    // Helpers
    private String getOrEmpty(String text) {
        return text != null ? text : "";
    }

    private String formatCurrency(Double value) {
        if (value == null) return "0đ";
        return String.format("%,.0fđ", value);
    }

    private String formatPercent(Double value) {
        if (value == null) return "0%";
        return String.format("%.0f%%", value);
    }

    private String formatNumber(Long value) {
        if (value == null) return "0";
        return String.valueOf(value);
    }
    private void addToCart() {
        // Lấy thông tin từ các TextView đang hiển thị
        String tenGoi = tvTenGoi.getText().toString().replace("🔥 ", "").replace(" 🔥", "");
        double giaGiam = parseCurrency(tvGiaGiam.getText().toString());
        int soLuong = 1;

        CartItem item = new CartItem();
        item.setPackageId(getIntent().getStringExtra("id"));
        item.setPackageName(tenGoi);
        item.setPackagePrice(giaGiam);
        item.setSoLuong(soLuong);

        CartManager.getInstance().addToCart(item);
    }
    private double parseCurrency(String text) {
        try {
            return Double.parseDouble(text.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
