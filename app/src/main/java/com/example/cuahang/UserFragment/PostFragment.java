package com.example.cuahang.UserFragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.PostAdapter;
import com.example.cuahang.model.Post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class PostFragment extends Fragment {

    private static final String TAG = "POST_FRAGMENT";
    private static final int REQUEST_CODE_PICK_IMAGE = 101;

    private RecyclerView recyclerPosts;
    private FloatingActionButton fabPost;
    private ProgressBar progressLoading;
    private TextView txtEmpty;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<Uri> selectedImageUris;
    private ImageView imgPreview;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        recyclerPosts = view.findViewById(R.id.recyclerPosts);
        fabPost = view.findViewById(R.id.fabPost);
        progressLoading = view.findViewById(R.id.progressLoading);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerPosts.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 cột
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerPosts.setAdapter(postAdapter);
        selectedImageUris = new ArrayList<>();

        checkUserOrderAndSetupUI();
        loadPosts();

        fabPost.setOnClickListener(view1 -> handlePostClick());

        return view;
    }

    private void handlePostClick() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        db.collection("Orders")
                .whereEqualTo("idKhach", uid)
                .whereEqualTo("statusThanhToan", "Đã thanh toán")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean hasValidPackage = false;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        List<Map<String, Object>> packages = (List<Map<String, Object>>) doc.get("packages");
                        if (packages != null) {
                            for (Map<String, Object> pkg : packages) {
                                Long quantityLong = (Long) pkg.get("quantity");
                                int quantity = quantityLong != null ? quantityLong.intValue() : 0;

                                if (quantity > 0) {
                                    hasValidPackage = true;
                                    break;
                                }
                            }
                        }

                        if (hasValidPackage) break;
                    }

                    if (hasValidPackage) {
                        showAddPostDialog();
                    } else {
                        Toast.makeText(getContext(), "Bạn chưa mua gói tin hoặc gói đã hết hạn", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi kiểm tra đơn hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserOrderAndSetupUI() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        db.collection("Orders")
                .whereEqualTo("idKhach", uid)
                .whereEqualTo("statusThanhToan", "Đã thanh toán")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean canPost = false;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        List<Map<String, Object>> packages = (List<Map<String, Object>>) doc.get("packages");
                        if (packages != null) {
                            for (Map<String, Object> pkg : packages) {
                                Long quantityLong = (Long) pkg.get("quantity");
                                int quantity = quantityLong != null ? quantityLong.intValue() : 0;

                                if (quantity > 0) {
                                    canPost = true;
                                    break;
                                }
                            }
                        }

                        if (canPost) break;
                    }

                    fabPost.setVisibility(canPost ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi kiểm tra đơn hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPosts() {
        progressLoading.setVisibility(View.VISIBLE);

        db.collection("Posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        postList.add(post);
                    }
                    postAdapter.notifyDataSetChanged();
                    progressLoading.setVisibility(View.GONE);
                    txtEmpty.setVisibility(postList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressLoading.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi tải bài đăng", Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddPostDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.add_post, null);

        EditText edtTieuDe = dialogView.findViewById(R.id.edtTieuDe);
        EditText edtMoTa = dialogView.findViewById(R.id.edtMoTa);
        EditText edtNoiDung = dialogView.findViewById(R.id.edtNoiDung);
        EditText edtGia = dialogView.findViewById(R.id.edtGia);
        imgPreview = dialogView.findViewById(R.id.imgPreview);
        Button btnChonAnh = dialogView.findViewById(R.id.btnChonAnh);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Thêm bài đăng mới")
                .setView(dialogView)
                .setPositiveButton("Đăng", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnPost = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnPost.setOnClickListener(v -> {
                String title = edtTieuDe.getText().toString().trim();
                String desc = edtMoTa.getText().toString().trim();
                String content = edtNoiDung.getText().toString().trim();
                String priceText = edtGia.getText().toString().trim();

                if (title.isEmpty() || content.isEmpty() || priceText.isEmpty()) {
                    Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                long price = Long.parseLong(priceText);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) return;

                List<String> imageList = new ArrayList<>();
                for (Uri uri : selectedImageUris) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                        byte[] imageBytes = baos.toByteArray();
                        String encoded = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        imageList.add(encoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                Post newPost = new Post("", user.getUid(), "", "", title, desc, content, imageList, price, date, "hiển thị");

                db.collection("Posts")
                        .add(newPost)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(getContext(), "Đăng bài thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadPosts();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Đăng bài thất bại", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });
            });
        });

        btnChonAnh.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "Chọn nhiều ảnh"), REQUEST_CODE_PICK_IMAGE);
        });

        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUris.clear();
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }

            if (!selectedImageUris.isEmpty() && imgPreview != null) {
                imgPreview.setImageURI(selectedImageUris.get(0));
            }
        }
    }
}