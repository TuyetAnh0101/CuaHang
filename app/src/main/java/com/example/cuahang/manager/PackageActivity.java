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
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PackageAdapter(this, packageList);
        recyclerView.setAdapter(adapter);

        // Lấy categoryId từ Intent
        String categoryId = getIntent().getStringExtra("categoryId");
        if (categoryId != null) {
            loadPackagesByCategory(categoryId);
        } else {
            Toast.makeText(this, "Không có categoryId!", Toast.LENGTH_SHORT).show();
            finish();
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

                    if (packageList.isEmpty()) {
                        txtEmpty.setVisibility(View.VISIBLE);
                    } else {
                        txtEmpty.setVisibility(View.GONE);
                    }

                    adapter.notifyDataSetChanged();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    txtEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(PackageActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}
