package com.example.cuahang.manager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cuahang.R;
import com.example.cuahang.model.Statistics;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class StatisticsOverviewActivity extends AppCompatActivity {

    private PieChart pieChartPackages;
    private CombinedChart combinedChartRevenue;
    private Spinner spinnerTimeFilter;

    private FirebaseFirestore db;
    private final List<String> revenueLabels = new ArrayList<>();
    private final List<Statistics> allStatistics = new ArrayList<>();
    private boolean isFirstLoad = true;
    private List<Statistics> lastFilteredStatistics = new ArrayList<>();

    private EditText edtStartDate, edtEndDate;
    private Button btnThongKe;
    private Calendar calendarStart, calendarEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_overview);


        pieChartPackages = findViewById(R.id.pieChartPackages);
        combinedChartRevenue = findViewById(R.id.combinedChartRevenue);
        spinnerTimeFilter = findViewById(R.id.spinnerTimeFilter);
        db = FirebaseFirestore.getInstance();
        combinedChartRevenue.setOnTouchListener((v, event) -> {
            Log.d("ChartTouchTest", "CombinedChart touched: " + event.getAction());
            return false;
        });
        pieChartPackages.setOnTouchListener((v, event) -> {
            Log.d("ChartTouchTest", "PieChart touched: " + event.getAction());
            return false;
        });
        setupTimeFilter();
        setupChartClicks();
        combinedChartRevenue.setTouchEnabled(true);
        combinedChartRevenue.setHighlightPerTapEnabled(true);
        combinedChartRevenue.setNestedScrollingEnabled(false);

        pieChartPackages.setTouchEnabled(true);
        pieChartPackages.setHighlightPerTapEnabled(true);
        pieChartPackages.setNestedScrollingEnabled(false);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);
        btnThongKe = findViewById(R.id.btnThongKe);
        calendarStart = Calendar.getInstance();
        calendarEnd = Calendar.getInstance();

        setupDatePickers();
        btnThongKe.setOnClickListener(v -> filterByDateRange());

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstLoad) {
            loadStatisticsData();
            isFirstLoad = false;
        } else if (!lastFilteredStatistics.isEmpty()) {
            showCombinedChartRevenue(lastFilteredStatistics);
            showPieChart(lastFilteredStatistics);
        }
    }

    private void setupTimeFilter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("Ngày", "Tuần", "Tháng"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeFilter.setAdapter(adapter);

        spinnerTimeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!allStatistics.isEmpty()) filterAndShowCharts();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadStatisticsData() {
        db.collection("statistics").get().addOnSuccessListener(querySnapshot -> {
            allStatistics.clear();
            for (DocumentSnapshot doc : querySnapshot) {
                Statistics s = doc.toObject(Statistics.class);
                if (s != null) allStatistics.add(s);
            }
            filterAndShowCharts();
        });
    }

    private void filterAndShowCharts() {
        String timeFilter = spinnerTimeFilter.getSelectedItem().toString();
        lastFilteredStatistics = filterStatisticsByTime(allStatistics, timeFilter);
        showCombinedChartRevenue(lastFilteredStatistics);
        showPieChart(lastFilteredStatistics);
    }

    private boolean isBeforeOrEqualToday(String dateStr) {
        try {
            Calendar calInput = Calendar.getInstance();
            String[] parts = dateStr.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int day = Integer.parseInt(parts[2]);
            calInput.set(year, month, day, 0, 0, 0);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);

            return !calInput.after(today);
        } catch (Exception e) {
            return false;
        }
    }

    private List<Statistics> filterStatisticsByTime(List<Statistics> list, String timeFilter) {
        List<Statistics> filteredByDate = new ArrayList<>();
        for (Statistics s : list) {
            if (isBeforeOrEqualToday(s.getDate())) {
                filteredByDate.add(s);
            }
        }

        List<Statistics> filtered = new ArrayList<>();
        switch (timeFilter) {
            case "Ngày":
                filtered = filteredByDate;
                break;
            case "Tuần":
                Map<String, Statistics> weekMap = new HashMap<>();
                Calendar cal = Calendar.getInstance();
                for (Statistics s : filteredByDate) {
                    try {
                        String[] parts = s.getDate().split("-");
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]) - 1;
                        int day = Integer.parseInt(parts[2]);
                        cal.set(year, month, day);
                        int week = cal.get(Calendar.WEEK_OF_YEAR);
                        String key = year + "-W" + week;
                        weekMap.putIfAbsent(key, new Statistics(key));
                        mergeStat(weekMap.get(key), s);
                    } catch (Exception ignored) {}
                }
                filtered.addAll(weekMap.values());
                break;
            case "Tháng":
                Map<String, Statistics> monthMap = new HashMap<>();
                for (Statistics s : filteredByDate) {
                    try {
                        String key = s.getDate().substring(0, 7);
                        monthMap.putIfAbsent(key, new Statistics(key));
                        mergeStat(monthMap.get(key), s);
                    } catch (Exception ignored) {}
                }
                filtered.addAll(monthMap.values());
                break;
        }
        filtered.sort(Comparator.comparing(Statistics::getDate));
        return filtered;
    }

    private void mergeStat(Statistics target, Statistics source) {
        target.setTotalRevenue(target.getTotalRevenue() + source.getTotalRevenue());
        target.setTotalOrders(target.getTotalOrders() + source.getTotalOrders());
        target.setPackagesSold(target.getPackagesSold() + source.getPackagesSold());
        target.setNewUsers(target.getNewUsers() + source.getNewUsers());
        mergeMaps(target.getTopCategories(), source.getTopCategories());
    }

    private void mergeMaps(Map<String, Integer> base, Map<String, Integer> addition) {
        if (base == null || addition == null) return;
        for (Map.Entry<String, Integer> entry : addition.entrySet()) {
            base.put(entry.getKey(), base.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
    }
    private void showCombinedChartRevenue(List<Statistics> list) {
        List<BarEntry> barEntries = new ArrayList<>();
        List<Entry> lineEntries = new ArrayList<>();
        revenueLabels.clear();

        for (int i = 0; i < list.size(); i++) {
            Statistics s = list.get(i);
            barEntries.add(new BarEntry(i, s.getTotalOrders()));
            lineEntries.add(new Entry(i, s.getTotalRevenue()));
            revenueLabels.add(s.getDate());
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Số đơn hàng");
        barDataSet.setColor(Color.rgb(60, 120, 240));
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.3f);

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Doanh thu");
        lineDataSet.setColor(Color.RED);
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        LineData lineData = new LineData(lineDataSet);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(lineData);

        combinedChartRevenue.setData(combinedData);

        XAxis xAxis = combinedChartRevenue.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(revenueLabels.size());
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return (i >= 0 && i < revenueLabels.size()) ? revenueLabels.get(i) : "";
            }
        });

        combinedChartRevenue.getAxisLeft().setAxisMinimum(0);
        combinedChartRevenue.getAxisRight().setAxisMinimum(0);

        combinedChartRevenue.getDescription().setText("Thống kê tổng hợp");
        combinedChartRevenue.invalidate();
    }

    private void showPieChart(List<Statistics> list) {
        Map<String, Integer> packageMap = new HashMap<>();
        for (Statistics stat : list) {
            if (stat.getTopCategories() == null) continue;
            for (Map.Entry<String, Integer> entry : stat.getTopCategories().entrySet()) {
                packageMap.put(entry.getKey(), packageMap.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : packageMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.rgb(255, 87, 34), Color.rgb(63, 81, 181), Color.rgb(76, 175, 80), Color.rgb(255, 193, 7));
        dataSet.setValueTextSize(12f);
        PieData data = new PieData(dataSet);

        pieChartPackages.setData(data);
        pieChartPackages.setUsePercentValues(true);
        pieChartPackages.setCenterText("Gói được mua");
        pieChartPackages.setCenterTextSize(14f);
        pieChartPackages.setEntryLabelColor(Color.BLACK);
        pieChartPackages.setHoleColor(Color.TRANSPARENT);
        pieChartPackages.getDescription().setEnabled(false);
        pieChartPackages.invalidate();
    }

    private void setupChartClicks() {
        pieChartPackages.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String label = ((PieEntry) e).getLabel();
                openDetailStatistics("package", label);
            }
            @Override public void onNothingSelected() {}
        });

        combinedChartRevenue.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int x = (int) e.getX();
                if (x >= 0 && x < revenueLabels.size()) {
                    String date = revenueLabels.get(x);
                    openDetailStatistics("revenue", date);
                }
            }
            @Override public void onNothingSelected() {}
        });
    }

    private void openDetailStatistics(String type, String value) {
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.putExtra("chartType", type);
        intent.putExtra("value", value);
        startActivity(intent);
        intent.putExtra("timeFilter", spinnerTimeFilter.getSelectedItem().toString());
        startActivity(intent);
    }
    private void setupDatePickers() {
        edtStartDate.setOnClickListener(v -> showDatePicker(true));
        edtEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar target = isStartDate ? calendarStart : calendarEnd;
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            target.set(year, month, dayOfMonth);
            String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            if (isStartDate) {
                edtStartDate.setText(dateStr);
            } else {
                edtEndDate.setText(dateStr);
            }
        }, target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void filterByDateRange() {
        String startStr = edtStartDate.getText().toString().trim();
        String endStr = edtEndDate.getText().toString().trim();

        if (startStr.isEmpty() || endStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ Từ ngày và Đến ngày", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Statistics> filtered = new ArrayList<>();
        for (Statistics s : allStatistics) {
            if (isDateInRange(s.getDate(), startStr, endStr)) {
                filtered.add(s);
            }
        }

        lastFilteredStatistics = filtered;
        showCombinedChartRevenue(filtered);
        showPieChart(filtered);
    }

    private boolean isDateInRange(String dateStr, String startStr, String endStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            Date start = sdf.parse(startStr);
            Date end = sdf.parse(endStr);
            return date != null && start != null && end != null && !date.before(start) && !date.after(end);
        } catch (Exception e) {
            return false;
        }
    }

}
