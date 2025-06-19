package com.example.cuahang.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.PackageAdapter;
import com.example.cuahang.model.Package;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PackageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtEmpty;
    private ProgressBar progressBar;
    private PackageAdapter adapter;
    private List<Package> packageList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_package);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.packages), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerPackage);
        txtEmpty = findViewById(R.id.txtEmpty);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        adapter = new PackageAdapter(this, packageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        String categoryId = getIntent().getStringExtra("categoryId");
        if (categoryId != null) {
            loadPackagesByCategory(categoryId);
        } else {
            loadAllPackages(); // ✅ Nếu không có categoryId, hiển thị toàn bộ gói
        }
    }

    private void loadPackagesByCategory(String categoryId) {
        showLoading(true);

        db.collection("Package")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    packageList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Package pkg = doc.toObject(Package.class);
                        packageList.add(pkg);
                    }

                    txtEmpty.setVisibility(packageList.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.notifyDataSetChanged();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    txtEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ✅ Hàm mới để load tất cả gói nếu không truyền categoryId
    private void loadAllPackages() {
        showLoading(true);

        db.collection("Package")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    packageList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Package pkg = doc.toObject(Package.class);
                        packageList.add(pkg);
                    }

                    txtEmpty.setVisibility(packageList.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.notifyDataSetChanged();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    txtEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}
