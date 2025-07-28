package com.example.cuahang.manager;

import com.example.cuahang.model.CartItem;

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
}
