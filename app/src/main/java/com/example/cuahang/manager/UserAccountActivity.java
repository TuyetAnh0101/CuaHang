package com.example.cuahang.manager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.UserAdapter;
import com.example.cuahang.model.Role;
import com.example.cuahang.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserAccountActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseFirestore db;
    private FloatingActionButton fabAddUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        recyclerView = findViewById(R.id.recyclerUserAccounts);
        fabAddUser = findViewById(R.id.fabAddUser);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onEdit(User user) {
                Toast.makeText(UserAccountActivity.this, "Chỉnh sửa: " + user.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Hiển thị dialog chỉnh sửa nếu cần
            }

            @Override
            public void onDelete(User user) {
                deleteUser(user);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);

        fabAddUser.setOnClickListener(v -> showAddUserDialog());

        loadUsersFromFirestore();
    }

    private void loadUsersFromFirestore() {
        db.collection("User")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (var doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        user.setId(doc.getId());
                        userList.add(user);
                    }
                    userAdapter.setUserList(userList);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải người dùng", Toast.LENGTH_SHORT).show());
    }

    private void deleteUser(@NonNull User user) {
        db.collection("User").document(user.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã xóa " + user.getName(), Toast.LENGTH_SHORT).show();
                    loadUsersFromFirestore();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Xoá thất bại", Toast.LENGTH_SHORT).show());
    }

    private void showAddUserDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_user_account_layout, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        EditText edtName = dialogView.findViewById(R.id.edtUserName);
        EditText edtEmail = dialogView.findViewById(R.id.edtEmail);
        EditText edtPassword = dialogView.findViewById(R.id.edtPassword);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
        Button btnSave = dialogView.findViewById(R.id.btnSaveUser);
        Button btnChooseAvatar = dialogView.findViewById(R.id.btnChooseAvatar);
        ImageView imgAvatar = dialogView.findViewById(R.id.imgAvatarPreview);

        // Ẩn không cần thiết
        edtPassword.setVisibility(View.GONE);
        btnChooseAvatar.setVisibility(View.GONE);
        imgAvatar.setVisibility(View.GONE);

        // Adapter role
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"STAFF", "MANAGER", "ADMIN"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Adapter trạng thái
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Hoạt động", "Đã khóa"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String roleStr = spinnerRole.getSelectedItem().toString();
            boolean active = spinnerStatus.getSelectedItemPosition() == 0;

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            String dummyPassword = "123456";

            mAuth.createUserWithEmailAndPassword(email, dummyPassword)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            mAuth.sendPasswordResetEmail(email)
                                    .addOnSuccessListener(unused -> Toast.makeText(this, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi email: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                            User newUser = new User();
                            newUser.setId(uid);
                            newUser.setEmail(email);
                            newUser.setName(name);
                            newUser.setRole(Role.fromString(roleStr));
                            newUser.setActive(active);

                            db.collection("User").document(uid)
                                    .set(newUser)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                                        userList.add(newUser);
                                        userAdapter.notifyItemInserted(userList.size() - 1);
                                        dialog.dismiss();
                                        mAuth.signOut();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạo tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsersFromFirestore();
    }
}
