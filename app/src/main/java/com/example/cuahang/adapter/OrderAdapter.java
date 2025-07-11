package com.example.cuahang.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.Order;
import com.google.firebase.database.FirebaseDatabase;

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
        holder.tvTongTien.setText("Tổng tiền: " + order.getTongTien() + "đ");
        holder.tvTrangThaiXuLy.setText("Xử lý: " + order.getStatusXuLy());
        holder.tvTrangThaiThanhToan.setText("Thanh toán: " + order.getStatusThanhToan());
        holder.tvSoLuong.setText("Tổng SL: " + order.getTongSoLuong());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(order);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (order.getId() == null || order.getId().isEmpty()) {
                Toast.makeText(context, "Không thể xóa: ID đơn hàng không tồn tại.", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d("OrderAdapter", "Xóa đơn hàng với ID: " + order.getId());

            new AlertDialog.Builder(context)
                    .setTitle("Xóa đơn hàng")
                    .setMessage("Bạn có chắc muốn xóa đơn hàng này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("Orders")
                                .child(order.getId())
                                .removeValue()
                                .addOnSuccessListener(unused -> {
                                    orderList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Đã xóa đơn hàng", Toast.LENGTH_SHORT).show();
                                    if (listener != null) listener.onDelete(order);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
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
