package com.example.carsale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.carsale.Adapter.BannerAdapter;
import com.example.carsale.Adapter.CarAdapter;
import com.example.carsale.Adapter.CarMakeAdapter;
import com.example.carsale.Database.CarHelper;
import com.example.carsale.Model.Car;
import com.example.carsale.Model.CarMake;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {
    private Spinner spinnerCity;
    private final List<String> cityList = new ArrayList<>();

    private ViewPager2 bannerViewPager;
    private BannerAdapter bannerAdapter;
    private LinearLayout indicatorLayout;
    private ImageView dot1, dot2, dot3, dot4;
    private final List<Integer> bannerImages = Arrays.asList(
            R.drawable.banner_1,
            R.drawable.banner_2,
            R.drawable.banner_3,
            R.drawable.banner_4,
            R.drawable.banner_background,
            R.drawable.background_home
    );

    private RecyclerView recyclerViewBrands;
    private RecyclerView recyclerViewCars;
    private CarMakeAdapter carMakeAdapter;
    private CarAdapter carAdapter;
    private final List<CarMake> carMakeList = new ArrayList<>();
    private final List<Car> carList = new ArrayList<>();
    private final List<Car> cachedCars = new ArrayList<>();
    private boolean isCarsLoaded = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerCity = view.findViewById(R.id.spinner_city);
        fetchCities();

        initBannerViewPager(view);
        initRecyclerViews(view);
        loadCarMakes();
        loadAndCacheCars();
    }

    // Gọi API lấy danh sách thành phố (giả lập endpoint)
    private void fetchCities() {
        cityList.clear();
        cityList.add("Hà Nội");
        cityList.add("Hồ Chí Minh");
        cityList.add("Đà Nẵng");
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getContext() != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        R.layout.spinner_item, // Custom layout có text màu trắng
                        cityList
                );
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item); // Optional
                spinnerCity.setAdapter(adapter);
            }
        });
    }

    private void initBannerViewPager(View view) {
        Context context = getContext();
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        indicatorLayout = view.findViewById(R.id.indicator_layout);
        dot1 = view.findViewById(R.id.dot_1);
        dot2 = view.findViewById(R.id.dot_2);
        dot3 = view.findViewById(R.id.dot_3);
        dot4 = view.findViewById(R.id.dot_4);

        bannerAdapter = new BannerAdapter(context, bannerImages);
        bannerAdapter.setOnBannerClickListener(position -> {
            // Xử lý khi click vào banner
            showToast("Banner " + (position + 1) + " được click");
        });

        bannerViewPager.setAdapter(bannerAdapter);

        // Bắt đầu từ vị trí giữa để có thể scroll cả 2 hướng
        bannerViewPager.setCurrentItem(Integer.MAX_VALUE / 2, false);

        // Thêm listener để cập nhật indicator
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicator(position % bannerImages.size());
            }
        });

        // Tự động chuyển banner mỗi 3 giây
        startAutoScroll();
    }

    private void initRecyclerViews(View view) {
        Context context = getContext();

        recyclerViewBrands = view.findViewById(R.id.brands_layout);
        recyclerViewBrands.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        carMakeAdapter = new CarMakeAdapter(context, carMakeList, carMake -> filterCarsByMake(carMake != null ? carMake.getName() : null));
        recyclerViewBrands.setAdapter(carMakeAdapter);

        recyclerViewCars = view.findViewById(R.id.listings_layout);
        recyclerViewCars.setLayoutManager(new GridLayoutManager(context, 2));
        carAdapter = new CarAdapter(context, carList, false, "", new CarAdapter.OnCarActionListener() {
            @Override
            public void onEdit(Car car) {
                Intent intent = new Intent(getActivity(), AddEditCarActivity.class);
                intent.putExtra("car", car);
                startActivity(intent);
            }

            @Override
            public void onDelete(Car car) {
                if (context != null) {
                    Toast.makeText(context, "Bạn không có quyền xoá xe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCarClick(Car car) {
                if (car != null) {
                    Intent intent = new Intent(getActivity(), DetailCarActivity.class);
                    intent.putExtra("car", car);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Xe không hợp lệ!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerViewCars.setAdapter(carAdapter);

        // Tối ưu: setHasFixedSize(true) cho hiệu năng tốt hơn nếu item kích thước cố định
        recyclerViewBrands.setHasFixedSize(true);
        recyclerViewCars.setHasFixedSize(true);
    }

    private void loadCarMakes() {
        FirebaseFirestore.getInstance()
                .collection("car_makes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    carMakeList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CarMake make = doc.toObject(CarMake.class);
                        carMakeList.add(make);
                    }
                    carMakeAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Lỗi tải hãng xe: " + e.getMessage()));
    }

    private void loadAndCacheCars() {
        // Không tải lại nếu đã có dữ liệu (tối ưu khi back lại fragment)
        if (isCarsLoaded) {
            filterCarsByMake(null);
            return;
        }
        CarHelper.getInstance().getAllCars(new CarHelper.CarsListCallback() {
            @Override
            public void onSuccess(List<Car> cars) {
                cachedCars.clear();
                cachedCars.addAll(cars);
                isCarsLoaded = true;
                filterCarsByMake(null); // Load tất cả xe ban đầu
            }

            @Override
            public void onError(String error) {
                showToast("Lỗi tải xe: " + error);
            }
        });
    }

    private void filterCarsByMake(@Nullable String make) {
        carList.clear();
        for (Car car : cachedCars) {
            if ("available".equalsIgnoreCase(car.getStatus()) &&
                    (make == null || make.equalsIgnoreCase(car.getMake()))) {
                carList.add(car);
            }
        }
        carAdapter.notifyDataSetChanged();
    }

    private void showToast(String message) {
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateIndicator(int position) {
        // Reset tất cả dots
        dot1.setAlpha(0.5f);
        dot2.setAlpha(0.5f);
        dot3.setAlpha(0.5f);
        dot4.setAlpha(0.5f);

        // Highlight dot hiện tại
        switch (position) {
            case 0:
                dot1.setAlpha(1.0f);
                break;
            case 1:
                dot2.setAlpha(1.0f);
                break;
            case 2:
                dot3.setAlpha(1.0f);
                break;
            case 3:
                dot4.setAlpha(1.0f);
                break;
        }
    }

    private void startAutoScroll() {
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (bannerViewPager != null && isAdded()) {
                    bannerViewPager.setCurrentItem(bannerViewPager.getCurrentItem() + 1);
                }
                handler.postDelayed(this, 3000); // 3 giây
            }
        };
        handler.postDelayed(runnable, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dừng auto scroll khi fragment bị destroy
        if (bannerViewPager != null) {
            bannerViewPager.unregisterOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {});
            bannerViewPager.setAdapter(null);
        }
    }
}