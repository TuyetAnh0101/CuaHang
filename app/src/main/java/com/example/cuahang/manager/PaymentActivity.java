package com.example.cuahang.manager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.CartAdapter;
import com.example.cuahang.model.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView tvTotal;
    private Button btnPay;
    private List<CartItem> cartItems;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        recyclerView = findViewById(R.id.rcvCartItems);
        tvTotal = findViewById(R.id.tvTotalPrice);
        btnPay = findViewById(R.id.btnPay);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cartItems = CartManager.getInstance().getCartItems();

        adapter = new CartAdapter(this, cartItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        updateTotal();

        btnPay.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            } else {
                saveInvoiceAndOrder();
            }
        });
    }

    @Override
    public void onDeleteClick(int position) {
        CartManager.getInstance().removeItem(position);
        adapter.notifyItemRemoved(position);
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPackagePrice() * item.getSoLuong();
        }
        tvTotal.setText("Tổng tiền: " + String.format("%,.0f", total) + "đ");
    }

    private void saveInvoiceAndOrder() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        double totalPrice = 0;
        double totalTax = 0;
        double totalDiscount = 0;
        int totalQuantity = 0;

        List<Map<String, Object>> orderPackageList = new ArrayList<>();
        for (CartItem item : cartItems) {
            int quantity = item.getSoLuong();
            double price = item.getDiscount(); // Giá đã giảm = giá bán cuối
            double thanhTien = price * quantity;

            // Nếu CartItem chưa có thanhTien, thì tính lại ở đây
            item.setThanhTien(thanhTien);

            Map<String, Object> packageMap = new HashMap<>();
            packageMap.put("packageId", item.getPackageId());
            packageMap.put("packageType", item.getPackageType() != null ? item.getPackageType() : ""); // tránh null
            packageMap.put("tenGoi", item.getPackageName() != null ? item.getPackageName() : "");       // tránh null
            packageMap.put("soLuong", quantity);
            packageMap.put("giaGiam", price);
            packageMap.put("thanhTien", thanhTien);

            totalQuantity += quantity;
            orderPackageList.add(packageMap);

            // Ghi log kiểm tra đầy đủ
            Log.d("CartItemCheck", "packageId: " + item.getPackageId());
            Log.d("CartItemCheck", "packageType: " + item.getPackageType());
            Log.d("CartItemCheck", "tenGoi: " + item.getPackageName());
            Log.d("CartItemCheck", "soLuong: " + quantity);
            Log.d("CartItemCheck", "giaGiam: " + price);
            Log.d("CartItemCheck", "thanhTien: " + thanhTien);
        }

        double totalAmount = totalPrice + totalTax - totalDiscount;
        String invoiceId = db.collection("invoices").document().getId();

        Map<String, Object> invoiceMap = new HashMap<>();
        invoiceMap.put("id", invoiceId);
        invoiceMap.put("dateTime", FieldValue.serverTimestamp());
        invoiceMap.put("totalAmount", totalAmount);
        invoiceMap.put("totalPrice", totalPrice);
        invoiceMap.put("tax", totalTax);
        invoiceMap.put("totalDiscount", totalDiscount);
        invoiceMap.put("totalQuantity", totalQuantity);
        invoiceMap.put("createdBy", userId);
        invoiceMap.put("status", "Đã thanh toán");

        int finalTotalQuantity = totalQuantity;

        db.collection("invoices")
                .document(invoiceId)
                .set(invoiceMap)
                .addOnSuccessListener(aVoid -> {
                    String orderId = db.collection("Orders").document().getId();

                    // Format ngày theo chuỗi "yyyy-MM-dd HH:mm:ss"
                    String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(new Date());

                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("id", orderId);
                    orderMap.put("idKhach", userId);
                    orderMap.put("ngayDat", currentDateTime);
                    orderMap.put("tongTien", totalAmount);
                    orderMap.put("tongSoLuong", finalTotalQuantity);
                    orderMap.put("statusXuLy", "Chờ xử lý");
                    orderMap.put("statusThanhToan", "Đã thanh toán");
                    orderMap.put("note", "");
                    orderMap.put("invoiceId", invoiceId);
                    orderMap.put("packages", orderPackageList);

                    db.collection("Orders")
                            .document(orderId)
                            .set(orderMap)
                            .addOnSuccessListener(orderVoid -> {
                                // ✅ Sau khi tạo đơn hàng, giảm số lượng gói trong collection Packages
                                for (CartItem item : cartItems) {
                                    String packageId = item.getPackageId();
                                    int quantityToSubtract = item.getSoLuong();

                                    db.collection("Package").document(packageId)
                                            .update("soLuong", FieldValue.increment(-quantityToSubtract))
                                            .addOnSuccessListener(a -> {
                                                // Thành công
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Không thể cập nhật số lượng gói: " + packageId, Toast.LENGTH_SHORT).show();
                                            });
                                }

                                Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                                CartManager.getInstance().clearCart();
                                adapter.notifyDataSetChanged();
                                updateTotal();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi lưu đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
