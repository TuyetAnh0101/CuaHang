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
import com.example.cuahang.manager.Invoicesctivity;
import com.example.cuahang.manager.PackageActivity;
import com.example.cuahang.manager.OrderActivity;
import com.example.cuahang.manager.StatisticsActivity;
import com.example.cuahang.manager.StatisticsOverviewActivity;
import com.example.cuahang.manager.SystemConfigActivity;
import com.example.cuahang.manager.UserAccountActivity;
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
            startActivity(loginIntent);
            if (getActivity() != null) getActivity().finish();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_user, container, false);

        Button btnCategory = view.findViewById(R.id.btnCategory);
        Button btnPackage = view.findViewById(R.id.btnPackage);
        Button btnInvoice = view.findViewById(R.id.btnInvoice);
        Button btnUser = view.findViewById(R.id.btnUser);
        Button btnOrder = view.findViewById(R.id.btnOrder);
        Button btnStatisticsOverview = view.findViewById(R.id.btnStatisticsOverview);
        Button btnSystemConfig = view.findViewById(R.id.btnSystemConfig); // 👈 Thêm dòng này

        btnCategory.setOnClickListener(v ->
                startActivity(new Intent(getContext(), CategoryActivity.class)));

        btnPackage.setOnClickListener(v ->
                startActivity(new Intent(getContext(), PackageActivity.class)));

        btnInvoice.setOnClickListener(v ->
                startActivity(new Intent(getContext(), Invoicesctivity.class)));

        btnUser.setOnClickListener(v ->
                startActivity(new Intent(getContext(), UserAccountActivity.class)));

        btnOrder.setOnClickListener(v ->
                startActivity(new Intent(getContext(), OrderActivity.class)));

        btnStatisticsOverview.setOnClickListener(v ->
                startActivity(new Intent(getContext(), StatisticsOverviewActivity.class)));

        btnSystemConfig.setOnClickListener(v ->
                startActivity(new Intent(getContext(), SystemConfigActivity.class)));

        return view;
    }

    private void showFeatureComingSoon() {
        android.widget.Toast.makeText(getContext(), "Chức năng đang phát triển", android.widget.Toast.LENGTH_SHORT).show();
    }
}
