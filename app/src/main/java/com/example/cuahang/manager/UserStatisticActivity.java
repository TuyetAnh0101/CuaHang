package com.example.cuahang.manager;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cuahang.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class UserStatisticActivity extends AppCompatActivity {

    private LineChart lineChart;
    private Button btnThongKe;
    private Spinner spinnerFilterType;
    private EditText edtStartDate, edtEndDate;
    private final Calendar calendarStart = Calendar.getInstance();
    private final Calendar calendarEnd = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_statistic);

        lineChart = findViewById(R.id.lineChart);
        btnThongKe = findViewById(R.id.btnThongKe);
        spinnerFilterType = findViewById(R.id.spinnerFilterType);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("Ngày", "Tuần", "Tháng"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterType.setAdapter(adapter);

        setupDatePickers();
        setupLineChartDefaults();

        btnThongKe.setOnClickListener(v -> loadUserStatistics());
    }
    private void setupDatePickers() {
        edtStartDate.setOnClickListener(v -> showDatePicker(calendarStart, edtStartDate));
        edtEndDate.setOnClickListener(v -> showDatePicker(calendarEnd, edtEndDate));
    }

    private void showDatePicker(Calendar calendar, EditText target) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            target.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadUserStatistics() {
        if (edtStartDate.getText().toString().isEmpty() || edtEndDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày bắt đầu và ngày kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }

        String filterType = spinnerFilterType.getSelectedItem().toString();
        String startDate = edtStartDate.getText().toString();
        String endDate = edtEndDate.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Orders").get().addOnSuccessListener(orderSnapshots -> {
            Map<String, Long> userRevenue = new HashMap<>();

            for (DocumentSnapshot doc : orderSnapshots) {
                String userId = doc.getString("idKhach");
                String ngayDat = doc.getString("ngayDat");
                Long tongTien = doc.getLong("tongTien");

                if (userId != null && tongTien != null && ngayDat != null) {
                    if (isWithinRange(ngayDat, startDate, endDate, filterType)) {
                        userRevenue.put(userId, userRevenue.getOrDefault(userId, 0L) + tongTien);
                    }
                }
            }
            loadUserInfo(userRevenue);
        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show());
    }

    private boolean isWithinRange(String ngayDat, String start, String end, String type) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(ngayDat);
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);

            Calendar cal = Calendar.getInstance();
            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();
            cal.setTime(date);
            startCal.setTime(startDate);
            endCal.setTime(endDate);

            if (type.equals("Ngày")) {
                return !date.before(startDate) && !date.after(endDate);
            } else if (type.equals("Tuần")) {
                return cal.get(Calendar.WEEK_OF_YEAR) >= startCal.get(Calendar.WEEK_OF_YEAR) &&
                        cal.get(Calendar.WEEK_OF_YEAR) <= endCal.get(Calendar.WEEK_OF_YEAR) &&
                        cal.get(Calendar.YEAR) == startCal.get(Calendar.YEAR);
            } else if (type.equals("Tháng")) {
                return (cal.get(Calendar.YEAR) == startCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.MONTH) >= startCal.get(Calendar.MONTH) &&
                        cal.get(Calendar.MONTH) <= endCal.get(Calendar.MONTH));
            }
        } catch (Exception ignored) {
            Log.e("DATE_PARSE_ERROR", ignored.getMessage());
        }
        return false;
    }

    private void loadUserInfo(Map<String, Long> userRevenue) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User").get().addOnSuccessListener(userSnapshots -> {
            List<String> labels = new ArrayList<>();
            List<Entry> lineEntries = new ArrayList<>();
            int index = 0;

            for (DocumentSnapshot userDoc : userSnapshots) {
                String userId = userDoc.getString("userId");
                String userName = userDoc.getString("name");
                String role = userDoc.getString("role");

                if ("USER".equalsIgnoreCase(role) && userId != null && userRevenue.containsKey(userId)) {
                    labels.add(userName != null ? userName : "User " + (index + 1));
                    lineEntries.add(new Entry(index, userRevenue.get(userId).floatValue()));
                    index++;
                }
            }

            if (labels.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu để hiển thị", Toast.LENGTH_SHORT).show();
                lineChart.clear();
                lineChart.invalidate();
            } else {
                setupLineChart(lineEntries, labels);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải danh sách người dùng", Toast.LENGTH_SHORT).show());
    }

    private void setupLineChart(List<Entry> lineEntries, List<String> labels) {
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Tổng doanh thu theo người dùng");
        lineDataSet.setColor(Color.parseColor("#FF5722"));
        lineDataSet.setCircleColor(Color.parseColor("#FF5722"));
        lineDataSet.setValueTextSize(12f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-30);
        xAxis.setTextSize(12f);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setTextSize(12f);

        Legend legend = lineChart.getLegend();
        legend.setTextSize(14f);
        legend.setFormSize(14f);

        Description description = new Description();
        description.setText("Biểu đồ tăng trưởng doanh thu theo người dùng");
        description.setTextSize(14f);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }

    private void setupLineChartDefaults() {
        lineChart.setDrawGridBackground(false);
        lineChart.getAxisLeft().setGridColor(Color.LTGRAY);
        lineChart.getXAxis().setGridColor(Color.LTGRAY);
        lineChart.getXAxis().setDrawAxisLine(true);
        lineChart.getAxisLeft().setDrawAxisLine(true);
    }
}
