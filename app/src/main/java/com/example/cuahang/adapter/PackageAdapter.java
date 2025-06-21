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

    private final Context context;
    private final List<Package> list;
    private final OnPackageClickListener listener;

    // Giao diện callback để xử lý sự kiện Sửa/Xóa
    public interface OnPackageClickListener {
        void onEdit(Package pkg);
        void onDelete(Package pkg);
    }

    public PackageAdapter(Context context, List<Package> list, OnPackageClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFirst, btnEdit, btnDelete;
        TextView txtTenGoi, txtGiaGoc, txtGiaGiam, txtSoLuong;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFirst = itemView.findViewById(R.id.imgFirst);
            txtTenGoi = itemView.findViewById(R.id.txtTenGoi);
            txtGiaGoc = itemView.findViewById(R.id.txtGiaGoc);
            txtGiaGiam = itemView.findViewById(R.id.txtGiaGiam);
            txtSoLuong = itemView.findViewById(R.id.txtSoLuong);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_package_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Package pkg = list.get(position);

        holder.txtTenGoi.setText(pkg.getTenGoi());
        holder.txtGiaGoc.setText("Giá gốc: " + pkg.getGiaGoc() + "đ");
        holder.txtGiaGiam.setText("Giá giảm: " + pkg.getGiaGiam() + "đ");
        holder.txtSoLuong.setText("Số lượng: " + pkg.getSoLuong());

        // Load ảnh đầu tiên
        if (pkg.getHinhAnh() != null && !pkg.getHinhAnh().isEmpty()) {
            Glide.with(context)
                    .load(pkg.getHinhAnh().get(0))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.imgFirst);
        } else {
            holder.imgFirst.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Sự kiện sửa & xóa
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(pkg));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(pkg));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
