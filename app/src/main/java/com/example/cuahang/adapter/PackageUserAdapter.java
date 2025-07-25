package com.example.cuahang.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.Package;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PackageUserAdapter extends RecyclerView.Adapter<PackageUserAdapter.PackageViewHolder> {

    private final Context context;
    private List<Package> packageList;
    private final List<Package> originalList;
    private final OnBuyClickListener buyClickListener;
    private OnItemClickListener itemClickListener;

    public interface OnBuyClickListener {
        void onBuyClick(Package item);
    }

    public interface OnItemClickListener {
        void onItemClick(Package item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public PackageUserAdapter(Context context, List<Package> packageList, OnBuyClickListener listener) {
        this.context = context;
        this.packageList = new ArrayList<>(packageList);
        this.originalList = new ArrayList<>(packageList); // ⭐ Lưu lại danh sách gốc ban đầu
        this.buyClickListener = listener;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_package, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        Package pkg = packageList.get(position);

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
        String giaGiamFormatted = decimalFormat.format(pkg.getGiaGiam()) + " đ";

        holder.tvTenGoi.setText(pkg.getTenGoi());
        holder.tvMoTa.setText(pkg.getMoTa());
        holder.tvGiaGiam.setText("Giá giảm: " + giaGiamFormatted);
        holder.tvSoLuong.setText("Số lượng: " + pkg.getSoLuong());
        holder.tvBilling.setText("Chu kỳ: " + pkg.getBillingCycle());
        holder.tvThoiGian.setText("Thời gian: " + pkg.getStartDate() + " - " + pkg.getEndDate());

        // Khi nhấn nút "Mua gói"
        holder.btnMuaGoi.setOnClickListener(v -> {
            Log.d("PACKAGE_ADAPTER", "Mua gói: " + pkg.getTenGoi());
            buyClickListener.onBuyClick(pkg);
        });

        // Khi click toàn bộ item
        holder.itemView.setOnClickListener(v -> {
            Log.d("PACKAGE_ADAPTER", "Click item: " + pkg.getTenGoi());
            if (itemClickListener != null) {
                itemClickListener.onItemClick(pkg);
            }
        });
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

    // Cập nhật danh sách khi lọc/tìm kiếm
    public void updateList(List<Package> newList) {
        this.packageList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    // ⭐ Trả về danh sách gốc ban đầu
    public List<Package> getOriginalList() {
        return originalList;
    }

    public static class PackageViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenGoi, tvMoTa, tvGiaGiam, tvSoLuong, tvBilling, tvThoiGian;
        Button btnMuaGoi;

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenGoi = itemView.findViewById(R.id.tvTenGoi);
            tvMoTa = itemView.findViewById(R.id.tvMoTa);
            tvGiaGiam = itemView.findViewById(R.id.tvGiaGiam);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvBilling = itemView.findViewById(R.id.tvBilling);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
            btnMuaGoi = itemView.findViewById(R.id.btnMuaGoi);
        }
    }
}
