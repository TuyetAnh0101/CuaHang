package com.example.cuahang.manager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cuahang.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class InvoiceDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvUser, tvDate, tvTotal, tvQuantity, tvTax, tvDiscount, tvPackageName, tvPackagePrice;
    private String invoiceId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        tvUser = findViewById(R.id.tvUser);
        tvDate = findViewById(R.id.tvDate);
        tvTotal = findViewById(R.id.tvTotal);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTax = findViewById(R.id.tvTax);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvPackageName = findViewById(R.id.tvPackageName);
        tvPackagePrice = findViewById(R.id.tvPackagePrice);

        invoiceId = getIntent().getStringExtra("invoiceId");

        if (invoiceId != null) {
            Log.d("InvoiceDetail", "Đang tải hóa đơn với ID: " + invoiceId);
            loadInvoiceDetails(invoiceId);
        } else {
            Toast.makeText(this, "Không tìm thấy mã hóa đơn", Toast.LENGTH_SHORT).show();
            Log.e("InvoiceDetail", "invoiceId null trong Intent");
            finish();
        }
    }

    private void loadInvoiceDetails(String id) {
        db.collection("invoices").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("InvoiceDetail", "Hóa đơn tồn tại");

                        String userId = documentSnapshot.getString("userId");
                        String ngayDat = documentSnapshot.getString("ngayDat");
                        Double tongTien = documentSnapshot.getDouble("tongTien");
                        Double giamGia = documentSnapshot.getDouble("giamGia");
                        Double thue = documentSnapshot.getDouble("thue");
                        Long soLuong = documentSnapshot.getLong("soLuong");
                        Map<String, Object> packageInfo = (Map<String, Object>) documentSnapshot.get("package");

                        Log.d("InvoiceDetail", "userId: " + userId);
                        Log.d("InvoiceDetail", "ngayDat: " + ngayDat);
                        Log.d("InvoiceDetail", "tongTien: " + tongTien);
                        Log.d("InvoiceDetail", "giamGia: " + giamGia);
                        Log.d("InvoiceDetail", "thue: " + thue);
                        Log.d("InvoiceDetail", "soLuong: " + soLuong);
                        Log.d("InvoiceDetail", "package: " + packageInfo);

                        tvUser.setText("Mã người dùng: " + (userId != null ? userId : "Không có"));
                        tvDate.setText("Ngày đặt: " + (ngayDat != null ? ngayDat : "Không có"));
                        tvTotal.setText("Tổng tiền: " + formatCurrency(tongTien != null ? tongTien : 0));
                        tvDiscount.setText("Giảm giá: " + formatCurrency(giamGia != null ? giamGia : 0));
                        tvTax.setText("Thuế: " + formatCurrency(thue != null ? thue : 0));
                        tvQuantity.setText("Số lượng: " + (soLuong != null ? soLuong : 0));

                        if (packageInfo != null) {
                            String tenGoi = (String) packageInfo.get("tenGoi");
                            Number gia = (Number) packageInfo.get("gia");

                            Log.d("InvoiceDetail", "tenGoi: " + tenGoi);
                            Log.d("InvoiceDetail", "gia: " + gia);

                            tvPackageName.setText("Tên gói: " + (tenGoi != null ? tenGoi : "Không có"));
                            tvPackagePrice.setText("Giá gói: " + formatCurrency(gia != null ? gia.doubleValue() : 0));
                        } else {
                            tvPackageName.setText("Tên gói: Không có");
                            tvPackagePrice.setText("Giá gói: Không có");
                            Log.d("InvoiceDetail", "Không có thông tin gói");
                        }

                    } else {
                        Log.w("InvoiceDetail", "Không tìm thấy tài liệu hóa đơn với ID: " + id);
                        Toast.makeText(this, "Không tìm thấy hóa đơn", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải chi tiết hóa đơn", Toast.LENGTH_SHORT).show();
                    Log.e("InvoiceDetail", "Lỗi khi truy vấn Firestore: ", e);
                    finish();
                });
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return format.format(amount);
    }
}


