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
// Bạn sẽ thêm các Activity tương ứng nếu có:
// import com.example.cuahang.manager.InvoiceActivity;
// import com.example.cuahang.manager.UserAccountActivity;
// import com.example.cuahang.manager.OrderActivity;
// import com.example.cuahang.manager.OrderDetailActivity;

public class UserFragment extends Fragment {

    public UserFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        Button btnCategory = view.findViewById(R.id.btnCategory);
        Button btnPackage = view.findViewById(R.id.btnPackage);
        Button btnInvoice = view.findViewById(R.id.btnInvoice);
        Button btnUser = view.findViewById(R.id.btnUser);
        Button btnOrder = view.findViewById(R.id.btnOrder);
        Button btnOrderDetail = view.findViewById(R.id.btnOrderDetail);

        btnCategory.setOnClickListener(v ->
                startActivity(new Intent(getContext(), CategoryActivity.class)));

        btnPackage.setOnClickListener(v ->
                startActivity(new Intent(getContext(), PackageActivity.class)));

        // Các intent khác bạn chỉ cần tạo thêm Activity rồi gọi như sau:
        /*
        btnInvoice.setOnClickListener(v ->
                startActivity(new Intent(getContext(), InvoiceActivity.class)));

        btnUser.setOnClickListener(v ->
                startActivity(new Intent(getContext(), UserAccountActivity.class)));

        btnOrder.setOnClickListener(v ->
                startActivity(new Intent(getContext(), OrderActivity.class)));

        btnOrderDetail.setOnClickListener(v ->
                startActivity(new Intent(getContext(), OrderDetailActivity.class)));
        */

        return view;
    }
}
