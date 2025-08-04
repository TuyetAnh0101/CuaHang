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
        void onBuyAgain(String packageId);
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
        String packageName = pkg.get("packageName") != null ? pkg.get("packageName").toString() : "Không rõ";

        long quantity = 0;
        if (pkg.get("quantity") instanceof Long) {
            quantity = (Long) pkg.get("quantity");
        } else if (pkg.get("quantity") instanceof Integer) {
            quantity = ((Integer) pkg.get("quantity")).longValue();
        }

        double thanhTien = 0;
        if (pkg.get("thanhTien") instanceof Double) {
            thanhTien = (Double) pkg.get("thanhTien");
        } else if (pkg.get("thanhTien") instanceof Long) {
            thanhTien = ((Long) pkg.get("thanhTien")).doubleValue();
        }

        holder.tvPackageName.setText(packageName);
        holder.tvQuantity.setText("Số lượng: " + quantity);
        holder.tvThanhTien.setText("Tổng tiền: " + String.format("%,.0f", thanhTien) + "đ");

        // Kiểm tra số lượng gói từ Firestore
        FirebaseFirestore.getInstance()
                .collection("Package")
                .document(packageId)
                .get()
                .addOnSuccessListener(doc -> {
                    Long soLuong = doc.getLong("soLuong");
                    boolean isAvailable = soLuong != null && soLuong > 0;

                    holder.btnBuyAgain.setVisibility(isAvailable ? View.VISIBLE : View.GONE);
                    holder.btnBuyAgain.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onBuyAgain(packageId);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderPackageAdapter", "Lỗi truy vấn Package: " + e.getMessage());
                    holder.btnBuyAgain.setVisibility(View.GONE);
                    Toast.makeText(holder.itemView.getContext(), "Không kiểm tra được số lượng gói.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return orderPackages.size();
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
