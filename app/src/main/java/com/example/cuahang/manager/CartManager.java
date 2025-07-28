package com.example.cuahang.manager;

import com.example.cuahang.model.CartItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void addToCart(CartItem item) {
        cartItems.add(item);
    }

    public void removeItem(int index) {
        if (index >= 0 && index < cartItems.size()) {
            cartItems.remove(index);
        }
    }

    public void clearCart() {
        cartItems.clear();
    }
    public void addPackageById(String packageId, Runnable onSuccess) {
        FirebaseFirestore.getInstance().collection("Package")
                .document(packageId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("tenGoi");
                        double price = doc.getDouble("giaGiam");
                        double tax = doc.getDouble("vat");
                        int quantity = 1;

                        CartItem item = new CartItem(packageId, name, price, 1);
                        cartItems.add(item);
                        if (onSuccess != null) onSuccess.run();
                    }
                });
    }

}
