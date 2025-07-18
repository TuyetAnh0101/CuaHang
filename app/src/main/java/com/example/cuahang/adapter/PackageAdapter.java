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
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.util.Locale;


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
        void onSoLuongChange(Package pkg, int newSoLuong);
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
        TextView txtChuKy, txtNgay, txtGioiHan, txtFree;
        TextView txtPackageType;
        CheckBox checkboxSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFirst = itemView.findViewById(R.id.imgFirst);
            txtTenGoi = itemView.findViewById(R.id.txtTenGoi);
            txtGiaGoc = itemView.findViewById(R.id.txtGiaGoc);
            txtGiaGiam = itemView.findViewById(R.id.txtGiaGiam);
            txtSoLuong = itemView.findViewById(R.id.txtSoLuong);
            txtChuKy = itemView.findViewById(R.id.txtChuKy);
            txtNgay = itemView.findViewById(R.id.txtNgay);
            txtGioiHan = itemView.findViewById(R.id.txtGioiHan);
            txtFree = itemView.findViewById(R.id.txtFree);
            txtPackageType = itemView.findViewById(R.id.txtPackageType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
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
        NumberFormat formatVn = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String giaGocFormatted = formatVn.format(pkg.getGiaGoc()).replace("₫", "₫").replace(".", ",");
        String giaGiamFormatted = formatVn.format(pkg.getGiaGiam()).replace("₫", "₫").replace(".", ",");
        holder.txtGiaGoc.setText("Giá gốc: " + giaGocFormatted);
        holder.txtGiaGiam.setText("Giá KM: " + giaGiamFormatted);

        holder.txtSoLuong.setText("SL còn: " + pkg.getSoLuong());
        holder.txtChuKy.setText("Chu kỳ: " + pkg.getBillingCycle());
        holder.txtNgay.setText("Từ: " + pkg.getStartDate() + " đến " + pkg.getEndDate());
        holder.txtGioiHan.setText("Giới hạn: " + pkg.getMaxPosts() + " bài, " + pkg.getMaxCharacters() + " ký tự/bài, " + pkg.getMaxImages() + " ảnh");
        holder.txtFree.setText("Gói miễn phí 3 post đầu: " + (pkg.isFree3Posts() ? "Có" : "Không"));

        String packageType = pkg.getPackageType();
        if (packageType == null || packageType.isEmpty()) {
            packageType = "Không xác định";
        }
        holder.txtPackageType.setText("Loại gói: " + packageType);

        if (pkg.getHinhAnh() != null && !pkg.getHinhAnh().isEmpty()) {
            try {
                String base64Image = pkg.getHinhAnh().get(0);
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imgFirst.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.imgFirst.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            holder.imgFirst.setImageResource(R.drawable.ic_launcher_foreground);
        }

        if (selectMode) {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.checkboxSelect.setVisibility(View.VISIBLE);

            holder.checkboxSelect.setOnCheckedChangeListener(null);
            holder.checkboxSelect.setChecked(selectedPackages.contains(pkg));
            holder.checkboxSelect.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) selectedPackages.add(pkg);
                else selectedPackages.remove(pkg);
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

    public void updateSoLuong(Package pkg, int newSoLuong) {
        pkg.setSoLuong(newSoLuong);
        notifyDataSetChanged();
        if (listener != null) listener.onSoLuongChange(pkg, newSoLuong);
    }
}
