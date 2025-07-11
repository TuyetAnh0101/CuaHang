package com.example.cuahang.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.Package;

import java.util.ArrayList;
import java.util.List;

public class PackageSelectAdapter extends RecyclerView.Adapter<PackageSelectAdapter.PackageViewHolder> {

    private final Context context;
    private List<Package> packageList;

    public PackageSelectAdapter(Context context, List<Package> packageList) {
        this.context = context;
        this.packageList = packageList;
    }

    public void setPackages(List<Package> packages) {
        this.packageList = packages;
        notifyDataSetChanged();
    }

    public List<Package> getSelectedPackages() {
        List<Package> selected = new ArrayList<>();
        for (Package p : packageList) {
            if (p.isSelected()) {
                p.setSoLuong(1); // Mặc định số lượng 1 khi được chọn
                selected.add(p);
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_package_select, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        Package pkg = packageList.get(position);

        holder.tvTenGoi.setText(pkg.getTenGoi());
        holder.tvGia.setText("Giá: " + pkg.getGiaGiam() + " đ");
        holder.checkbox.setChecked(pkg.isSelected());

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> pkg.setSelected(isChecked));

        holder.itemView.setOnClickListener(v -> {
            boolean checked = !holder.checkbox.isChecked();
            holder.checkbox.setChecked(checked);
        });
    }

    @Override
    public int getItemCount() {
        return packageList != null ? packageList.size() : 0;
    }

    static class PackageViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenGoi, tvGia;
        CheckBox checkbox;

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenGoi = itemView.findViewById(R.id.tvTenGoi);
            tvGia = itemView.findViewById(R.id.tvGia);
            checkbox = itemView.findViewById(R.id.checkboxPackage);
        }
    }
}
