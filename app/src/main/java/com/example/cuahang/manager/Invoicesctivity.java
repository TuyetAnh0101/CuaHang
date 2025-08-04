package com.example.cuahang.manager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.InvoicesAdapter;
import com.example.cuahang.model.Invoices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Invoicesctivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InvoicesAdapter adapter;
    private List<Invoices> invoiceList;
    private FirebaseFirestore db;
    private FloatingActionButton fabAddInvoice;
    private double vatPercent = 10.0;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoicesctivity);

        recyclerView = findViewById(R.id.recyclerInvoices);
        fabAddInvoice = findViewById(R.id.fabAddInvoice);

        db = FirebaseFirestore.getInstance();
        invoiceList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();

        adapter = new InvoicesAdapter(invoiceList, new InvoicesAdapter.OnInvoiceActionListener() {
            @Override
            public void onEdit(Invoices invoice) {
                showEditInvoiceDialog(invoice);
            }

            @Override
            public void onDelete(Invoices invoice) {
                db.collection("invoices").document(invoice.getId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(Invoicesctivity.this, "Đã xóa hóa đơn", Toast.LENGTH_SHORT).show();
                            loadInvoices();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(Invoicesctivity.this, "Lỗi xoá: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }

            @Override
            public void onClick(Invoices invoice) {
                Intent intent = new Intent(Invoicesctivity.this, InvoiceDetailActivity.class);
                intent.putExtra("invoiceId", invoice.getId());
                startActivity(intent);
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

    private double tinhTongThanhToan(double tongGia, double giamGia, double vatPercent) {
        return tongGia + (tongGia * vatPercent / 100) - giamGia;
    }

    private void showAddInvoiceDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.add_invoices_layout, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        Spinner spinnerNhanVien = view.findViewById(R.id.spinnerNhanVien);
        Spinner spinnerTrangThai = view.findViewById(R.id.spinnerStatus);
        EditText edtTongSoLuong = view.findViewById(R.id.edtTongSoLuong);
        EditText edtTongGia = view.findViewById(R.id.edtTongGia);
        EditText edtGiamGia = view.findViewById(R.id.edtGiamGia);
        EditText edtVAT = view.findViewById(R.id.edtVAT);
        EditText edtTongThanhToan = view.findViewById(R.id.edtTongThanhToan);
        Button btnLuuHoaDon = view.findViewById(R.id.btnLuuHoaDon);

        edtVAT.setText(String.valueOf(vatPercent));
        edtVAT.setEnabled(false); // không cho người dùng sửa VAT

        loadNhanVienToSpinner(spinnerNhanVien);
        loadTrangThaiToSpinner(spinnerTrangThai, null);

        // Tự tính tổng thành tiền mỗi khi giá hoặc giảm giá thay đổi
        setupAutoTinhTong(edtTongGia, edtGiamGia, edtTongThanhToan);

        btnLuuHoaDon.setOnClickListener(v -> {
            try {
                String nhanVien = spinnerNhanVien.getSelectedItem().toString();
                String trangThai = spinnerTrangThai.getSelectedItem().toString();
                int tongSoLuong = Integer.parseInt(edtTongSoLuong.getText().toString().trim());
                double tongGia = Double.parseDouble(edtTongGia.getText().toString().trim());
                double giamGia = Double.parseDouble(edtGiamGia.getText().toString().trim());

                double tongThanhToan = tinhTongThanhToan(tongGia, giamGia, vatPercent);

                getNextInvoiceId(nextId -> {
                    Date ngayTao = new Date();

                    Invoices invoice = new Invoices(nextId, ngayTao, tongThanhToan, nhanVien, tongSoLuong, tongGia, vatPercent, giamGia);
                    invoice.setStatus(trangThai);

                    db.collection("invoices").document(nextId)
                            .set(invoice)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Đã lưu hóa đơn", Toast.LENGTH_SHORT).show();
                                loadInvoices();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                });

            } catch (Exception e) {
                Toast.makeText(this, "Lỗi nhập dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void getNextInvoiceId(OnNextIdListener listener) {
        db.collection("invoices")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int max = 0;
                    for (DocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        if (id.startsWith("IV")) {
                            try {
                                int num = Integer.parseInt(id.substring(2));
                                if (num > max) max = num;
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    String nextId = String.format("IV%02d", max + 1);
                    listener.onNextId(nextId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tạo ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private interface OnNextIdListener {
        void onNextId(String nextId);
    }

    private void setupAutoTinhTong(EditText edtTongGia, EditText edtGiamGia, EditText edtTongThanhToan) {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double tongGia = Double.parseDouble(edtTongGia.getText().toString().trim());
                    double giamGia = Double.parseDouble(edtGiamGia.getText().toString().trim());
                    double tongThanhToan = tinhTongThanhToan(tongGia, giamGia, vatPercent);
                    edtTongThanhToan.setText(String.valueOf(tongThanhToan));
                } catch (Exception e) {
                    edtTongThanhToan.setText("");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        edtTongGia.addTextChangedListener(watcher);
        edtGiamGia.addTextChangedListener(watcher);
    }

    private void showEditInvoiceDialog(Invoices invoice) {
        View view = LayoutInflater.from(this).inflate(R.layout.add_invoices_layout, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        Spinner spinnerNhanVien = view.findViewById(R.id.spinnerNhanVien);
        Spinner spinnerTrangThai = view.findViewById(R.id.spinnerStatus);
        EditText edtTongSoLuong = view.findViewById(R.id.edtTongSoLuong);
        EditText edtTongGia = view.findViewById(R.id.edtTongGia);
        EditText edtGiamGia = view.findViewById(R.id.edtGiamGia);
        EditText edtVAT = view.findViewById(R.id.edtVAT);
        EditText edtTongThanhToan = view.findViewById(R.id.edtTongThanhToan);
        Button btnLuuHoaDon = view.findViewById(R.id.btnLuuHoaDon);

        edtVAT.setText(String.valueOf(vatPercent));
        edtVAT.setEnabled(false);

        edtTongSoLuong.setText(String.valueOf(invoice.getTotalQuantity()));
        edtTongGia.setText(String.valueOf(invoice.getTotalPrice()));
        edtGiamGia.setText(String.valueOf(invoice.getTotalDiscount()));
        edtTongThanhToan.setText(String.valueOf(invoice.getTotalAmount()));

        loadNhanVienToSpinner(spinnerNhanVien, invoice.getCreatedBy());
        loadTrangThaiToSpinner(spinnerTrangThai, invoice.getStatus());

        setupAutoTinhTong(edtTongGia, edtGiamGia, edtTongThanhToan);

        btnLuuHoaDon.setText("Lưu thay đổi");

        btnLuuHoaDon.setOnClickListener(v -> {
            try {
                String nhanVien = spinnerNhanVien.getSelectedItem().toString();
                String trangThai = spinnerTrangThai.getSelectedItem().toString();
                int tongSoLuong = Integer.parseInt(edtTongSoLuong.getText().toString().trim());
                double tongGia = Double.parseDouble(edtTongGia.getText().toString().trim());
                double giamGia = Double.parseDouble(edtGiamGia.getText().toString().trim());
                double tongThanhToan = tinhTongThanhToan(tongGia, giamGia, vatPercent);

                invoice.setCreatedBy(nhanVien);
                invoice.setStatus(trangThai);
                invoice.setTotalQuantity(tongSoLuong);
                invoice.setTotalPrice(tongGia);
                invoice.setTotalDiscount(giamGia);
                invoice.setTotalTax(vatPercent);
                invoice.setTotalAmount(tongThanhToan);

                db.collection("invoices").document(invoice.getId())
                        .set(invoice)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã cập nhật hóa đơn", Toast.LENGTH_SHORT).show();
                            loadInvoices();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi nhập dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void loadNhanVienToSpinner(Spinner spinner) {
        loadNhanVienToSpinner(spinner, null);
    }

    private void loadNhanVienToSpinner(Spinner spinner, String preSelectName) {
        db.collection("User")
                .whereIn("role", Arrays.asList("STAFF", "ADMIN"))
                .get()
                .addOnSuccessListener(query -> {
                    List<String> staffNames = new ArrayList<>();
                    for (var doc : query) {
                        staffNames.add(doc.getString("name"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, staffNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    if (preSelectName != null) {
                        int position = staffNames.indexOf(preSelectName);
                        if (position >= 0) spinner.setSelection(position);
                    }
                });
    }

    private void loadTrangThaiToSpinner(Spinner spinner, String preSelectStatus) {
        List<String> statusList = Arrays.asList("Đã thanh toán", "Chưa thanh toán");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (preSelectStatus != null) {
            int position = statusList.indexOf(preSelectStatus);
            if (position >= 0) spinner.setSelection(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInvoices();
    }
}
