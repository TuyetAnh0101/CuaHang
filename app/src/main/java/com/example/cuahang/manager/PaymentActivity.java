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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            double price = item.getPackagePrice();
            double tax = item.getTax();           // giả sử có sẵn trong CartItem
            double discount = item.getDiscount(); // giả sử có sẵn trong CartItem

            double thanhTien = (price + tax - discount) * quantity;

            Map<String, Object> packageMap = new HashMap<>();
            packageMap.put("packageId", item.getPackageId());
            packageMap.put("packageName", item.getPackageName());
            packageMap.put("price", price);
            packageMap.put("quantity", quantity);
            packageMap.put("tax", tax);
            packageMap.put("discount", discount);
            packageMap.put("thanhTien", thanhTien);

            totalPrice += price * quantity;
            totalTax += tax * quantity;
            totalDiscount += discount * quantity;
            totalQuantity += quantity;

            orderPackageList.add(packageMap);
        }

        double totalAmount = totalPrice + totalTax - totalDiscount;

        // 1. Tạo hóa đơn
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
                    // 2. Tạo đơn hàng sau khi lưu hóa đơn thành công
                    String orderId = db.collection("Orders").document().getId();

                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("id", orderId);
                    orderMap.put("idKhach", userId);
                    orderMap.put("ngayDat", FieldValue.serverTimestamp());
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
