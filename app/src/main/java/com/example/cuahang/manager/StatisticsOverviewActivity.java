package com.example.cuahang.manager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cuahang.R;
import com.example.cuahang.model.Statistics;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class StatisticsOverviewActivity extends AppCompatActivity {

    private PieChart pieChartPackages;
    private BarChart barChartPostTypes;
    private BarChart barChartRevenue; // đổi từ LineChart sang BarChart
    private FirebaseFirestore db;

    private Spinner spinnerTimeFilter, spinnerChartType;

    private List<String> revenueLabels = new ArrayList<>();
    private List<String> barLabels = new ArrayList<>();

    private List<Statistics> allStatistics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics_overview);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.overview), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pieChartPackages = findViewById(R.id.pieChartPackages);
        barChartPostTypes = findViewById(R.id.barChartPostTypes);
        barChartRevenue = findViewById(R.id.barChartRevenue);

        spinnerTimeFilter = findViewById(R.id.spinnerTimeFilter);
        spinnerChartType = findViewById(R.id.spinnerChartType);

        db = FirebaseFirestore.getInstance();

        setupSpinners();
        setupChartClicks();

        loadStatisticsData();
    }

    private void setupSpinners() {
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                Arrays.asList("Ngày", "Tuần", "Tháng"));
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeFilter.setAdapter(timeAdapter);

        ArrayAdapter<String> chartTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                Arrays.asList("Gói", "Loại tin", "Doanh thu"));
        chartTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChartType.setAdapter(chartTypeAdapter);

        spinnerTimeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!allStatistics.isEmpty()) filterAndShowCharts();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerChartType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateChartVisibility();
                if (!allStatistics.isEmpty()) filterAndShowCharts();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateChartVisibility() {
        String selectedChart = spinnerChartType.getSelectedItem().toString();

        pieChartPackages.setVisibility(View.GONE);
        barChartPostTypes.setVisibility(View.GONE);
        barChartRevenue.setVisibility(View.GONE);

        switch (selectedChart) {
            case "Gói": pieChartPackages.setVisibility(View.VISIBLE); break;
            case "Loại tin": barChartPostTypes.setVisibility(View.VISIBLE); break;
            case "Doanh thu": barChartRevenue.setVisibility(View.VISIBLE); break;
        }
    }

    private void loadStatisticsData() {
        db.collection("statistics")
                .get()
                .addOnSuccessListener(querySnapshot -> {
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
        List<Statistics> filteredList = filterStatisticsByTime(allStatistics, timeFilter);
        String selectedChart = spinnerChartType.getSelectedItem().toString();
        switch (selectedChart) {
            case "Gói": showPieChart(filteredList); break;
            case "Loại tin": showBarChart(filteredList); break;
            case "Doanh thu": showBarChartRevenue(filteredList); break;
        }
    }

    private List<Statistics> filterStatisticsByTime(List<Statistics> list, String timeFilter) {
        List<Statistics> filtered = new ArrayList<>();
        switch (timeFilter) {
            case "Ngày":
                filtered = list;
                break;
            case "Tuần": {
                Map<String, Statistics> weekMap = new HashMap<>();
                Calendar cal = Calendar.getInstance();
                for (Statistics s : list) {
                    try {
                        String dateStr = s.getDate();
                        String[] parts = dateStr.split("-");
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]) - 1;
                        int day = Integer.parseInt(parts[2]);
                        cal.set(year, month, day);
                        int week = cal.get(Calendar.WEEK_OF_YEAR);
                        String key = year + "-W" + week;
                        if (!weekMap.containsKey(key)) {
                            weekMap.put(key, new Statistics(key, 0, 0, 0, 0, new HashMap<>(), new HashMap<>()));
                        }
                        Statistics current = weekMap.get(key);
                        current.setTotalRevenue(current.getTotalRevenue() + s.getTotalRevenue());
                        current.setTotalOrders(current.getTotalOrders() + s.getTotalOrders());
                        current.setPackagesSold(current.getPackagesSold() + s.getPackagesSold());
                        current.setNewUsers(current.getNewUsers() + s.getNewUsers());
                        mergeMaps(current.getTopCategories(), s.getTopCategories());
                        mergeMaps(current.getTopPostTypes(), s.getTopPostTypes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                filtered.addAll(weekMap.values());
                break;
            }
            case "Tháng": {
                Map<String, Statistics> monthMap = new HashMap<>();
                for (Statistics s : list) {
                    try {
                        String dateStr = s.getDate();
                        String key = dateStr.substring(0, 7); // yyyy-MM
                        if (!monthMap.containsKey(key)) {
                            monthMap.put(key, new Statistics(key, 0, 0, 0, 0, new HashMap<>(), new HashMap<>()));
                        }
                        Statistics current = monthMap.get(key);
                        current.setTotalRevenue(current.getTotalRevenue() + s.getTotalRevenue());
                        current.setTotalOrders(current.getTotalOrders() + s.getTotalOrders());
                        current.setPackagesSold(current.getPackagesSold() + s.getPackagesSold());
                        current.setNewUsers(current.getNewUsers() + s.getNewUsers());
                        mergeMaps(current.getTopCategories(), s.getTopCategories());
                        mergeMaps(current.getTopPostTypes(), s.getTopPostTypes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                filtered.addAll(monthMap.values());
                break;
            }
        }
        filtered.sort(Comparator.comparing(Statistics::getDate));
        return filtered;
    }

    private void mergeMaps(Map<String, Integer> baseMap, Map<String, Integer> addMap) {
        if (baseMap == null || addMap == null) return;
        for (Map.Entry<String, Integer> entry : addMap.entrySet()) {
            baseMap.put(entry.getKey(), baseMap.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
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
        dataSet.setColors(Color.rgb(244, 67, 54), Color.rgb(33, 150, 243),
                Color.rgb(76, 175, 80), Color.rgb(255, 193, 7), Color.rgb(156, 39, 176));
        PieData data = new PieData(dataSet);

        pieChartPackages.setData(data);
        pieChartPackages.setUsePercentValues(true);
        pieChartPackages.setCenterText("Gói mua nhiều");
        pieChartPackages.setCenterTextSize(14f);
        pieChartPackages.setEntryLabelColor(Color.BLACK);
        pieChartPackages.setHoleColor(Color.TRANSPARENT);
        pieChartPackages.getDescription().setEnabled(false);
        pieChartPackages.invalidate();
    }

    private void showBarChart(List<Statistics> list) {
        Map<String, Integer> typeMap = new HashMap<>();
        for (Statistics stat : list) {
            if (stat.getTopPostTypes() == null) continue;
            for (Map.Entry<String, Integer> entry : stat.getTopPostTypes().entrySet()) {
                typeMap.put(entry.getKey(), typeMap.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
        List<BarEntry> entries = new ArrayList<>();
        barLabels.clear();
        int index = 0;
        for (Map.Entry<String, Integer> entry : typeMap.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            barLabels.add(entry.getKey());
            index++;
        }
        BarDataSet dataSet = new BarDataSet(entries, "Loại tin");
        dataSet.setColors(Color.rgb(3, 169, 244), Color.rgb(255, 87, 34), Color.rgb(139, 195, 74));
        BarData data = new BarData(dataSet);
        barChartPostTypes.setData(data);
        barChartPostTypes.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                if (i >= 0 && i < barLabels.size()) return barLabels.get(i);
                else return "";
            }
        });
        Description desc = new Description();
        desc.setText("");
        barChartPostTypes.setDescription(desc);
        barChartPostTypes.invalidate();
    }

    private void showBarChartRevenue(List<Statistics> list) {
        List<BarEntry> entries = new ArrayList<>();
        revenueLabels.clear();

        Collections.sort(list, Comparator.comparing(Statistics::getDate));

        for (int i = 0; i < list.size(); i++) {
            Statistics s = list.get(i);
            entries.add(new BarEntry(i, s.getTotalRevenue()));
            revenueLabels.add(s.getDate());
        }
        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu");
        dataSet.setColor(Color.BLUE);
        BarData data = new BarData(dataSet);
        barChartRevenue.setData(data);
        barChartRevenue.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                if (i >= 0 && i < revenueLabels.size()) return revenueLabels.get(i);
                else return "";
            }
        });
        Description desc = new Description();
        desc.setText("");
        barChartRevenue.setDescription(desc);
        barChartRevenue.invalidate();
    }

    private void setupChartClicks() {
        pieChartPackages.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                String label = ((PieEntry) e).getLabel();
                openDetailStatistics("pie", label);
            }
            @Override
            public void onNothingSelected() {}
        });

        barChartPostTypes.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                int x = (int) e.getX();
                if (x >= 0 && x < barLabels.size()) {
                    String label = barLabels.get(x);
                    openDetailStatistics("bar", label);
                }
            }
            @Override
            public void onNothingSelected() {}
        });

        barChartRevenue.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                int x = (int) e.getX();
                if (x >= 0 && x < revenueLabels.size()) {
                    String date = revenueLabels.get(x);
                    openDetailStatistics("line", date);
                }
            }
            @Override
            public void onNothingSelected() {}
        });
    }

    private void openDetailStatistics(String chartType, String value) {
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.putExtra("chartType", chartType);
        intent.putExtra("value", value);
        startActivity(intent);
    }
}
