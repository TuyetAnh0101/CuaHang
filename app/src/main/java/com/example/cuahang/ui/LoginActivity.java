package com.example.cuahang.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cuahang.MainActivity;
import com.example.cuahang.R;
import com.example.cuahang.model.Role;
import com.example.cuahang.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            fetchUserRole(currentUser.getUid());
            return;
        }

        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (!isValid(email, password)) return;

            loginUser(email, password);
        });

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private boolean isValid(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchUserRole(firebaseUser.getUid());
                        } else {
                            Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserRole(String uid) {
        db.collection("User").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);

                        // ✅ GHI LOG UID và role
                        String roleStr = documentSnapshot.getString("role");
                        android.util.Log.d("LOGIN_DEBUG", "UID: " + uid);
                        android.util.Log.d("LOGIN_DEBUG", "Role (từ Firestore): " + roleStr);

                        if (user != null && user.getRole() != null) {
                            Role role = user.getRole();
                            android.util.Log.d("LOGIN_DEBUG", "Role (object): " + role.name());

                            // ✅ Chuyển sang MainActivity, kèm role
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("role", role.name());
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Tài khoản chưa có vai trò", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Tài khoản không tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lấy dữ liệu người dùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đặt lại mật khẩu");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Nhập email của bạn");
        builder.setView(input);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Lỗi gửi email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
