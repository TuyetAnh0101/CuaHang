package com.example.cuahang.manager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuahang.R;
import com.example.cuahang.adapter.OrderAdapter;
import com.example.cuahang.adapter.PackageSelectAdapter;
import com.example.cuahang.model.Order;
import com.example.cuahang.model.OrderPackage;
import com.example.cuahang.model.Package;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private FloatingActionButton fabAddOrder;
    private List<Order> orderList = new ArrayList<>();
    private OrderAdapter orderAdapter;

    private static final String TAG = "OrderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        recyclerOrders = findViewById(R.id.recyclerOrders);
        fabAddOrder = findViewById(R.id.fabAddOrder);

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this, orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onEdit(Order order) {
                openEditOrderDialog(order);
            }

            @Override
            public void onDelete(Order order) {
                new AlertDialog.Builder(OrderActivity.this)
                        .setTitle("Xác nhận xoá")
                        .setMessage("Bạn có chắc muốn xoá đơn này?")
                        .setPositiveButton("Xoá", (dialog, which) -> {
                            FirebaseFirestore.getInstance().collection("Orders")
                                    .document(order.getId())
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(OrderActivity.this, "Đã xoá đơn", Toast.LENGTH_SHORT).show();
                                        loadOrders();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(OrderActivity.this, "Xoá đơn thất bại", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });
        recyclerOrders.setAdapter(orderAdapter);

        fabAddOrder.setOnClickListener(v -> openAddOrderDialog());

        loadOrders();
    }

    private void loadOrders() {
        FirebaseFirestore.getInstance().collection("Orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            orderList.add(order);
                        }
                    }
                    orderAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OrderActivity.this, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi tải đơn hàng từ Firestore", e);
                });
    }

    private void openAddOrderDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.add_order_layout, null);

        EditText edtKhachHang = view.findViewById(R.id.edtKhachHang);
        Spinner spinnerXuLy = view.findViewById(R.id.spinnerXuLy);
        Spinner spinnerThanhToan = view.findViewById(R.id.spinnerThanhToan);
        EditText edtNote = view.findViewById(R.id.edtNote);
        RecyclerView recyclerPackages = view.findViewById(R.id.recyclerPackageSelect);

        ArrayAdapter<String> xuLyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Chờ xử lý", "Đang xử lý", "Hoàn tất"));
        xuLyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerXuLy.setAdapter(xuLyAdapter);

        ArrayAdapter<String> thanhToanAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Chưa thanh toán", "Đã thanh toán"));
        thanhToanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerThanhToan.setAdapter(thanhToanAdapter);

        recyclerPackages.setLayoutManager(new LinearLayoutManager(this));
        PackageSelectAdapter packageSelectAdapter = new PackageSelectAdapter(this, new ArrayList<>());
        recyclerPackages.setAdapter(packageSelectAdapter);

        FirebaseFirestore.getInstance().collection("Package")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Package> packages = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Package p = doc.toObject(Package.class);
                        if (p != null) packages.add(p);
                    }
                    packageSelectAdapter.setPackages(packages);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải gói từ Firestore", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Load packages error", e);
                });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Tạo đơn hàng mới")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("Huỷ", (d, w) -> d.dismiss())
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(dlg -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String tenKhach = edtKhachHang.getText().toString().trim();
                if (tenKhach.isEmpty()) {
                    edtKhachHang.setError("Nhập tên khách hàng");
                    return;
                }

                List<Package> selectedPackages = packageSelectAdapter.getSelectedPackages();
                if (selectedPackages.isEmpty()) {
                    Toast.makeText(this, "Chưa chọn gói nào!", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<OrderPackage> orderPackages = new ArrayList<>();
                long tongTien = 0;
                long tongSoLuong = 0;

                for (Package p : selectedPackages) {
                    OrderPackage op = new OrderPackage(
                            p.getId(),
                            p.getTenGoi(),
                            p.getGiaGiam(),
                            p.getSoLuong()
                    );
                    orderPackages.add(op);
                    tongTien += op.getThanhTien();
                    tongSoLuong += op.getSoLuong();
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String id = db.collection("Orders").document().getId();
                String ngay = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                if (id == null) {
                    Toast.makeText(this, "Lỗi tạo ID đơn hàng", Toast.LENGTH_SHORT).show();
                    return;
                }

                Order order = new Order(
                        id,
                        tenKhach,
                        ngay,
                        tongTien,
                        tongSoLuong,
                        spinnerXuLy.getSelectedItem().toString(),
                        spinnerThanhToan.getSelectedItem().toString(),
                        edtNote.getText().toString().trim(),
                        orderPackages
                );

                db.collection("Orders")
                        .document(id)
                        .set(order)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã thêm đơn hàng", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadOrders();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi khi thêm đơn", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Lỗi lưu đơn", e);
                        });
            });
        });

        dialog.show();
    }

    private void openEditOrderDialog(Order order) {
        View view = LayoutInflater.from(this).inflate(R.layout.add_order_layout, null);

        EditText edtKhachHang = view.findViewById(R.id.edtKhachHang);
        Spinner spinnerXuLy = view.findViewById(R.id.spinnerXuLy);
        Spinner spinnerThanhToan = view.findViewById(R.id.spinnerThanhToan);
        EditText edtNote = view.findViewById(R.id.edtNote);
        RecyclerView recyclerPackages = view.findViewById(R.id.recyclerPackageSelect);

        ArrayAdapter<String> xuLyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Chờ xử lý", "Đang xử lý", "Hoàn tất"));
        xuLyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerXuLy.setAdapter(xuLyAdapter);

        ArrayAdapter<String> thanhToanAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Chưa thanh toán", "Đã thanh toán"));
        thanhToanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerThanhToan.setAdapter(thanhToanAdapter);

        recyclerPackages.setLayoutManager(new LinearLayoutManager(this));
        PackageSelectAdapter packageSelectAdapter = new PackageSelectAdapter(this, new ArrayList<>());
        recyclerPackages.setAdapter(packageSelectAdapter);

        FirebaseFirestore.getInstance().collection("Package")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Package> packages = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Package p = doc.toObject(Package.class);
                        if (p != null) packages.add(p);
                    }

                    // Gán packages cho adapter
                    packageSelectAdapter.setPackages(packages);

                    // Gán số lượng cho từng package dựa trên order.getPackages()
                    List<OrderPackage> orderPackages = order.getPackages();
                    for (Package p : packages) {
                        for (OrderPackage op : orderPackages) {
                            if (p.getId().equals(op.getPackageId())) {
                                p.setSoLuong(op.getSoLuong());
                                break;
                            }
                        }
                    }

                    packageSelectAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải gói từ Firestore", Toast.LENGTH_SHORT).show();
                });

        // Set dữ liệu cũ lên các trường
        edtKhachHang.setText(order.getIdKhach());
        spinnerXuLy.setSelection(xuLyAdapter.getPosition(order.getStatusXuLy()));
        spinnerThanhToan.setSelection(thanhToanAdapter.getPosition(order.getStatusThanhToan()));
        edtNote.setText(order.getNote());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sửa đơn hàng")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("Huỷ", (d, w) -> d.dismiss())
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(dlg -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String tenKhach = edtKhachHang.getText().toString().trim();
                if (tenKhach.isEmpty()) {
                    edtKhachHang.setError("Nhập tên khách hàng");
                    return;
                }

                List<Package> selectedPackages = packageSelectAdapter.getSelectedPackages();
                if (selectedPackages.isEmpty()) {
                    Toast.makeText(this, "Chưa chọn gói nào!", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<OrderPackage> orderPackages = new ArrayList<>();
                long tongTien = 0;
                long tongSoLuong = 0;

                for (Package p : selectedPackages) {
                    OrderPackage op = new OrderPackage(
                            p.getId(),
                            p.getTenGoi(),
                            p.getGiaGiam(),
                            p.getSoLuong()
                    );
                    orderPackages.add(op);
                    tongTien += op.getThanhTien();
                    tongSoLuong += op.getSoLuong();
                }

                Order updatedOrder = new Order(
                        order.getId(),
                        tenKhach,
                        order.getNgayDat(),
                        tongTien,
                        tongSoLuong,
                        spinnerXuLy.getSelectedItem().toString(),
                        spinnerThanhToan.getSelectedItem().toString(),
                        edtNote.getText().toString().trim(),
                        orderPackages
                );

                FirebaseFirestore.getInstance().collection("Orders")
                        .document(order.getId())
                        .set(updatedOrder)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã cập nhật đơn", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadOrders();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi cập nhật đơn", Toast.LENGTH_SHORT).show();
                        });
            });
        });

        dialog.show();
    }
}
