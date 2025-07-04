package com.example.cuahang.manager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.InvoicesAdapter;
import com.example.cuahang.model.Invoices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Invoicesctivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InvoicesAdapter adapter;
    private List<Invoices> invoiceList;
    private FirebaseFirestore db;
    private FloatingActionButton fabAddInvoice;
    private double vatPercent = 10.0; // default value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoicesctivity);

        recyclerView = findViewById(R.id.recyclerInvoices);
        fabAddInvoice = findViewById(R.id.fabAddInvoice);

        db = FirebaseFirestore.getInstance();
        invoiceList = new ArrayList<>();
        adapter = new InvoicesAdapter(invoiceList, new InvoicesAdapter.OnInvoiceActionListener() {
            @Override
            public void onEdit(Invoices invoice) {
                Toast.makeText(Invoicesctivity.this, "Chỉnh sửa chưa hỗ trợ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(Invoices invoice) {
                db.collection("invoices").document(invoice.getId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(Invoicesctivity.this, "Đã xóa hóa đơn", Toast.LENGTH_SHORT).show();
                            loadInvoices();
                        })
                        .addOnFailureListener(e -> Toast.makeText(Invoicesctivity.this, "Lỗi xoá: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAddInvoice.setOnClickListener(v -> showAddInvoiceDialog());

        loadSystemConfig();
        loadInvoices();
    }

    private void loadInvoices() {
        db.collection("invoices")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    invoiceList.clear();
                    for (var doc : querySnapshot) {
                        Invoices invoice = doc.toObject(Invoices.class);
                        invoice.setId(doc.getId());
                        invoiceList.add(invoice);
                    }
                    adapter.setInvoiceList(invoiceList);
                });
    }

    private void loadSystemConfig() {
        db.collection("SystemConfig").document("default")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Double vat = snapshot.getDouble("vatPercent");
                        if (vat != null) {
                            vatPercent = vat;
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không thể tải cấu hình VAT", Toast.LENGTH_SHORT).show());
    }

    private void showAddInvoiceDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.add_invoices_layout, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        EditText edtNhanVien = view.findViewById(R.id.edtNhanVien);
        EditText edtTongSoLuong = view.findViewById(R.id.edtTongSoLuong);
        EditText edtTongGia = view.findViewById(R.id.edtTongGia);
        EditText edtGiamGia = view.findViewById(R.id.edtGiamGia);
        EditText edtVAT = view.findViewById(R.id.edtVAT);
        EditText edtTongThanhToan = view.findViewById(R.id.edtTongThanhToan);
        Button btnLuuHoaDon = view.findViewById(R.id.btnLuuHoaDon);

        edtVAT.setText(String.valueOf(vatPercent));
        edtVAT.setEnabled(false);

        btnLuuHoaDon.setOnClickListener(v -> {
            try {
                String nhanVien = edtNhanVien.getText().toString().trim();
                int tongSoLuong = Integer.parseInt(edtTongSoLuong.getText().toString().trim());
                double tongGia = Double.parseDouble(edtTongGia.getText().toString().trim());
                double giamGia = Double.parseDouble(edtGiamGia.getText().toString().trim());

                double vat = vatPercent;
                double tongThanhToan = tongGia + (tongGia * vat / 100) - giamGia;

                String id = UUID.randomUUID().toString();
                Date ngayTao = new Date();

                Invoices invoice = new Invoices(id, ngayTao, tongThanhToan, nhanVien, tongSoLuong, tongGia, vat, giamGia);

                db.collection("invoices").document(id)
                        .set(invoice)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã lưu hóa đơn", Toast.LENGTH_SHORT).show();
                            loadInvoices();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                Toast.makeText(this, "Lỗi nhập dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadInvoices();
    }
}