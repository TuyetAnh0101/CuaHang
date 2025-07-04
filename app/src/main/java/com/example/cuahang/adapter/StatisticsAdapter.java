package com.example.cuahang.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.Statistics;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatisticsViewHolder> {

    private List<Statistics> statisticsList;
    private StatisticsClickListener listener;

    // Interface xử lý sự kiện Sửa / Xóa
    public interface StatisticsClickListener {
        void onEdit(Statistics statistics);
        void onDelete(Statistics statistics);
    }

    public StatisticsAdapter(List<Statistics> statisticsList, StatisticsClickListener listener) {
        this.statisticsList = statisticsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StatisticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_statistic_layout, parent, false);
        return new StatisticsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticsViewHolder holder, int position) {
        Statistics stats = statisticsList.get(position);

        // Định dạng tiền tệ (có dấu chấm phân cách)
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String revenueStr = formatter.format(stats.getTotalRevenue()) + "đ";

        holder.tvDate.setText("Ngày: " + stats.getDate());
        holder.tvRevenue.setText("Doanh thu: " + revenueStr);
        holder.tvOrders.setText("Đơn hàng: " + stats.getTotalOrders());
        holder.tvPackages.setText("Gói đã bán: " + stats.getPackagesSold());
        holder.tvUsers.setText("Người dùng mới: " + stats.getNewUsers());

        // Top 3 danh mục phổ biến
        StringBuilder topCats = new StringBuilder("Top danh mục: ");
        Map<String, Integer> categoryMap = stats.getTopCategories();
        if (categoryMap != null && !categoryMap.isEmpty()) {
            int count = 0;
            for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
                topCats.append(entry.getKey())
                        .append(" (")
                        .append(entry.getValue())
                        .append(")");
                count++;
                if (count >= 3) break;
                topCats.append(", ");
            }
        } else {
            topCats.append("Không có");
        }
        holder.tvTopCategories.setText(topCats.toString());

        // Gán sự kiện Sửa
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(stats);
        });

        // Gán sự kiện Xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(stats);
        });
    }

    @Override
    public int getItemCount() {
        return statisticsList.size();
    }

    public static class StatisticsViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvRevenue, tvOrders, tvPackages, tvUsers, tvTopCategories;
        Button btnEdit, btnDelete;

        public StatisticsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
            tvOrders = itemView.findViewById(R.id.tvOrders);
            tvPackages = itemView.findViewById(R.id.tvPackages);
            tvUsers = itemView.findViewById(R.id.tvUsers);
            tvTopCategories = itemView.findViewById(R.id.tvTopCategories);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
