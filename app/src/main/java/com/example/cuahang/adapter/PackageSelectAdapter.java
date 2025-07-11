package com.example.cuahang.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

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
            if (p.isSelected() && p.getSoLuong() > 0) {
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
        holder.edtSoLuong.setText(pkg.getSoLuong() > 0 ? String.valueOf(pkg.getSoLuong()) : "");

        // Gỡ TextWatcher cũ
        if (holder.textWatcher != null) {
            holder.edtSoLuong.removeTextChangedListener(holder.textWatcher);
        }

        // Gán TextWatcher mới
        holder.textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int sl = Integer.parseInt(s.toString());
                    pkg.setSoLuong(sl);
                } catch (NumberFormatException e) {
                    pkg.setSoLuong(0);
                }
            }
        };
        class InputFilterMinMax implements InputFilter {
            private int min, max;

            public InputFilterMinMax(int min, int max) {
                this.min = min;
                this.max = max;
            }

            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    int input = Integer.parseInt(dest.toString() + source.toString());
                    if (isInRange(min, max, input))
                        return null;
                } catch (NumberFormatException nfe) { }
                return "";
            }

            private boolean isInRange(int a, int b, int c) {
                return b > a ? c >= a && c <= b : c >= b && c <= a;
            }
        }

        // Handle CheckBox
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> pkg.setSelected(isChecked));

        // Click cả item để toggle checkbox
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
        EditText edtSoLuong;
        CheckBox checkbox;
        TextWatcher textWatcher;

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenGoi = itemView.findViewById(R.id.tvTenGoi);
            tvGia = itemView.findViewById(R.id.tvGia);
            edtSoLuong = itemView.findViewById(R.id.edtSoLuong);
            checkbox = itemView.findViewById(R.id.checkboxPackage);
        }
    }
}
