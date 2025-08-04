package com.example.cuahang.manager;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.InvoicePackageAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class InvoiceDetailActivity extends AppCompatActivity {

    private TextView tvUser, tvCreatedBy, tvDate, tvStatus, tvTotal, tvDiscount, tvQuantity;
    private RecyclerView rvPackages;
    private InvoicePackageAdapter adapter;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        // Ánh xạ đúng ID
        tvUser = findViewById(R.id.tvUser);
        tvCreatedBy = findViewById(R.id.tvCreatedBy);
        tvDate = findViewById(R.id.tvDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvTotal = findViewById(R.id.tvTotal);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvQuantity = findViewById(R.id.tvQuantity);
        rvPackages = findViewById(R.id.rvPackages);

        rvPackages.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        String invoiceId = getIntent().getStringExtra("invoiceId");
        if (invoiceId != null) {
            loadInvoiceDetails(invoiceId);
        } else {
            Toast.makeText(this, "Không tìm thấy hóa đơn!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInvoiceDetails(String invoiceId) {
        db.collection("invoices").document(invoiceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Gán dữ liệu cho TextView
                        tvUser.setText(documentSnapshot.getString("userId"));
                        tvCreatedBy.setText(documentSnapshot.getString("createdBy"));
                        tvDate.setText(documentSnapshot.getString("ngay"));
                        tvStatus.setText(documentSnapshot.getString("status"));

                        tvTotal.setText(documentSnapshot.get("tongTien") + " đ");
                        tvDiscount.setText(documentSnapshot.get("discount") + " đ");
                        tvQuantity.setText(documentSnapshot.get("tongSoLuong") + " SP");

                        // Lấy danh sách packages
                        List<Map<String, Object>> packageList =
                                (List<Map<String, Object>>) documentSnapshot.get("packages");

                        if (packageList != null && !packageList.isEmpty()) {
                            adapter = new InvoicePackageAdapter(packageList);
                            rvPackages.setAdapter(adapter);
                            Log.d("InvoiceDetail", "Có " + packageList.size() + " gói được mua");
                        } else {
                            Log.d("InvoiceDetail", "Không có gói nào trong hóa đơn");
                        }

                    } else {
                        Toast.makeText(this, "Hóa đơn không tồn tại!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải hóa đơn!", Toast.LENGTH_SHORT).show();
                    Log.e("InvoiceDetail", "Lỗi: ", e);
                });
    }
}
