package com.example.cuahang.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.Invoices;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InvoicesAdapter extends RecyclerView.Adapter<InvoicesAdapter.InvoiceViewHolder> {

    public interface OnInvoiceActionListener {
        void onEdit(Invoices invoice);
        void onDelete(Invoices invoice);
    }

    private List<Invoices> invoiceList;
    private final OnInvoiceActionListener listener;

    public InvoicesAdapter(List<Invoices> invoiceList, OnInvoiceActionListener listener) {
        this.invoiceList = invoiceList;
        this.listener = listener;
    }

    public void setInvoiceList(List<Invoices> list) {
        this.invoiceList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invoices_layout, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        Invoices invoice = invoiceList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        holder.tvInvoiceId.setText("🧾 Hóa đơn ID: " + invoice.getId());

        if (invoice.getCreatedAt() != null) {
            holder.tvNgayTao.setText("📅 Ngày tạo: " + sdf.format(invoice.getCreatedAt()));
        } else {
            holder.tvNgayTao.setText("📅 Ngày tạo: (trống)");
        }

        holder.tvNhanVien.setText("👤 Nhân viên: " + invoice.getCreatedBy());
        holder.tvTongSoLuong.setText("🔢 Tổng SL: " + invoice.getTotalQuantity());
        holder.tvTongGia.setText("💰 Tổng giá: " + formatMoney(invoice.getTotalPrice()));
        holder.tvTongGiamGia.setText("💸 Giảm giá: " + formatMoney(invoice.getTotalDiscount()));
        holder.tvTongVAT.setText("💼 VAT: " + formatMoney(invoice.getTotalTax()));
        holder.tvTongTien.setText("🧾 Tổng thanh toán: " + formatMoney(invoice.getTotalAmount()));

        String statusText = invoice.getStatus() != null ? invoice.getStatus() : "Chưa xác định";
        holder.tvStatus.setText("📌 Trạng thái: " + statusText);

        if ("Đã thanh toán".equalsIgnoreCase(statusText)) {
            holder.tvStatus.setTextColor(holder.itemView.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getResources().getColor(android.R.color.holo_orange_dark));
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(invoice);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(invoice);
            }
        });
    }

    @Override
    public int getItemCount() {
        return invoiceList != null ? invoiceList.size() : 0;
    }

    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceId, tvNgayTao, tvNhanVien, tvTongSoLuong,
                tvTongGia, tvTongGiamGia, tvTongVAT, tvTongTien, tvStatus;
        ImageView btnEdit, btnDelete;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceId = itemView.findViewById(R.id.tvInvoiceId);
            tvNgayTao = itemView.findViewById(R.id.tvNgayTao);
            tvNhanVien = itemView.findViewById(R.id.tvNhanVien);
            tvTongSoLuong = itemView.findViewById(R.id.tvTongSoLuong);
            tvTongGia = itemView.findViewById(R.id.tvTongGia);
            tvTongGiamGia = itemView.findViewById(R.id.tvTongGiamGia);
            tvTongVAT = itemView.findViewById(R.id.tvTongVAT);
            tvTongTien = itemView.findViewById(R.id.tvTongTien);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private String formatMoney(double amount) {
        return String.format(Locale.getDefault(), "%,.0fđ", amount);
    }
}
