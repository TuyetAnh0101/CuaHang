package com.example.cuahang.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.StatisticsAdapter;
import com.example.cuahang.model.Statistics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class StatisticsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnCalculate;
    private StatisticsAdapter adapter;
    private List<Statistics> statisticsList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.statistics), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerStatistics);
        btnCalculate = findViewById(R.id.btnCalculate);

        statisticsList = new ArrayList<>();

        adapter = new StatisticsAdapter(statisticsList, new StatisticsAdapter.StatisticsClickListener() {
            @Override
            public void onEdit(Statistics statistics) {
                showEditDialog(statistics);
            }

            @Override
            public void onDelete(Statistics statistics) {
                db.collection("statistics").document(statistics.getDate())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(StatisticsActivity.this, "✅ Đã xóa", Toast.LENGTH_SHORT).show();
                            loadStatistics();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(StatisticsActivity.this, "❌ Xóa thất bại", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadStatistics();

        btnCalculate.setOnClickListener(v -> calculateTodayStatistics());
    }

    private void loadStatistics() {
        db.collection("statistics")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    statisticsList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Statistics s = doc.toObject(Statistics.class);
                        if (s != null) {
                            statisticsList.add(s);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "❌ Lỗi tải thống kê", Toast.LENGTH_SHORT).show());
    }

    private void calculateTodayStatistics() {
        // Format ngày hiện tại thành dạng "yyyy-MM-dd"
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        final int[] totalRevenue = {0};
        final int[] totalOrders = {0};
        final int[] packagesSold = {0};
        final int[] newUsers = {0};
        final Map<String, Integer> topCategories = new HashMap<>();

        db.collection("Orders")
                .get()
                .addOnSuccessListener(orderSnapshots -> {
                    for (DocumentSnapshot doc : orderSnapshots) {
                        String ngayDat = doc.getString("ngayDat");
                        if (ngayDat != null && ngayDat.startsWith(today)) {
                            totalOrders[0]++;

                            Long amount = doc.getLong("tongTien");
                            if (amount != null) {
                                totalRevenue[0] += amount;
                            }

                            List<Map<String, Object>> packageList = (List<Map<String, Object>>) doc.get("packages");
                            if (packageList != null) {
                                packagesSold[0] += packageList.size();

                                for (Map<String, Object> pkg : packageList) {
                                    String tenGoi = (String) pkg.get("tenGoi");
                                    if (tenGoi != null && !tenGoi.isEmpty()) {
                                        int current = topCategories.getOrDefault(tenGoi, 0);
                                        topCategories.put(tenGoi, current + 1);
                                    }
                                }
                            }
                        }
                    }

                    // Tiếp tục đếm số người dùng tạo hôm nay
                    db.collection("User")
                            .get()
                            .addOnSuccessListener(userSnapshots -> {
                                for (DocumentSnapshot userDoc : userSnapshots) {
                                    String createdDate = userDoc.getString("createdDate");
                                    if (createdDate != null && createdDate.startsWith(today)) {
                                        newUsers[0]++;
                                    }
                                }

                                // Tạo thống kê hôm nay
                                Statistics statistics = new Statistics(
                                        today,
                                        totalRevenue[0],
                                        totalOrders[0],
                                        packagesSold[0],
                                        newUsers[0],
                                        topCategories
                                );

                                db.collection("statistics")
                                        .document(today)
                                        .set(statistics)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "✅ Đã tạo thống kê hôm nay", Toast.LENGTH_SHORT).show();
                                            loadStatistics();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "❌ Lỗi khi lưu thống kê", Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "❌ Lỗi tải User", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "❌ Lỗi tải Orders", Toast.LENGTH_SHORT).show());
    }

    private void showEditDialog(Statistics statistics) {
        View dialogView = getLayoutInflater().inflate(R.layout.add_statistic_layout, null);

        EditText edtDate = dialogView.findViewById(R.id.edtDate);
        EditText edtRevenue = dialogView.findViewById(R.id.edtRevenue);
        EditText edtOrders = dialogView.findViewById(R.id.edtOrders);
        EditText edtPackages = dialogView.findViewById(R.id.edtPackages);
        EditText edtUsers = dialogView.findViewById(R.id.edtUsers);
        EditText edtTopCategories = dialogView.findViewById(R.id.edtTopCategories);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        edtDate.setText(statistics.getDate());
        edtRevenue.setText(String.valueOf(statistics.getTotalRevenue()));
        edtOrders.setText(String.valueOf(statistics.getTotalOrders()));
        edtPackages.setText(String.valueOf(statistics.getPackagesSold()));
        edtUsers.setText(String.valueOf(statistics.getNewUsers()));

        StringBuilder catStr = new StringBuilder();
        for (Map.Entry<String, Integer> entry : statistics.getTopCategories().entrySet()) {
            catStr.append(entry.getKey()).append(":").append(entry.getValue()).append(", ");
        }
        if (catStr.length() > 0) {
            catStr.setLength(catStr.length() - 2);
        }
        edtTopCategories.setText(catStr.toString());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnSave.setOnClickListener(v -> {
            String date = edtDate.getText().toString().trim();
            int revenue = Integer.parseInt(edtRevenue.getText().toString().trim());
            int orders = Integer.parseInt(edtOrders.getText().toString().trim());
            int packages = Integer.parseInt(edtPackages.getText().toString().trim());
            int users = Integer.parseInt(edtUsers.getText().toString().trim());
            String topCatsInput = edtTopCategories.getText().toString().trim();

            Map<String, Integer> topCategories = new HashMap<>();
            if (!topCatsInput.isEmpty()) {
                String[] pairs = topCatsInput.split(",");
                for (String pair : pairs) {
                    String[] parts = pair.trim().split(":");
                    if (parts.length == 2) {
                        topCategories.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    }
                }
            }

            Statistics updated = new Statistics(date, revenue, orders, packages, users, topCategories);
            db.collection("statistics").document(date)
                    .set(updated)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "✅ Đã cập nhật", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadStatistics();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "❌ Lỗi cập nhật", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
}
