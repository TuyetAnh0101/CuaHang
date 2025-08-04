package com.example.cuahang.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cuahang.R;
import com.example.cuahang.adapter.OrderPackageItemAdapter;
import com.example.cuahang.manager.CartManager;
import com.example.cuahang.manager.PaymentActivity;
import com.example.cuahang.model.User;
import com.example.cuahang.ui.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private RecyclerView rcvPurchased;
    private Button btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private OrderPackageItemAdapter adapter;
    private List<Map<String, Object>> allPackages = new ArrayList<>();

    public AccountFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        rcvPurchased = view.findViewById(R.id.rvPurchasedPackages);
        btnLogout = view.findViewById(R.id.btnLogout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadUserInfo();
        loadUserOrders();

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void setupRecyclerView() {
        rcvPurchased.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderPackageItemAdapter(allPackages, (packageId, quantity) -> {
            CartManager.getInstance().addPackageById(packageId, quantity, () -> {
                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), PaymentActivity.class));
            });
        });
        rcvPurchased.setAdapter(adapter);
    }

    private void loadUserInfo() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("User").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    User user = snapshot.toObject(User.class);
                    if (user != null) {
                        tvUserName.setText(user.getName());
                        tvUserEmail.setText(user.getEmail());
                    }
                });
    }

    private void loadUserOrders() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("Orders")
                .whereEqualTo("idKhach", uid)
                .orderBy("ngayDat", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    allPackages.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        List<Map<String, Object>> pkgs = (List<Map<String, Object>>) doc.get("packages");
                        if (pkgs != null) {
                            allPackages.addAll(pkgs);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
