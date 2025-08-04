package com.example.cuahang.manager;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
import java.util.*;

public class PaymentActivity extends AppCompatActivity implements CartAdapter.OnCartItemActionListener {

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
    public void onCartUpdated() {
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getThanhTien(); // đã bao gồm thuế
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
        final int[] totalQuantity = {0};

        List<Map<String, Object>> orderPackageList = new ArrayList<>();

        for (CartItem item : cartItems) {
            int quantity = item.getSoLuong();
            double price = item.getPackagePrice();
            double tax = item.getTax();
            double thanhTien = item.getThanhTien();
            double totalBeforeTax = item.getTotalBeforeTax();

            totalPrice += totalBeforeTax;
            totalTax += thanhTien - totalBeforeTax;
            totalQuantity[0] += item.getSoLuong();

            Map<String, Object> packageMap = new HashMap<>();
            packageMap.put("packageId", item.getPackageId());
            packageMap.put("packageName", item.getPackageName());
            packageMap.put("packageType", item.getPackageType());
            packageMap.put("soLuong", quantity);
            packageMap.put("giaGoc", item.getOriginalPrice());
            packageMap.put("giaGiam", price);
            packageMap.put("VAT", tax);
            packageMap.put("thanhTien", thanhTien);

            orderPackageList.add(packageMap);
        }

        double totalAmount = totalPrice + totalTax;
        String invoiceId = db.collection("invoices").document().getId();

        Map<String, Object> invoiceMap = new HashMap<>();
        invoiceMap.put("id", invoiceId);
        invoiceMap.put("dateTime", FieldValue.serverTimestamp());
        invoiceMap.put("totalPrice", totalPrice);
        invoiceMap.put("tax", totalTax);
        invoiceMap.put("totalAmount", totalAmount);
        invoiceMap.put("totalQuantity", totalQuantity);
        invoiceMap.put("createdBy", userId);
        invoiceMap.put("status", "Đã thanh toán");

        String orderId = db.collection("Orders").document().getId();
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        db.collection("invoices").document(invoiceId).set(invoiceMap)
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("id", orderId);
                    orderMap.put("idKhach", userId);
                    orderMap.put("ngayDat", currentDateTime);
                    orderMap.put("tongTien", totalAmount);
                    orderMap.put("tongSoLuong", totalQuantity);
                    orderMap.put("statusXuLy", "Chờ xử lý");
                    orderMap.put("statusThanhToan", "Đã thanh toán");
                    orderMap.put("note", "");
                    orderMap.put("invoiceId", invoiceId);
                    orderMap.put("packages", orderPackageList);

                    db.collection("Orders").document(orderId).set(orderMap)
                            .addOnSuccessListener(orderVoid -> {
                                for (CartItem item : cartItems) {
                                    db.collection("Package").document(item.getPackageId())
                                            .update("soLuong", FieldValue.increment(-item.getSoLuong()))
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Lỗi cập nhật số lượng: " + item.getPackageId(), Toast.LENGTH_SHORT).show();
                                            });
                                }

                                Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                                CartManager.getInstance().clearCart();
                                adapter.notifyDataSetChanged();
                                updateTotal();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi lưu đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
