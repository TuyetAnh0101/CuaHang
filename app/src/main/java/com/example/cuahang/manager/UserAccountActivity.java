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
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

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
                showEditUserDialog(user);
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
                    if (queryDocumentSnapshots.isEmpty()) {
                        createDefaultAdminAccount();
                    } else {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            User user = doc.toObject(User.class);
                            user.setId(doc.getId());
                            userList.add(user);
                        }
                        userAdapter.setUserList(userList);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải người dùng", Toast.LENGTH_SHORT).show());
    }

    private void createDefaultAdminAccount() {
        String email = "admin@gmail.com";
        String password = "anh123";
        String name = "Admin Default";

        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> methods = task.getResult().getSignInMethods();
                if (methods == null || methods.isEmpty()) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                FirebaseUser firebaseUser = authResult.getUser();
                                if (firebaseUser != null) {
                                    saveUserToFirestore(firebaseUser.getUid(), "AD01", email, name, Role.ADMIN, true);
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạo admin mặc định: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void saveUserToFirestore(String uid, String userId, String email, String name, Role role, boolean active) {
        User user = new User();
        user.setId(uid);
        user.setUserId(userId);
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        user.setActive(active);

        db.collection("User").document(uid).set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã lưu tài khoản " + userId, Toast.LENGTH_SHORT).show();
                    userList.add(user);
                    userAdapter.setUserList(userList);
                    mAuth.signOut();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create();

        EditText edtName = dialogView.findViewById(R.id.edtUserName);
        EditText edtEmail = dialogView.findViewById(R.id.edtEmail);
        EditText edtPassword = dialogView.findViewById(R.id.edtPassword);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
        Button btnSave = dialogView.findViewById(R.id.btnSaveUser);
        Button btnChooseAvatar = dialogView.findViewById(R.id.btnChooseAvatar);
        ImageView imgAvatar = dialogView.findViewById(R.id.imgAvatarPreview);

        edtPassword.setVisibility(View.GONE);
        btnChooseAvatar.setVisibility(View.GONE);
        imgAvatar.setVisibility(View.GONE);

        Role[] roles = Role.values();
        String[] roleNames = new String[roles.length];
        for (int i = 0; i < roles.length; i++) {
            roleNames[i] = roles[i].name();
        }
        spinnerRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roleNames));
        spinnerStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Hoạt động", "Đã khóa"}));

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String roleStr = spinnerRole.getSelectedItem().toString();
            boolean active = spinnerStatus.getSelectedItemPosition() == 0;

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            String prefix;
            switch (roleStr) {
                case "ADMIN":
                    prefix = "AD";
                    break;
                case "USER":
                    prefix = "US";
                    break;
                default:
                    prefix = "ST";
                    break;
            }

            db.collection("User").whereEqualTo("role", roleStr).get()
                    .addOnSuccessListener(query -> {
                        int count = query.size();
                        String newUserId = prefix + String.format("%02d", count + 1);

                        String dummyPassword = "123456";
                        mAuth.createUserWithEmailAndPassword(email, dummyPassword)
                                .addOnSuccessListener(authResult -> {
                                    FirebaseUser firebaseUser = authResult.getUser();
                                    if (firebaseUser != null) {
                                        saveUserToFirestore(firebaseUser.getUid(), newUserId, email, name, Role.fromString(roleStr), active);
                                        dialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạo tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi đếm người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
    private void showEditUserDialog(User user) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_user_account_layout, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create();

        EditText edtName = dialogView.findViewById(R.id.edtUserName);
        EditText edtEmail = dialogView.findViewById(R.id.edtEmail);
        EditText edtPassword = dialogView.findViewById(R.id.edtPassword);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
        Button btnSave = dialogView.findViewById(R.id.btnSaveUser);
        Button btnChooseAvatar = dialogView.findViewById(R.id.btnChooseAvatar);
        ImageView imgAvatar = dialogView.findViewById(R.id.imgAvatarPreview);

        // Ẩn những phần không cần khi sửa
        edtPassword.setVisibility(View.GONE);
        btnChooseAvatar.setVisibility(View.GONE);
        imgAvatar.setVisibility(View.GONE);

        // Gán dữ liệu cũ
        edtName.setText(user.getName());
        edtEmail.setText(user.getEmail());
        edtEmail.setEnabled(false); // không sửa email

        // Gán dữ liệu role
        Role[] roles = Role.values();
        String[] roleNames = new String[roles.length];
        int selectedRoleIndex = 0;
        for (int i = 0; i < roles.length; i++) {
            roleNames[i] = roles[i].name();
            if (roles[i] == user.getRole()) selectedRoleIndex = i;
        }

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roleNames);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
        spinnerRole.setSelection(selectedRoleIndex);

        // Trạng thái
        String[] statusOptions = new String[]{"Hoạt động", "Đã khóa"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setSelection(user.isActive() ? 0 : 1);

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String roleStr = spinnerRole.getSelectedItem().toString();
            boolean active = spinnerStatus.getSelectedItemPosition() == 0;

            if (name.isEmpty()) {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật Firestore
            DocumentReference userRef = db.collection("User").document(user.getId());
            userRef.update(
                    "name", name,
                    "role", roleStr,
                    "active", active
            ).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadUsersFromFirestore(); // reload lại list
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadUsersFromFirestore();
    }
}