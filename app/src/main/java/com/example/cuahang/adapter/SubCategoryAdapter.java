package com.example.cuahang.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.SubCategory;

import java.util.List;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.ViewHolder> {

    // Giao diện xử lý sự kiện chỉnh sửa và xoá
    public interface OnSubCategoryClickListener {
        void onEdit(SubCategory subCategory);
        void onDelete(SubCategory subCategory);
    }

    private final Context context;
    private final List<SubCategory> subCategoryList;
    private final OnSubCategoryClickListener listener;

    // Giao diện xử lý sự kiện click toàn bộ item
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(SubCategory subCategory);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public SubCategoryAdapter(Context context, List<SubCategory> subCategoryList, OnSubCategoryClickListener listener) {
        this.context = context;
        this.subCategoryList = subCategoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subcategory_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubCategory sub = subCategoryList.get(position);
        holder.txtName.setText(sub.getName());

        // Xử lý nút Sửa
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(sub);
        });

        // Xử lý nút Xoá
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(sub);
        });

        // Click toàn item
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onItemClick(sub);
        });
    }

    @Override
    public int getItemCount() {
        return subCategoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtSubCategoryName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
