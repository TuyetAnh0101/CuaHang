package com.example.cuahang.manager;

import com.example.cuahang.model.CartItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class CartManager {

    private static CartManager instance;
    private final List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // Thêm sản phẩm vào giỏ hàng
    public void addToCart(CartItem item) {
        for (CartItem existing : cartItems) {
            if (existing.getPackageId().equals(item.getPackageId())) {
                existing.setSoLuong(existing.getSoLuong() + item.getSoLuong());
                return;
            }
        }
        cartItems.add(item);
    }

    // Xoá sản phẩm khỏi giỏ
    public void removeFromCart(CartItem item) {
        cartItems.remove(item);
    }

    // Xoá toàn bộ giỏ hàng
    public void clearCart() {
        cartItems.clear();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    // Tăng số lượng sản phẩm
    public void increaseQuantity(String packageId) {
        for (CartItem item : cartItems) {
            if (item.getPackageId().equals(packageId)) {
                item.setSoLuong(item.getSoLuong() + 1);
                break;
            }
        }
    }

    // Giảm số lượng sản phẩm, nếu về 0 thì xoá khỏi giỏ
    public void decreaseQuantity(String packageId) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getPackageId().equals(packageId)) {
                int newQuantity = item.getSoLuong() - 1;
                if (newQuantity <= 0) {
                    cartItems.remove(i);
                } else {
                    item.setSoLuong(newQuantity);
                }
                break;
            }
        }
    }

    // Tổng giá chưa thuế
    public double getTotalBeforeTax() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalBeforeTax();
        }
        return total;
    }

    // Tổng thuế
    public double getTotalTax() {
        double tax = 0;
        for (CartItem item : cartItems) {
            tax += item.getTotalBeforeTax() * item.getTax() / 100.0;
        }
        return tax;
    }

    // Tổng giá sau thuế
    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getThanhTien();
        }
        return total;
    }

    // Tổng số lượng
    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getSoLuong();
        }
        return total;
    }

    // Lấy package từ Firestore và thêm vào giỏ
    public void addPackageById(String packageId, int soLuongDaMua, Runnable onSuccess) {
        FirebaseFirestore.getInstance()
                .collection("Package")
                .document(packageId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            String id = documentSnapshot.getId();
                            String name = documentSnapshot.getString("tenGoi");
                            String type = documentSnapshot.getString("packageType");

                            Double giaGoc = documentSnapshot.getDouble("giaGoc");
                            Double giaGiam = documentSnapshot.getDouble("giaGiam");
                            Double vat = documentSnapshot.getDouble("vat");

                            if (name == null || giaGiam == null) {
                                Log.e("CartManager", "Thiếu tên gói hoặc giá giảm.");
                                return;
                            }

                            // Dữ liệu mặc định nếu thiếu
                            String safeType = (type != null) ? type : "";
                            double safeGiaGoc = (giaGoc != null) ? giaGoc : giaGiam;
                            double safeVat = (vat != null) ? vat : 0.0;

                            CartItem cartItem = new CartItem(
                                    id,
                                    name,
                                    safeType,
                                    safeGiaGoc,
                                    giaGiam,
                                    soLuongDaMua,
                                    safeVat
                            );

                            addToCart(cartItem);

                            if (onSuccess != null) {
                                onSuccess.run();
                            }

                        } catch (Exception e) {
                            Log.e("CartManager", "Lỗi khi đọc dữ liệu Package: " + e.getMessage());
                        }
                    } else {
                        Log.w("CartManager", "Không tìm thấy document với ID: " + packageId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CartManager", "Lỗi khi truy cập Firestore: " + e.getMessage());
                });
    }
}
