package com.example.carsale;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.Adapter.CarAdapter;
import com.example.carsale.Database.CarHelper;
import com.example.carsale.Model.Car;

import java.util.ArrayList;
import java.util.List;

public class AdminFragment extends Fragment {

    private RecyclerView recyclerView;
    private CarAdapter adapter;
    private List<Car> carList = new ArrayList<>();

    public AdminFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewCars);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Khởi tạo adapter trước khi set
        adapter = new CarAdapter(getContext(), carList, new CarAdapter.OnCarActionListener() {
            @Override
            public void onEdit(Car car) {
                Intent intent = new Intent(getActivity(), AddEditCarActivity.class);
                intent.putExtra("car", car);
                startActivity(intent);
            }

            @Override
            public void onDelete(Car car) {
                List<String> imageUrls = car.getImageUrls() != null ? car.getImageUrls() : new ArrayList<>();
                CarHelper.getInstance().deleteCar(car.getId(), imageUrls, new CarHelper.CarCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        loadCars(); // Làm mới danh sách sau khi xóa
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        recyclerView.setAdapter(adapter); // Gán adapter sau khi khởi tạo
        loadCars(); // Tải dữ liệu khi fragment được tạo

        Button btnAddCar = view.findViewById(R.id.btnAddCar);
        btnAddCar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditCarActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCars(); // Làm mới danh sách khi quay về fragment
    }

    private void loadCars() {
        if (getContext() == null) return; // Kiểm tra context tránh crash
        CarHelper.getInstance().getAllCars(new CarHelper.CarsListCallback() {
            @Override
            public void onSuccess(List<Car> cars) {
                carList.clear();
                carList.addAll(cars);
                if (adapter != null) {
                    adapter.notifyDataSetChanged(); // Cập nhật adapter
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi tải danh sách: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}