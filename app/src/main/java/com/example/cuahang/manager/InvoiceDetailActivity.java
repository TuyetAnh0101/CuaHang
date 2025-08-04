package com.example.cuahang.manager;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cuahang.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvUser, tvCreatedBy, tvDate, tvStatus, tvTotal, tvDiscount, tvQuantity;
    private FirebaseFirestore db;

    private static final String TAG = "InvoiceDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);
        setTitle("Chi tiết hóa đơn");

        // Ánh xạ view
        tvOrderId = findViewById(R.id.tvOrderId);
        tvUser = findViewById(R.id.tvUser);
        tvCreatedBy = findViewById(R.id.tvCreatedBy);
        tvDate = findViewById(R.id.tvDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvTotal = findViewById(R.id.tvTotal);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvQuantity = findViewById(R.id.tvQuantity);

        db = FirebaseFirestore.getInstance();

        String invoiceId = getIntent().getStringExtra("id");
        Log.d(TAG, "invoiceId nhận được: " + invoiceId);

        if (invoiceId != null && !invoiceId.isEmpty()) {
            loadInvoiceDetails(invoiceId);
        } else {
            Toast.makeText(this, "Không tìm thấy hóa đơn!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "invoiceId null hoặc rỗng");
        }
    }

    private void loadInvoiceDetails(String invoiceId) {
        db.collection("invoices").document(invoiceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Dữ liệu hóa đơn: " + documentSnapshot.getData());

                        String orderId = documentSnapshot.getString("orderId");
                        String userId = documentSnapshot.getString("userId");
                        String createdBy = documentSnapshot.getString("createdBy");
                        String status = documentSnapshot.getString("status");

                        Double totalAmount = documentSnapshot.getDouble("totalAmount");
                        Double totalDiscount = documentSnapshot.getDouble("totalDiscount");
                        Double totalQuantity = documentSnapshot.getDouble("totalQuantity");
                        Timestamp timestamp = documentSnapshot.getTimestamp("dateTime");

                        // Định dạng ngày giờ
                        String dateFormatted = "N/A";
                        if (timestamp != null) {
                            Date date = timestamp.toDate();
                            dateFormatted = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
                        }

                        // Định dạng tiền tệ
                        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
                        String totalAmountStr = (totalAmount != null) ? decimalFormat.format(totalAmount) + " đ" : "0 đ";
                        String totalDiscountStr = (totalDiscount != null) ? decimalFormat.format(totalDiscount) + " đ" : "0 đ";
                        String totalQuantityStr = (totalQuantity != null) ? totalQuantity.intValue() + " SP" : "0 SP";

                        // Set dữ liệu hiển thị với emoji + prefix giống layout
                        tvOrderId.setText("🧾 Mã đơn hàng: " + (orderId != null ? orderId : "N/A"));
                        tvUser.setText("👤 Mã người dùng: " + (userId != null ? userId : "N/A"));
                        tvCreatedBy.setText("👨‍💼 Người tạo: " + (createdBy != null ? createdBy : "N/A"));
                        tvStatus.setText("📌 Trạng thái: " + (status != null ? status : "N/A"));
                        tvDate.setText("📅 Ngày tạo: " + dateFormatted);
                        tvTotal.setText("💵 Tổng tiền thanh toán: " + totalAmountStr);
                        tvDiscount.setText("🎁 Giảm giá: " + totalDiscountStr);
                        tvQuantity.setText("🔢 Tổng số lượng: " + totalQuantityStr);

                    } else {
                        Toast.makeText(this, "Hóa đơn không tồn tại!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Document không tồn tại: " + invoiceId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải hóa đơn!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi Firestore khi lấy hóa đơn", e);
                });
    }
}
