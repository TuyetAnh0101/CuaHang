package com.example.cuahang.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.Package;

import java.util.ArrayList;
import java.util.List;

public class PackageSelectAdapter extends RecyclerView.Adapter<PackageSelectAdapter.PackageViewHolder> {
    private Context context;
    private List<Package> packageList;

    public PackageSelectAdapter(Context context, List<Package> packageList) {
        this.context = context;
        this.packageList = packageList;
    }

    public void setPackages(List<Package> packages) {
        this.packageList = packages;
        notifyDataSetChanged();
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

        // Loại bỏ TextWatcher cũ trước khi gán mới (để tránh lỗi do tái sử dụng ViewHolder)
        if (holder.textWatcher != null) {
            holder.edtSoLuong.removeTextChangedListener(holder.textWatcher);
        }

        // Set giá trị số lượng hiện tại hoặc để trống nếu <= 0
        holder.edtSoLuong.setText(pkg.getSoLuong() > 0 ? String.valueOf(pkg.getSoLuong()) : "");

        // Tạo mới TextWatcher và lưu trong ViewHolder để có thể remove khi tái sử dụng
        holder.textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int soLuong = 0;
                try {
                    soLuong = Integer.parseInt(s.toString());
                } catch (NumberFormatException ignored) {}
                pkg.setSoLuong(soLuong);
            }
        };

        holder.edtSoLuong.addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return packageList == null ? 0 : packageList.size();
    }

    /**
     * Trả về danh sách các package có số lượng > 0, tức là đã được chọn
     */
    public List<Package> getSelectedPackages() {
        List<Package> selected = new ArrayList<>();
        for (Package p : packageList) {
            if (p.getSoLuong() > 0) {
                selected.add(p);
            }
        }
        return selected;
    }

    public static class PackageViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenGoi, tvGia;
        EditText edtSoLuong;
        TextWatcher textWatcher;  // giữ tham chiếu TextWatcher để remove

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenGoi = itemView.findViewById(R.id.tvTenGoi);
            tvGia = itemView.findViewById(R.id.tvGia);
            edtSoLuong = itemView.findViewById(R.id.edtSoLuong);
        }
    }
}
