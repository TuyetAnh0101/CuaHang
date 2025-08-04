package com.example.cuahang.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;

import java.util.List;
import java.util.Map;

public class InvoicePackageAdapter extends RecyclerView.Adapter<InvoicePackageAdapter.PackageViewHolder> {

    private final List<Map<String, Object>> packageList;

    public InvoicePackageAdapter(List<Map<String, Object>> packageList) {
        this.packageList = packageList;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package_invoice, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        Map<String, Object> pack = packageList.get(position);

        String tenGoi = (String) pack.get("tenGoi");
        Double gia = (Double) pack.get("gia");
        Long soLuong = (Long) pack.get("soLuong");

        holder.tvTenGoi.setText("Gói: " + tenGoi);
        holder.tvGia.setText("Giá: " + gia + "đ");
        holder.tvSoLuong.setText("Số lượng: " + soLuong);
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

    public static class PackageViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenGoi, tvGia, tvSoLuong;

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenGoi = itemView.findViewById(R.id.tvTenGoi);
            tvGia = itemView.findViewById(R.id.tvGia);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
        }
    }
}
