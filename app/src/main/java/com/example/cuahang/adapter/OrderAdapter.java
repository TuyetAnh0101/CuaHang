package com.example.cuahang.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onEdit(Order order);
        void onDelete(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList, OnOrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_layout, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvNgayDat.setText("Ngày đặt: " + order.getNgayDat());
        holder.tvTongTien.setText("Tổng tiền: " + formatMoney(order.getTongTien()));
        holder.tvTrangThaiXuLy.setText("Xử lý: " + order.getStatusXuLy());
        holder.tvTrangThaiThanhToan.setText("Thanh toán: " + order.getStatusThanhToan());
        holder.tvSoLuong.setText("Tổng SL: " + order.getTongSoLuong());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(order);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(order);
        });
    }
    private String formatMoney(double amount) {
        return String.format("%,.0fđ", amount); // hoặc dùng NumberFormat
    }


    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvNgayDat, tvTongTien, tvTrangThaiXuLy, tvTrangThaiThanhToan, tvSoLuong;
        ImageView btnEdit, btnDelete;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNgayDat = itemView.findViewById(R.id.tvNgayDat);
            tvTongTien = itemView.findViewById(R.id.tvTongTien);
            tvTrangThaiXuLy = itemView.findViewById(R.id.tvTrangThaiXuLy);
            tvTrangThaiThanhToan = itemView.findViewById(R.id.tvTrangThaiThanhToan);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
