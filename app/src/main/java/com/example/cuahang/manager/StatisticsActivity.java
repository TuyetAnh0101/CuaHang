package com.example.cuahang.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StatisticsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnCalculate;
    private StatisticsAdapter adapter;
    private List<Statistics> statisticsList;
    private FirebaseFirestore db;

    private String chartType;
    private String value;
    private String timeFilter;

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
                        .addOnSuccessListener(unused -> refreshStatistics())
                        .addOnFailureListener(e -> Toast.makeText(StatisticsActivity.this, "❌ Xóa thất bại", Toast.LENGTH_SHORT).show());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        chartType = intent.getStringExtra("chartType");
        value = intent.getStringExtra("value");
        timeFilter = intent.getStringExtra("timeFilter");
        Log.d("StatisticsActivity", "chartType: " + chartType + ", value: " + value + ", timeFilter: " + timeFilter);

        btnCalculate.setOnClickListener(v -> calculateTodayStatistics());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatistics();
    }

    private void refreshStatistics() {
        if (timeFilter != null && value != null && !timeFilter.isEmpty() && !value.isEmpty()) {
            filterStatistics(timeFilter, value);
        } else {
            loadStatistics();
        }
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
                });
    }

    private void filterStatistics(String filterType, String value) {
        db.collection("statistics")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Statistics> tempList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Statistics s = doc.toObject(Statistics.class);
                        if (s == null) continue;

                        switch (filterType) {
                            case "Ngày":
                                if (s.getDate().equals(value)) tempList.add(s);
                                break;
                            case "Tuần":
                                if (getWeekKey(s.getDate()).equals(value)) tempList.add(s);
                                break;
                            case "Tháng":
                                if (s.getDate().startsWith(value)) tempList.add(s);
                                break;
                        }
                    }

                    statisticsList.clear();
                    if (filterType.equals("Ngày")) {
                        statisticsList.addAll(tempList);
                    } else {
                        Statistics combined = combineStatistics(tempList, value);
                        statisticsList.add(combined);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private Statistics combineStatistics(List<Statistics> list, String dateLabel) {
        int totalRevenue = 0;
        int totalOrders = 0;
        int packagesSold = 0;
        int newUsers = 0;
        Map<String, Integer> topCategories = new HashMap<>();

        for (Statistics s : list) {
            totalRevenue += s.getTotalRevenue();
            totalOrders += s.getTotalOrders();
            packagesSold += s.getPackagesSold();
            newUsers += s.getNewUsers();

            if (s.getTopCategories() != null) {
                for (Map.Entry<String, Integer> entry : s.getTopCategories().entrySet()) {
                    int current = topCategories.getOrDefault(entry.getKey(), 0);
                    topCategories.put(entry.getKey(), current + entry.getValue());
                }
            }
        }

        return new Statistics(dateLabel, totalRevenue, totalOrders, packagesSold, newUsers, topCategories, new HashMap<>());
    }

    private String getWeekKey(String date) {
        try {
            Calendar cal = Calendar.getInstance();
            String[] parts = date.split("-");
            cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            int week = cal.get(Calendar.WEEK_OF_YEAR);
            return parts[0] + "-W" + week;
        } catch (Exception e) {
            return "";
        }
    }
    private void calculateTodayStatistics() {
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
                            if (amount != null) totalRevenue[0] += amount;
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

                    db.collection("User")
                            .get()
                            .addOnSuccessListener(userSnapshots -> {
                                for (DocumentSnapshot userDoc : userSnapshots) {
                                    String createdDate = userDoc.getString("createdDate");
                                    if (createdDate != null && createdDate.startsWith(today)) {
                                        newUsers[0]++;
                                    }
                                }

                                Statistics statistics = new Statistics(
                                        today, totalRevenue[0], totalOrders[0],
                                        packagesSold[0], newUsers[0],
                                        topCategories, new HashMap<>()
                                );

                                db.collection("statistics").document(today)
                                        .set(statistics)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "✅ Đã cập nhật/thêm thống kê hôm nay", Toast.LENGTH_SHORT).show();
                                            refreshStatistics();
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
        if (statistics.getTopCategories() != null) {
            for (Map.Entry<String, Integer> entry : statistics.getTopCategories().entrySet()) {
                catStr.append(entry.getKey()).append(":" + entry.getValue()).append(", ");
            }
        }
        if (catStr.length() > 0) {
            catStr.setLength(catStr.length() - 2);
        }
        edtTopCategories.setText(catStr.toString());

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        btnSave.setOnClickListener(v -> {
            try {
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
                            try {
                                topCategories.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }

                Statistics updated = new Statistics(
                        date, revenue, orders, packages, users, topCategories, new HashMap<>()
                );

                db.collection("statistics").document(date)
                        .set(updated)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "✅ Đã cập nhật", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            refreshStatistics();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "❌ Lỗi cập nhật", Toast.LENGTH_SHORT).show());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "❌ Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
