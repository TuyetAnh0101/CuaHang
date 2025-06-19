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
import com.example.cuahang.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<Category> categoryList;

    private OnCategoryEditListener editListener;
    private OnCategoryDeleteListener deleteListener;
    private OnCategoryClickListener clickListener;

    // Giao diện xử lý sự kiện sửa
    public interface OnCategoryEditListener {
        void onEdit(Category category);
    }

    // Giao diện xử lý sự kiện xóa
    public interface OnCategoryDeleteListener {
        void onDelete(Category category);
    }

    // Giao diện xử lý sự kiện click xem danh sách gói đăng ký
    public interface OnCategoryClickListener {
        void onClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categoryList,
                           OnCategoryEditListener editListener,
                           OnCategoryDeleteListener deleteListener,
                           OnCategoryClickListener clickListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryName;
        ImageView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.txtCategoryName.setText(category.getName());

        // Nếu có sự kiện sửa
        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(category);
            }
        });

        // Nếu có sự kiện xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(category);
            }
        });

        // Nếu có sự kiện click item
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
