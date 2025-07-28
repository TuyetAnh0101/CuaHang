package com.example.cuahang.UserFragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cuahang.R;
import com.example.cuahang.adapter.BannerAdapter;
import com.example.cuahang.adapter.CategoryHorizontalAdapter;
import com.example.cuahang.adapter.PackageUserAdapter;
import com.example.cuahang.manager.CartActivity;
import com.example.cuahang.manager.PackageDetailActivity;
import com.example.cuahang.model.Category;
import com.example.cuahang.model.Package;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NguoiDungFragment extends Fragment implements CategoryHorizontalAdapter.OnCategoryClickListener {

    private RecyclerView recyclerViewPackages;
    private RecyclerView recyclerViewCategories;
    private EditText edtSearch;
    private ImageView ivCart;

    private PackageUserAdapter packageAdapter;
    private CategoryHorizontalAdapter categoryAdapter;

    private List<Package> packageList;
    private List<Category> categoryList;

    private FirebaseFirestore db;

    private ViewPager2 viewPagerBanner;
    private Handler bannerHandler = new Handler();
    private int currentBannerIndex = 0;
    private Runnable bannerRunnable;

    public NguoiDungFragment() {}

    public static NguoiDungFragment newInstance() {
        return new NguoiDungFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nguoi_dung, container, false);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ các view
        edtSearch = view.findViewById(R.id.edtSearch);
        ivCart = view.findViewById(R.id.ivCart);
        viewPagerBanner = view.findViewById(R.id.bannerViewPager);
        recyclerViewPackages = view.findViewById(R.id.recyclerPackages);
        recyclerViewCategories = view.findViewById(R.id.categoryContainer);

        // Setup RecyclerView Packages
        recyclerViewPackages.setLayoutManager(new LinearLayoutManager(getContext()));
        packageList = new ArrayList<>();
        packageAdapter = new PackageUserAdapter(getContext(), packageList, item -> {
            Log.d("PACKAGE_CLICK", "Gói được click: " + item.getTenGoi());
            Intent intent = new Intent(getContext(), PackageDetailActivity.class);
            intent.putExtra("id", item.getId());
            startActivity(intent);
        });
        recyclerViewPackages.setAdapter(packageAdapter);

        // Setup RecyclerView Categories ngang
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryHorizontalAdapter(getContext(), categoryList, this);
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Thiết lập sự kiện tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPackagesByName(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Setup banner động
        List<Integer> bannerImages = new ArrayList<>();
        bannerImages.add(R.drawable.banner1);
        bannerImages.add(R.drawable.banner2);
        bannerImages.add(R.drawable.banner3);
        BannerAdapter bannerAdapter = new BannerAdapter(requireContext(), bannerImages);
        viewPagerBanner.setAdapter(bannerAdapter);

        bannerRunnable = () -> {
            currentBannerIndex = (currentBannerIndex + 1) % bannerImages.size();
            viewPagerBanner.setCurrentItem(currentBannerIndex, true);
            bannerHandler.postDelayed(bannerRunnable, 3000);
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);

        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentBannerIndex = position;
            }
        });

        // Xử lý click vào icon giỏ hàng
        ivCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CartActivity.class);
            startActivity(intent);
        });

        // Load dữ liệu
        loadCategoriesFromFirestore();
        loadPackagesFromFirestore();

        return view;
    }

    private void loadCategoriesFromFirestore() {
        categoryList.clear();
        categoryList.add(new Category("all", "Tất cả", ""));

        db.collection("Category")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Category category = snapshot.toObject(Category.class);
                        if (category != null) {
                            category.setId(snapshot.getId());
                            categoryList.add(category);
                        }
                    }
                    categoryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPackagesFromFirestore() {
        db.collection("Package")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Package> allPackages = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Package pkg = snapshot.toObject(Package.class);
                        if (pkg != null) {
                            allPackages.add(pkg);
                        }
                    }
                    packageList.clear();
                    packageList.addAll(allPackages);
                    packageAdapter.updateList(allPackages);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải gói", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPackagesByCategory(String categoryId) {
        if ("all".equals(categoryId)) {
            loadPackagesFromFirestore();
            return;
        }

        db.collection("Package")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Package> filtered = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Package pkg = snapshot.toObject(Package.class);
                        if (pkg != null) {
                            filtered.add(pkg);
                        }
                    }

                    if (filtered.isEmpty()) {
                        Toast.makeText(getContext(), "Không có gói nào thuộc danh mục này", Toast.LENGTH_SHORT).show();
                    }

                    packageAdapter.updateList(filtered);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi lọc gói", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterPackagesByName(String query) {
        List<Package> filtered = new ArrayList<>();
        for (Package pkg : packageList) {
            if (pkg.getTenGoi().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(pkg);
            }
        }
        packageAdapter.updateList(filtered);
    }

    @Override
    public void onCategoryClick(Category category) {
        if (category != null) {
            loadPackagesByCategory(category.getId());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bannerHandler.removeCallbacks(bannerRunnable);
    }
}
