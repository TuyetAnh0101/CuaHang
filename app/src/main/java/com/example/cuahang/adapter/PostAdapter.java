package com.example.cuahang.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.model.Post;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private final List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.tvTieuDe.setText(post.getTieuDe());
        holder.tvMoTa.setText(post.getMoTa());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvGia.setText(format.format(post.getGia()));

        // Hiển thị 1 ảnh đầu tiên
        List<String> imageList = post.getListImageBase64();
        if (imageList != null && !imageList.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(imageList.get(0), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgThumb.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.imgThumb.setImageResource(R.drawable.ic_launcher_background); // ảnh mặc định
            }
        } else {
            holder.imgThumb.setImageResource(R.drawable.ic_launcher_background); // ảnh mặc định
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTieuDe, tvMoTa, tvGia;
        ImageView imgThumb;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
            tvMoTa = itemView.findViewById(R.id.tvMoTa);
            tvGia = itemView.findViewById(R.id.tvGia);
            imgThumb = itemView.findViewById(R.id.imgThumb);
        }
    }
}

