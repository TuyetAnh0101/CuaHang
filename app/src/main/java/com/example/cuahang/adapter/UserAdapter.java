package com.example.cuahang.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onEdit(User user);
        void onDelete(User user);
    }

    private List<User> userList;
    private OnUserActionListener listener;

    public UserAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    public void setUserList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_account_layout, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvEmail, tvRole;
        ImageView btnEdit, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(User user, OnUserActionListener listener) {
            tvUserName.setText(user.getName());
            tvEmail.setText(user.getEmail());


            String roleDisplay = "Quyền: ";

            if (user.getRole() == null) {
                roleDisplay += "Chưa xác định";
            } else {
                switch (user.getRole()) {
                    case ADMIN:
                        roleDisplay += "Quản trị viên";
                        break;
                    case USER:
                        roleDisplay += "Người dùng";
                        break;
                    default:
                        roleDisplay += "Nhân viên";
                        break;
                }
            }
            tvRole.setText(roleDisplay);
            // Xử lý nút sửa
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(user);
                }
            });

            // Xử lý nút xóa
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(user);
                }
            });
        }

    }
}
