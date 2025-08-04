package com.example.cuahang.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class OrderPackageItemAdapter extends RecyclerView.Adapter<OrderPackageItemAdapter.OrderPackageViewHolder> {

    public interface OnBuyAgainClickListener {
        void onBuyAgain(String packageId, int quantity);
    }

    private List<Map<String, Object>> orderPackages;
    private OnBuyAgainClickListener listener;

    public OrderPackageItemAdapter(List<Map<String, Object>> orderPackages, OnBuyAgainClickListener listener) {
        this.orderPackages = orderPackages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderPackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_package, parent, false);
        return new OrderPackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderPackageViewHolder holder, int position) {
        Map<String, Object> pkg = orderPackages.get(position);

        String packageId = pkg.get("packageId") != null ? pkg.get("packageId").toString() : "";
        String packageName = pkg.get("tenGoi") != null ? pkg.get("tenGoi").toString() : "Không rõ";

        long quantityLong = 0;
        if (pkg.get("soLuong") instanceof Number) {
            quantityLong = ((Number) pkg.get("soLuong")).longValue();
        }
        final int quantity = (int) quantityLong; // ép kiểu an toàn

        double thanhTien = 0;
        if (pkg.get("thanhTien") instanceof Number) {
            thanhTien = ((Number) pkg.get("thanhTien")).doubleValue();
        }

        holder.tvPackageName.setText(packageName);
        holder.tvQuantity.setText("Số lượng đã mua: " + quantity);
        holder.tvThanhTien.setText("Tổng tiền: " + String.format("%,.0f", thanhTien) + "đ");

        holder.btnBuyAgain.setVisibility(View.GONE);

        FirebaseFirestore.getInstance()
                .collection("Package")
                .document(packageId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Long tonKho = doc.getLong("soLuong");

                        if (tonKho != null && tonKho >= quantity) {
                            holder.btnBuyAgain.setVisibility(View.VISIBLE);

                            holder.btnBuyAgain.setOnClickListener(v -> {
                                FirebaseFirestore.getInstance()
                                        .collection("Package")
                                        .document(packageId)
                                        .get()
                                        .addOnSuccessListener(checkDoc -> {
                                            Long tonKhoCheck = checkDoc.getLong("soLuong");
                                            if (tonKhoCheck != null && tonKhoCheck >= quantity) {
                                                if (listener != null) {
                                                    listener.onBuyAgain(packageId, quantity);
                                                }
                                            } else {
                                                Toast.makeText(holder.itemView.getContext(),
                                                        "Không đủ hàng trong kho để mua lại (" + tonKhoCheck + " còn lại)",
                                                        Toast.LENGTH_SHORT).show();
                                                holder.btnBuyAgain.setVisibility(View.GONE);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(holder.itemView.getContext(),
                                                    "Lỗi kiểm tra tồn kho, thử lại sau.",
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(),
                            "Không thể kiểm tra tồn kho.",
                            Toast.LENGTH_SHORT).show();
                    holder.btnBuyAgain.setVisibility(View.GONE);
                });
    }

    @Override
    public int getItemCount() {
        return orderPackages != null ? orderPackages.size() : 0;
    }

    public static class OrderPackageViewHolder extends RecyclerView.ViewHolder {
        TextView tvPackageName, tvQuantity, tvThanhTien;
        Button btnBuyAgain;

        public OrderPackageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPackageName = itemView.findViewById(R.id.tvPackageName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvThanhTien = itemView.findViewById(R.id.tvThanhTien);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);
        }
    }
}
