package com.example.cuahang.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.OrderPackage;

import java.util.List;

public class OrderPackageAdapter extends RecyclerView.Adapter<OrderPackageAdapter.PackageViewHolder> {

    private final Context context;
    private final List<OrderPackage> packages;

    public OrderPackageAdapter(Context context, List<OrderPackage> packages) {
        this.context = context;
        this.packages = packages;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_package_order, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        OrderPackage pack = packages.get(position);
        holder.tvTenGoi.setText(pack.getTenGoi());
        holder.tvSoLuong.setText("Số lượng: " + pack.getSoLuong());
        holder.tvGia.setText("Giá: " + String.format("%,.0fđ", pack.getGiaGiam()));
    }

    @Override
    public int getItemCount() {
        return packages != null ? packages.size() : 0;
    }

    public static class PackageViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenGoi, tvSoLuong, tvGia;

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenGoi = itemView.findViewById(R.id.tvTenGoi);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvGia = itemView.findViewById(R.id.tvGia);
        }
    }
}
