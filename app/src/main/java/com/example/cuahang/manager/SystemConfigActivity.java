package com.example.cuahang.manager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cuahang.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SystemConfigActivity extends AppCompatActivity {

    private EditText edtVatPercent, edtDefaultUnit, edtImageLimit;
    private Button btnSaveConfig;
    private FirebaseFirestore db;
    private DocumentReference configRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_config);

        edtVatPercent = findViewById(R.id.edtVatPercent);
        edtDefaultUnit = findViewById(R.id.edtDefaultUnit);
        edtImageLimit = findViewById(R.id.edtImageLimit);
        btnSaveConfig = findViewById(R.id.btnSaveConfig);

        db = FirebaseFirestore.getInstance();
        configRef = db.collection("SystemConfig").document("default");

        loadConfig();

        btnSaveConfig.setOnClickListener(v -> saveConfig());
    }

    private void loadConfig() {
        configRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Double vat = snapshot.getDouble("vatPercent");
                String unit = snapshot.getString("defaultUnit");
                Long limitMB = snapshot.getLong("imageUploadLimitMB");

                edtVatPercent.setText(String.valueOf(vat != null ? vat : 10.0)); // VAT mặc định 10%
                edtDefaultUnit.setText(unit != null ? unit : "gói");             // đơn vị: gói, tháng, lần
                edtImageLimit.setText(String.valueOf(limitMB != null ? limitMB : 5)); // giới hạn ảnh: 5MB
            } else {
                // Set mặc định nếu chưa tồn tại document
                edtVatPercent.setText("10");
                edtDefaultUnit.setText("gói");
                edtImageLimit.setText("5");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải cấu hình", Toast.LENGTH_SHORT).show();
        });
    }


    private void saveConfig() {
        String vatStr = edtVatPercent.getText().toString().trim();
        String unit = edtDefaultUnit.getText().toString().trim();
        String limitStr = edtImageLimit.getText().toString().trim();

        if (vatStr.isEmpty() || unit.isEmpty() || limitStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double vat = Double.parseDouble(vatStr);
            long limitMB = Long.parseLong(limitStr);

            if (vat < 0 || vat > 20) {
                Toast.makeText(this, "VAT nên nằm trong khoảng 0–20%", Toast.LENGTH_SHORT).show();
                return;
            }

            if (limitMB < 1 || limitMB > 20) {
                Toast.makeText(this, "Dung lượng ảnh từ 1MB đến 20MB", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> config = new HashMap<>();
            config.put("vatPercent", vat);
            config.put("defaultUnit", unit); // nên là: "gói", "tháng", "lần"
            config.put("imageUploadLimitMB", limitMB);

            configRef.set(config)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Đã lưu cấu hình", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi lưu", Toast.LENGTH_SHORT).show());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "VAT và Giới hạn ảnh phải là số", Toast.LENGTH_SHORT).show();
        }
    }
}
