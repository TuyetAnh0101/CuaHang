package com.example.cuahang.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cuahang.R;
import com.example.cuahang.manager.CategoryActivity;
import com.example.cuahang.manager.PackageActivity;
import com.example.cuahang.manager.OrderActivity;
import com.example.cuahang.ui.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserFragment extends Fragment {

    public UserFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 🔒 Kiểm tra người dùng đã đăng nhập chưa
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Nếu chưa đăng nhập, chuyển sang LoginActivity
            Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
            startActivity(loginIntent);
            if (getActivity() != null) getActivity().finish();
            return null;
        }

        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // 🔘 Ánh xạ các nút
        Button btnCategory = view.findViewById(R.id.btnCategory);
        Button btnPackage = view.findViewById(R.id.btnPackage);
        Button btnInvoice = view.findViewById(R.id.btnInvoice);
        Button btnUser = view.findViewById(R.id.btnUser);
        Button btnOrder = view.findViewById(R.id.btnOrder);
        Button btnOrderDetail = view.findViewById(R.id.btnOrderDetail);

        // 📦 Chuyển đến các Activity tương ứng
        btnCategory.setOnClickListener(v ->
                startActivity(new Intent(getContext(), CategoryActivity.class)));

        btnPackage.setOnClickListener(v ->
                startActivity(new Intent(getContext(), PackageActivity.class)));

        btnOrder.setOnClickListener(v ->
                startActivity(new Intent(getContext(), OrderActivity.class)));

        // ❌ Các nút chưa được dùng thì để trống hoặc ẩn trong layout
        btnInvoice.setOnClickListener(v ->
                showFeatureComingSoon());

        btnUser.setOnClickListener(v ->
                showFeatureComingSoon());

        btnOrderDetail.setOnClickListener(v ->
                showFeatureComingSoon());

        return view;
    }

    // 🔒 Đăng xuất người dùng khi rời khỏi Fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FirebaseAuth.getInstance().signOut();
    }

    // ⚠️ Hàm thông báo chức năng chưa hoàn thiện
    private void showFeatureComingSoon() {
        // Toast hoặc AlertDialog tùy bạn
        android.widget.Toast.makeText(getContext(), "Chức năng đang phát triển", android.widget.Toast.LENGTH_SHORT).show();
    }
}
