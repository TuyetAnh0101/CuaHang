package com.example.cuahang;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.cuahang.fragment.AccountFragment;
import com.example.cuahang.fragment.UserFragment;
import com.example.cuahang.ui.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView mnBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Kiểm tra người dùng đã đăng nhập chưa
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // ❌ Nếu chưa đăng nhập, chuyển về LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Không cho vào MainActivity
            return;
        }

        // ✅ Người dùng đã đăng nhập, tiếp tục load layout
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo menu
        mnBottom = findViewById(R.id.bottomNav);

        // Gọi listener xử lý chọn item menu
        mnBottom.setOnItemSelectedListener(getItemBottomListener());

        // Mặc định hiển thị UserFragment khi mở app
        if (savedInstanceState == null) {
            loadFragment(new UserFragment());
            mnBottom.setSelectedItemId(R.id.userhome); // Đặt item được chọn
        }

        // Xử lý padding cho hệ thống (EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Xử lý khi người dùng chọn item trên menu
    private NavigationBarView.OnItemSelectedListener getItemBottomListener() {
        return item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.userhome) {
                selectedFragment = new UserFragment();
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
    // Load fragment vào khung giao diện
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, fragment)
                .commit();
    }
}
