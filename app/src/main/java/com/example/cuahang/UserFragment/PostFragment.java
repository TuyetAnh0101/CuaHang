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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.PostAdapter;
import com.example.cuahang.model.Package;
import com.example.cuahang.model.Post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class PostFragment extends Fragment {

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
    private Spinner spinnerDanhMuc;
    private List<Package> userPackages;
    private List<String> categoryNames;
    private Map<String, Package> categoryToPackage;

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

        recyclerPosts.setLayoutManager(new GridLayoutManager(getContext(), 2));
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
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        db.collection("Orders")
                .whereEqualTo("idKhach", uid)
                .whereEqualTo("statusThanhToan", "Đã thanh toán")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userPackages = new ArrayList<>();
                    categoryNames = new ArrayList<>();
                    categoryToPackage = new HashMap<>();

                    List<Map<String, Object>> allPackages = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        List<Map<String, Object>> packages = (List<Map<String, Object>>) doc.get("packages");
                        if (packages != null) {
                            allPackages.addAll(packages);
                        }
                    }

                    if (allPackages.isEmpty()) {
                        Toast.makeText(getContext(), "Bạn chưa mua gói tin hoặc gói đã hết hạn", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Đếm số lượng gói hợp lệ cần fetch để show dialog sau cùng
                    List<String> validPackageIds = new ArrayList<>();

                    for (Map<String, Object> pkg : allPackages) {
                        Object quantityObj = pkg.get("soLuong");  // đảm bảo dùng đúng key
                        int quantity = 0;

                        if (quantityObj instanceof Long) {
                            quantity = ((Long) quantityObj).intValue();
                        } else if (quantityObj instanceof Integer) {
                            quantity = (Integer) quantityObj;
                        } else {
                            Log.e("PostFragment", "❌ Giá trị soLuong không hợp lệ: " + quantityObj);
                            continue;
                        }

                        if (quantity > 0) {
                            String packageId = (String) pkg.get("packageId");
                            if (packageId != null && !packageId.isEmpty()) {
                                validPackageIds.add(packageId);
                            } else {
                                Log.e("PostFragment", "❌ packageId null hoặc rỗng: " + pkg);
                            }
                        }
                    }

                    if (validPackageIds.isEmpty()) {
                        Toast.makeText(getContext(), "Tất cả các gói đã hết lượt đăng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Đếm số gói đã tải về thành công để chỉ show dialog sau cùng
                    final int[] loadedCount = {0};
                    for (String packageId : validPackageIds) {
                        db.collection("Package").document(packageId).get().addOnSuccessListener(doc -> {
                            Package p = doc.toObject(Package.class);
                            if (p != null) {
                                userPackages.add(p);
                                categoryNames.add(p.getTenGoi());
                                categoryToPackage.put(p.getCategoryId(), p);
                            }

                            loadedCount[0]++;
                            if (loadedCount[0] == validPackageIds.size() && !userPackages.isEmpty()) {
                                showAddPostDialog();
                            }
                        }).addOnFailureListener(e -> {
                            Log.e("PostFragment", "❌ Lỗi khi tải gói tin: " + packageId, e);
                            loadedCount[0]++;
                            if (loadedCount[0] == validPackageIds.size() && !userPackages.isEmpty()) {
                                showAddPostDialog();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PostFragment", "❌ Lỗi khi truy vấn đơn hàng: ", e);
                    Toast.makeText(getContext(), "Lỗi khi kiểm tra đơn hàng", Toast.LENGTH_SHORT).show();
                });
    }


    private void checkUserOrderAndSetupUI() {
        FirebaseUser currentUser = auth.getCurrentUser();
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
                                Object quantityObj = pkg.get("soLuong");
                                int quantity = 0;

                                if (quantityObj instanceof Long) {
                                    quantity = ((Long) quantityObj).intValue();
                                } else if (quantityObj instanceof Integer) {
                                    quantity = (Integer) quantityObj;
                                } else {
                                    Log.e("PostFragment", "❌ Không thể đọc 'soLuong' từ package: " + pkg);
                                }

                                Log.d("PostFragment", "✅ Package: " + pkg + " | soLuong: " + quantity);

                                if (quantity > 0) {
                                    canPost = true;
                                    break;
                                }
                            }
                        } else {
                            Log.w("PostFragment", "⚠️ Gói tin rỗng hoặc null trong đơn hàng: " + doc.getId());
                        }

                        if (canPost) break;
                    }

                    fabPost.setVisibility(canPost ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("PostFragment", "❌ Lỗi khi kiểm tra đơn hàng: ", e);
                    fabPost.setVisibility(View.GONE);
                });
    }

    private void loadPosts() {
        progressLoading.setVisibility(View.VISIBLE);
        db.collection("Posts").get().addOnSuccessListener(queryDocumentSnapshots -> {
            postList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Post post = doc.toObject(Post.class);
                postList.add(post);
            }
            postAdapter.notifyDataSetChanged();
            progressLoading.setVisibility(View.GONE);
            txtEmpty.setVisibility(postList.isEmpty() ? View.VISIBLE : View.GONE);
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
        spinnerDanhMuc = dialogView.findViewById(R.id.spinnerDanhMuc);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDanhMuc.setAdapter(adapter);

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

                int pos = spinnerDanhMuc.getSelectedItemPosition();
                if (userPackages == null || userPackages.isEmpty()) {
                    Toast.makeText(getContext(), "Không có gói tin nào khả dụng", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pos < 0 || pos >= userPackages.size()) {
                    Toast.makeText(getContext(), "Vui lòng chọn gói tin hợp lệ", Toast.LENGTH_SHORT).show();
                    Log.e("PostFragment", "❌ Index spinner không hợp lệ: " + pos + " / size: " + userPackages.size());
                    return;
                }
                Package selectedPkg = userPackages.get(pos);

                // ✅ RÀNG BUỘC 1: maxPosts
                if (selectedPkg.getMaxPosts() <= 0) {
                    Toast.makeText(getContext(), "Bạn đã dùng hết lượt đăng của gói này!", Toast.LENGTH_LONG).show();
                    return;
                }

                // ✅ RÀNG BUỘC 2: maxCharacters
                int totalChars = title.length() + desc.length() + content.length();
                if (totalChars > selectedPkg.getMaxCharacters()) {
                    Toast.makeText(getContext(), "Tổng số ký tự vượt quá giới hạn cho phép (" + selectedPkg.getMaxCharacters() + " ký tự)", Toast.LENGTH_LONG).show();
                    return;
                }

                // ✅ RÀNG BUỘC 3: maxImages
                if (selectedImageUris.size() > selectedPkg.getMaxImages()) {
                    Toast.makeText(getContext(), "Bạn chỉ được đăng tối đa " + selectedPkg.getMaxImages() + " ảnh!", Toast.LENGTH_LONG).show();
                    return;
                }

                long price = Long.parseLong(priceText);
                FirebaseUser user = auth.getCurrentUser();
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

                // ✅ Trừ lượt maxPosts còn lại
                int newMaxPost = selectedPkg.getMaxPosts() - 1;
                selectedPkg.setMaxPosts(newMaxPost);
                db.collection("Package").document(selectedPkg.getId()).update("maxPosts", newMaxPost);

                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                Post newPost = new Post("", user.getUid(), selectedPkg.getId(), selectedPkg.getCategoryId(), title, desc, content, imageList, price, date, "hiển thị");

                db.collection("Posts")
                        .add(newPost)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(getContext(), "Đăng bài thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadPosts();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Đăng bài thất bại", Toast.LENGTH_SHORT).show();
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