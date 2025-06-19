package com.example.cuahang.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cuahang.R;
import com.example.cuahang.model.Package;

import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

    private List<Package> list;
    private Context context;

    public PackageAdapter(Context context, List<Package> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFirst;
        TextView txtTenGoi, txtGiaGoc, txtGiaGiam, txtSoLuong;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFirst = itemView.findViewById(R.id.imgFirst);
            txtTenGoi = itemView.findViewById(R.id.txtTenGoi);
            txtGiaGoc = itemView.findViewById(R.id.txtGiaGoc);
            txtGiaGiam = itemView.findViewById(R.id.txtGiaGiam);
            txtSoLuong = itemView.findViewById(R.id.txtSoLuong);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_package_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageAdapter.ViewHolder holder, int position) {
        Package pkg = list.get(position);

        holder.txtTenGoi.setText(pkg.getTenGoi());
        holder.txtGiaGoc.setText("Giá gốc: " + pkg.getGiaGoc() + "đ");
        holder.txtGiaGiam.setText("Giá giảm: " + pkg.getGiaGiam() + "đ");
        holder.txtSoLuong.setText("Số lượng: " + pkg.getSoLuong());

        // Hiển thị ảnh đầu tiên nếu có
        if (pkg.getHinhAnh() != null && !pkg.getHinhAnh().isEmpty()) {
            Glide.with(context).load(pkg.getHinhAnh().get(0)).into(holder.imgFirst);
        } else {
            holder.imgFirst.setImageResource(R.drawable.ic_launcher_foreground); // icon mặc định
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
