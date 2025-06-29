package com.example.carsale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.Adapter.CarAdapter;
import com.example.carsale.Adapter.CarMakeAdapter;
import com.example.carsale.Database.CarHelper;
import com.example.carsale.Model.Car;
import com.example.carsale.Model.CarMake;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewBrands;
    private RecyclerView recyclerViewCars;
    private CarMakeAdapter carMakeAdapter;
    private CarAdapter carAdapter;
    private final List<CarMake> carMakeList = new ArrayList<>();
    private final List<Car> carList = new ArrayList<>();
    private final List<Car> cachedCars = new ArrayList<>();
    private boolean isCarsLoaded = false;

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerViews(view);
        loadCarMakes();
        loadAndCacheCars();
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
}