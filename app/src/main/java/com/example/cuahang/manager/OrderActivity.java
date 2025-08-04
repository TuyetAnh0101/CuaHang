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
import com.google.gson.Gson;

import com.example.cuahang.R;
import com.example.cuahang.adapter.OrderAdapter;
import com.example.cuahang.adapter.PackageSelectAdapter;
import com.example.cuahang.model.Invoices;
import com.example.cuahang.model.Order;
import com.example.cuahang.model.OrderPackage;
import com.example.cuahang.model.Package;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private FloatingActionButton fabAddOrder;
    private final List<Order> orderList = new ArrayList<>();
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
                if (order.getId() == null || order.getId().isEmpty()) {
                    Toast.makeText(OrderActivity.this, "Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "ID đơn hàng null, không thể xoá");
                    return;
                }

                new AlertDialog.Builder(OrderActivity.this)
                        .setTitle("Xác nhận xoá")
                        .setMessage("Bạn có chắc muốn xoá đơn này?")
                        .setPositiveButton("Xoá", (dialog, which) -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Orders")
                                    .document(order.getId())
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(OrderActivity.this, "Đã xoá đơn", Toast.LENGTH_SHORT).show();
                                        loadOrders();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(OrderActivity.this, "Xoá đơn thất bại", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Lỗi xoá đơn: ", e);
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

        Spinner spinnerKhachHang = view.findViewById(R.id.spinnerKhachHang);
        List<String> userNameList = new ArrayList<>();
        HashMap<String, String> userIdMap = new HashMap<>();
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userNameList);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKhachHang.setAdapter(userAdapter);
        loadUserNames(userNameList, userAdapter, userIdMap);

        Spinner spinnerXuLy = view.findViewById(R.id.spinnerXuLy);
        Spinner spinnerThanhToan = view.findViewById(R.id.spinnerThanhToan);
        EditText edtNote = view.findViewById(R.id.edtNote);
        RecyclerView recyclerPackages = view.findViewById(R.id.recyclerPackageSelect);

        setupSpinners(spinnerXuLy, spinnerThanhToan);
        PackageSelectAdapter packageSelectAdapter = setupPackageList(recyclerPackages);

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
                String tenKhach = (spinnerKhachHang.getSelectedItem() != null) ? spinnerKhachHang.getSelectedItem().toString() : "";
                String userId = userIdMap.get(tenKhach);

                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(this, "Chọn khách hàng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Package> selectedPackages = packageSelectAdapter.getSelectedPackages();
                if (selectedPackages.isEmpty()) {
                    Toast.makeText(this, "Chưa chọn gói nào!", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseFirestore.getInstance().collection("Orders").get().addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size() + 1;
                    String newId = String.format("OD%02d", count);
                    String ngay = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    List<OrderPackage> orderPackages = new ArrayList<>();
                    long tongTien = 0;

                    for (Package p : selectedPackages) {
                        OrderPackage op = new OrderPackage(p.getId(), p.getTenGoi(), p.getPackageType(), p.getGiaGiam(), 1);
                        orderPackages.add(op);
                        tongTien += op.getThanhTien();
                        updatePackageSoLuong(p.getId(), -1);
                    }

                    long tongSoLuong = orderPackages.size();

                    Order order = new Order(newId, userId, ngay, tongTien, tongSoLuong,
                            spinnerXuLy.getSelectedItem().toString(),
                            spinnerThanhToan.getSelectedItem().toString(),
                            edtNote.getText().toString().trim(),
                            orderPackages);

                    FirebaseFirestore.getInstance().collection("Orders").document(newId).set(order)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Đã thêm đơn hàng", Toast.LENGTH_SHORT).show();
                                if (order.getStatusThanhToan().equals("Đã thanh toán")) {
                                    createOrUpdateInvoice(order);
                                }
                                dialog.dismiss();
                                loadOrders();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi thêm đơn", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Lỗi lưu đơn", e);
                            });
                });
            });
        });

        dialog.show();
    }

    private void loadUserNames(List<String> userNameList, ArrayAdapter<String> adapter, HashMap<String, String> userIdMap) {
        FirebaseFirestore.getInstance().collection("User")
                .whereEqualTo("role", "USER")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userNameList.clear();
                    userIdMap.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String userId = doc.getString("userId");
                        if (name != null && userId != null) {
                            userNameList.add(name);
                            userIdMap.put(name, userId);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi load user: ", e));
    }
    private void openEditOrderDialog(Order order) {
        View view = LayoutInflater.from(this).inflate(R.layout.add_order_layout, null);

        Spinner spinnerKhachHang = view.findViewById(R.id.spinnerKhachHang);
        List<String> userNameList = new ArrayList<>();
        HashMap<String, String> userIdMap = new HashMap<>();
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userNameList);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKhachHang.setAdapter(userAdapter);

        loadUserNames(userNameList, userAdapter, userIdMap);

        Spinner spinnerXuLy = view.findViewById(R.id.spinnerXuLy);
        Spinner spinnerThanhToan = view.findViewById(R.id.spinnerThanhToan);
        EditText edtNote = view.findViewById(R.id.edtNote);
        RecyclerView recyclerPackages = view.findViewById(R.id.recyclerPackageSelect);

        setupSpinners(spinnerXuLy, spinnerThanhToan);
        PackageSelectAdapter packageSelectAdapter = setupPackageList(recyclerPackages);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sửa đơn hàng")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("Huỷ", (d, w) -> d.dismiss())
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(dlg -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            spinnerKhachHang.post(() -> {
                for (Map.Entry<String, String> entry : userIdMap.entrySet()) {
                    if (entry.getValue().equals(order.getIdKhach())) {
                        int position = userAdapter.getPosition(entry.getKey());
                        if (position >= 0) {
                            spinnerKhachHang.setSelection(position);
                        }
                        break;
                    }
                }
            });

            btnSave.setOnClickListener(v -> {
                String tenKhach = (spinnerKhachHang.getSelectedItem() != null) ? spinnerKhachHang.getSelectedItem().toString() : "";
                String userId = userIdMap.get(tenKhach);

                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(this, "Chọn khách hàng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Package> selectedPackages = packageSelectAdapter.getSelectedPackages();
                if (selectedPackages.isEmpty()) {
                    Toast.makeText(this, "Chưa chọn gói nào!", Toast.LENGTH_SHORT).show();
                    return;
                }

                revertOldOrderPackages(order);

                List<OrderPackage> orderPackages = new ArrayList<>();
                long tongTien = 0;

                for (Package p : selectedPackages) {
                    OrderPackage op = new OrderPackage(p.getId(), p.getTenGoi(), p.getPackageType(), p.getGiaGiam(), 1);
                    orderPackages.add(op);
                    tongTien += op.getThanhTien();
                    updatePackageSoLuong(p.getId(), -1);
                }

                long tongSoLuong = orderPackages.size();

                Order updatedOrder = new Order(order.getId(), userId, order.getNgayDat(), tongTien, tongSoLuong,
                        spinnerXuLy.getSelectedItem().toString(),
                        spinnerThanhToan.getSelectedItem().toString(),
                        edtNote.getText().toString().trim(),
                        orderPackages);

                FirebaseFirestore.getInstance().collection("Orders").document(order.getId())
                        .set(updatedOrder)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã cập nhật đơn", Toast.LENGTH_SHORT).show();
                            if (updatedOrder.getStatusThanhToan().equals("Đã thanh toán")) {
                                createOrUpdateInvoice(updatedOrder);
                            }
                            dialog.dismiss();
                            loadOrders();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi cập nhật đơn", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Lỗi cập nhật đơn", e);
                        });
            });
        });

        dialog.show();
    }


    private void updatePackageSoLuong(String packageId, int changeAmount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Package").document(packageId).get().addOnSuccessListener(doc -> {
            Package p = doc.toObject(Package.class);
            if (p != null) {
                int newSoLuong = p.getSoLuong() + changeAmount;
                if (newSoLuong < 0) newSoLuong = 0;
                db.collection("Package").document(packageId).update("soLuong", newSoLuong);
            }
        });
    }

    private void setupSpinners(Spinner spinnerXuLy, Spinner spinnerThanhToan) {
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
    }

    private PackageSelectAdapter setupPackageList(RecyclerView recyclerPackages) {
        recyclerPackages.setLayoutManager(new LinearLayoutManager(this));
        PackageSelectAdapter adapter = new PackageSelectAdapter(this, new ArrayList<>());
        recyclerPackages.setAdapter(adapter);
        FirebaseFirestore.getInstance().collection("Package")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Package> packages = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Package p = doc.toObject(Package.class);
                        if (p != null) packages.add(p);
                    }
                    adapter.setPackages(packages);
                });
        return adapter;
    }

    private void createOrUpdateInvoice(Order order) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String invoiceId = order.getId(); // Dùng luôn ID đơn hàng làm ID hóa đơn

        double vatPercent = 10.0; // Hoặc load từ SystemConfig nếu bạn muốn, ở đây tạm để cố định

        double totalPrice = order.getTongTien();
        double totalTax = totalPrice * vatPercent / 100;
        double totalDiscount = 0; // Nếu chưa có discount thì để 0
        double totalAmount = totalPrice + totalTax - totalDiscount;

        Invoices invoice = new Invoices(
                invoiceId,
                new Date(),
                totalAmount,
                "Tự động tạo",
                (int) order.getTongSoLuong(),
                totalPrice,
                vatPercent,
                totalDiscount
        );

        db.collection("invoices").document(invoiceId).set(invoice)
                .addOnSuccessListener(unused -> Log.d(TAG, "Đã đồng bộ hóa đơn cho đơn hàng: " + invoiceId))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi đồng bộ hóa đơn", e));
    }
    private void loadUserNames(List<String> userNameList, ArrayAdapter<String> adapter) {
        FirebaseFirestore.getInstance().collection("User")
                .whereEqualTo("role", "USER")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userNameList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            userNameList.add(name);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi load user: ", e));
    }
    private void revertOldOrderPackages(Order oldOrder) {
        for (OrderPackage op : oldOrder.getPackages()) {
            updatePackageSoLuong(op.getPackageId(), op.getSoLuong());
        }
    }
}
