package com.example.cuahang.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.Category;

import java.util.List;

public class CategoryHorizontalAdapter extends RecyclerView.Adapter<CategoryHorizontalAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<Category> categoryList;
    private int selectedPosition = 0;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryHorizontalAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_horizontal, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        bindCategory(holder, position);
    }

    private void bindCategory(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvName.setText(category.getName());

        if (selectedPosition == position) {
            holder.tvName.setBackgroundResource(R.drawable.bg_category_selected);
            holder.tvName.setTextColor(Color.WHITE);
        } else {
            holder.tvName.setBackgroundResource(R.drawable.bg_category_unselected);
            holder.tvName.setTextColor(Color.BLACK);
        }

        holder.tvName.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            listener.onCategoryClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.txtCategoryName);
        }
    }

    // Hàm mới để tạo view theo vị trí
    public View createViewForCategory(int position, LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.item_category_horizontal, container, false);
        CategoryViewHolder holder = new CategoryViewHolder(view);
        bindCategory(holder, position);
        return view;
    }
}
