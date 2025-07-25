package com.example.cuahang;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.cuahang.UserFragment.NguoiDungFragment;
import com.example.cuahang.UserFragment.PostFragment;
import com.example.cuahang.fragment.AccountFragment;
import com.example.cuahang.fragment.UserFragment;
import com.example.cuahang.ui.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mnBottom;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Nhận role từ LoginActivity
        role = getIntent().getStringExtra("role");
        if (role == null) {
            Toast.makeText(this, "Không có vai trò người dùng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        mnBottom = findViewById(R.id.bottomNav);
        mnBottom.setOnItemSelectedListener(getItemBottomListener());

        // Ẩn nút "Đăng tin" nếu là ADMIN
        if ("ADMIN".equalsIgnoreCase(role)) {
            Menu menu = mnBottom.getMenu();
            menu.findItem(R.id.post).setVisible(false);
        }

        // Load fragment mặc định
        if (savedInstanceState == null) {
            Fragment defaultFragment = "ADMIN".equalsIgnoreCase(role)
                    ? new UserFragment()
                    : new NguoiDungFragment();

            loadFragment(defaultFragment);
            mnBottom.setSelectedItemId(R.id.userhome);
        }

        // Căn lề full màn hình
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private NavigationBarView.OnItemSelectedListener getItemBottomListener() {
        return item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.userhome) {
                selectedFragment = "ADMIN".equalsIgnoreCase(role)
                        ? new UserFragment()
                        : new NguoiDungFragment();

            } else if (id == R.id.post) {
                selectedFragment = new PostFragment();

            } else if (id == R.id.account) {
                selectedFragment = new AccountFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }

            return false;
        };
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, fragment)
                .commit();
    }
}
