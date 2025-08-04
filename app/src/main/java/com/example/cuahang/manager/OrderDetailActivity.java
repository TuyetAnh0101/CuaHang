package com.example.cuahang.manager;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.OrderPackageAdapter;
import com.example.cuahang.model.Order;
import com.example.cuahang.model.OrderPackage;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailActivity";

    private TextView tvOrderId, tvCustomerId, tvNgayDat, tvTotalPrice, tvTotalQuantity,
            tvStatusXuLy, tvStatusThanhToan, tvNote;
    private RecyclerView rvOrderPackages;

    private FirebaseFirestore db;
    private List<OrderPackage> packageList;
    private OrderPackageAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Log.d(TAG, "onCreate: Bắt đầu OrderDetailActivity");

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        initViews();

        // Nhận ID từ Intent
        String orderId = getIntent().getStringExtra("id"); // ✅ dùng getStringExtra

        if (orderId != null) {
            Log.d(TAG, "onCreate: Nhận được order ID: " + orderId);

            // Tải dữ liệu từ Firestore bằng ID
            loadOrderData(orderId);
            loadOrderPackages(orderId);
        } else {
            Log.e(TAG, "onCreate: Không nhận được order ID từ Intent");
        }
    }


    private void initViews() {
        tvOrderId = findViewById(R.id.tv_order_id);
        tvCustomerId = findViewById(R.id.tv_customer_id);
        tvNgayDat = findViewById(R.id.tv_order_date);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvTotalQuantity = findViewById(R.id.tv_total_quantity);
        tvStatusXuLy = findViewById(R.id.tv_status_xuly);
        tvStatusThanhToan = findViewById(R.id.tv_status_thanhtoan);
        tvNote = findViewById(R.id.tv_note);
        rvOrderPackages = findViewById(R.id.rv_order_packages);
    }

    private void bindOrderData(@NonNull Order order) {
        tvOrderId.setText("Mã đơn: " + order.getId());
        tvCustomerId.setText("Khách: " + order.getIdKhach());
        tvNgayDat.setText("Ngày đặt: " + order.getNgayDat());
        tvTotalPrice.setText("Tổng tiền: " + formatMoney(order.getTongTien()));
        tvTotalQuantity.setText("Tổng SL: " + order.getTongSoLuong());
        tvStatusXuLy.setText("Xử lý: " + order.getStatusXuLy());
        tvStatusThanhToan.setText("Thanh toán: " + order.getStatusThanhToan());
        tvNote.setText("Ghi chú: " + (order.getNote() != null ? order.getNote() : "Không có"));
    }

    private void loadOrderPackages(String orderId) {
        Log.d(TAG, "loadOrderPackages: Đang load packages cho orderId = " + orderId);

        packageList = new ArrayList<>();
        adapter = new OrderPackageAdapter(this, packageList);

        rvOrderPackages.setLayoutManager(new LinearLayoutManager(this));
        rvOrderPackages.setAdapter(adapter);

        CollectionReference packageRef = db.collection("Orders")
                .document(orderId)
                .collection("packages");

        packageRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                packageList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    OrderPackage pack = doc.toObject(OrderPackage.class);
                    packageList.add(pack);
                    Log.d(TAG, "Package loaded: " + pack.getTenGoi());
                }
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Tổng số packages: " + packageList.size());
            } else {
                Log.e(TAG, "Lỗi khi lấy packages", task.getException());
            }
        });
    }

    private String formatMoney(long amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return format.format(amount);
    }
    private void loadOrderData(String orderId) {
        db.collection("Orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Order order = documentSnapshot.toObject(Order.class);
                    if (order != null) {
                        bindOrderData(order); // hiện thông tin lên giao diện
                    } else {
                        Log.e(TAG, "Không tìm thấy đơn hàng với ID: " + orderId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi tải đơn hàng", e));
    }

}
