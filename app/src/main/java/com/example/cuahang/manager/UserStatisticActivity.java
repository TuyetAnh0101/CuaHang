package com.example.cuahang.manager;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cuahang.R;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserStatisticActivity extends AppCompatActivity {

    private CombinedChart combinedChart;
    private Spinner spinnerFilterType;
    private Button btnThongKe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_statistic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.userstatistic), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        combinedChart = findViewById(R.id.combinedChart);
        spinnerFilterType = findViewById(R.id.spinnerFilterType);
        btnThongKe = findViewById(R.id.btnThongKe);

        btnThongKe.setOnClickListener(v -> loadUserStatistics());
    }

    private void loadUserStatistics() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Orders").get().addOnSuccessListener(orderSnapshots -> {
            Map<String, Integer> userOrderCount = new HashMap<>();
            Map<String, Double> userRevenue = new HashMap<>();

            for (DocumentSnapshot doc : orderSnapshots) {
                String userId = doc.getString("idKhach");
                Double tongTien = doc.getDouble("tongTien");

                if (userId != null && tongTien != null) {
                    userOrderCount.put(userId, userOrderCount.getOrDefault(userId, 0) + 1);
                    userRevenue.put(userId, userRevenue.getOrDefault(userId, 0.0) + tongTien);
                }
            }

            db.collection("User").get().addOnSuccessListener(userSnapshots -> {
                List<String> labels = new ArrayList<>();
                List<BarEntry> barEntries = new ArrayList<>();
                List<Entry> lineEntries = new ArrayList<>();

                int index = 0;
                for (DocumentSnapshot userDoc : userSnapshots) {
                    String userId = userDoc.getId();
                    String userName = userDoc.getString("userName");
                    String role = userDoc.getString("role");

                    if ("customer".equalsIgnoreCase(role) && userOrderCount.containsKey(userId)) {
                        labels.add(userName != null ? userName : "User " + (index + 1));
                        barEntries.add(new BarEntry(index, userOrderCount.get(userId)));
                        lineEntries.add(new Entry(index, userRevenue.get(userId).floatValue()));
                        index++;
                    }
                }

                if (labels.isEmpty()) {
                    Toast.makeText(this, "Không có dữ liệu để hiển thị", Toast.LENGTH_SHORT).show();
                    combinedChart.clear();
                    combinedChart.invalidate();
                } else {
                    setupCombinedChart(barEntries, lineEntries, labels);
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Lỗi tải danh sách người dùng", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show());
    }

    private void setupCombinedChart(List<BarEntry> barEntries, List<Entry> lineEntries, List<String> labels) {
        BarDataSet barDataSet = new BarDataSet(barEntries, "Số đơn hàng");
        barDataSet.setColor(Color.parseColor("#2B572E"));
        BarData barData = new BarData(barDataSet);

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Doanh thu");
        lineDataSet.setColor(Color.parseColor("#2B572E"));
        lineDataSet.setCircleColor(Color.parseColor("#2B572E"));
        LineData lineData = new LineData(lineDataSet);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(lineData);

        combinedChart.setData(combinedData);
        combinedChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        combinedChart.getXAxis().setGranularity(1f);
        combinedChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        combinedChart.getXAxis().setDrawGridLines(false);
        combinedChart.getAxisRight().setEnabled(false);

        Description description = new Description();
        description.setText("Thống kê người dùng theo số đơn và doanh thu");
        combinedChart.setDescription(description);
        combinedChart.invalidate();
    }
}
