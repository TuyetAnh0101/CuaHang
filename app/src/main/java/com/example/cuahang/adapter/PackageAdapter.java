package com.example.cuahang.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.Package;

import java.util.ArrayList;
import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

    private final Context context;
    private final List<Package> list;
    private final OnPackageClickListener listener;
    private boolean selectMode = false;
    private final List<Package> selectedPackages = new ArrayList<>();

    public interface OnPackageClickListener {
        void onEdit(Package pkg);
        void onDelete(Package pkg);
    }

    public PackageAdapter(Context context, List<Package> list, OnPackageClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void setSelectMode(boolean mode) {
        this.selectMode = mode;
        selectedPackages.clear();
        notifyDataSetChanged();
    }

    public List<Package> getSelectedPackages() {
        return selectedPackages;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFirst, btnEdit, btnDelete;
        TextView txtTenGoi, txtGiaGoc, txtGiaGiam, txtSoLuong;
        LinearLayout layoutThumbnails;
        CheckBox checkboxSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFirst = itemView.findViewById(R.id.imgFirst);
            txtTenGoi = itemView.findViewById(R.id.txtTenGoi);
            txtGiaGoc = itemView.findViewById(R.id.txtGiaGoc);
            txtGiaGiam = itemView.findViewById(R.id.txtGiaGiam);
            txtSoLuong = itemView.findViewById(R.id.txtSoLuong);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            layoutThumbnails = itemView.findViewById(R.id.layoutThumbnails);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
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

        // Hiển thị ảnh chính
        if (pkg.getHinhAnh() != null && !pkg.getHinhAnh().isEmpty()) {
            try {
                String base64Image = pkg.getHinhAnh().get(0);
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imgFirst.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.imgFirst.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Hiển thị ảnh nhỏ
            holder.layoutThumbnails.removeAllViews();
            if (pkg.getHinhAnh().size() > 1) {
                holder.layoutThumbnails.setVisibility(View.VISIBLE);
                for (int i = 1; i < pkg.getHinhAnh().size(); i++) {
                    try {
                        String base64 = pkg.getHinhAnh().get(i);
                        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap thumbBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

                        ImageView thumb = new ImageView(context);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60, 60);
                        params.setMargins(4, 0, 4, 0);
                        thumb.setLayoutParams(params);
                        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        thumb.setImageBitmap(thumbBitmap);

                        holder.layoutThumbnails.addView(thumb);
                    } catch (Exception ignored) {}
                }
            } else {
                holder.layoutThumbnails.setVisibility(View.GONE);
            }
        } else {
            holder.imgFirst.setImageResource(R.drawable.ic_launcher_foreground);
            holder.layoutThumbnails.setVisibility(View.GONE);
        }

        // Hiển thị chế độ chọn hoặc quản lý
        if (selectMode) {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.checkboxSelect.setVisibility(View.VISIBLE);

            holder.checkboxSelect.setOnCheckedChangeListener(null);
            holder.checkboxSelect.setChecked(selectedPackages.contains(pkg));
            holder.checkboxSelect.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) {
                    if (!selectedPackages.contains(pkg)) selectedPackages.add(pkg);
                } else {
                    selectedPackages.remove(pkg);
                }
            });

        } else {
            holder.checkboxSelect.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(pkg);
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(pkg);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
