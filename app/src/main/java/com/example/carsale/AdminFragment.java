package com.example.carsale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.Adapter.CarAdapter;
import com.example.carsale.Database.CarHelper;
import com.example.carsale.Model.Car;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminFragment extends Fragment {

    private RecyclerView recyclerView;
    private CarAdapter adapter;
    private List<Car> carList = new ArrayList<>();

    private Spinner spinnerCarMake;
    private ImageButton btnAddCarMake, btnOpenDrawer;
    private Button btnAddCar;

    private List<String> carMakes = new ArrayList<>();
    private ArrayAdapter<String> carMakeAdapter;

    private boolean isAdmin = false;
    private String currentUserId = "";
    private DrawerLayout drawerLayout;

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

        spinnerCarMake = view.findViewById(R.id.spinnerCarMake);
        btnAddCarMake = view.findViewById(R.id.btnAddCarMake);
        btnAddCar = view.findViewById(R.id.btnAddCar);
        btnOpenDrawer = view.findViewById(R.id.btnOpenDrawer);
        drawerLayout = requireActivity().findViewById(R.id.drawer_layout);

        carMakeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, carMakes);
        spinnerCarMake.setAdapter(carMakeAdapter);
        Bundle args = getArguments();
        if (args != null) {
            isAdmin = args.getBoolean("isAdmin", false);
            currentUserId = args.getString("userId", "");
        }

        spinnerCarMake.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                String selectedMake = carMakes.get(position);
                loadCarsByMake(selectedMake);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAddCarMake.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddBrandActivity.class);
            startActivity(intent);
        });

        btnAddCar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditCarActivity.class);
            startActivity(intent);
        });

        btnOpenDrawer.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        adapter = new CarAdapter(getContext(), carList, isAdmin, currentUserId, new CarAdapter.OnCarActionListener() {
            @Override
            public void onEdit(Car car) {
                Intent intent = new Intent(getActivity(), AddEditCarActivity.class);
                intent.putExtra("car", car);
                startActivity(intent);
            }

            @Override
            public void onDelete(Car car) {
                List<String> imageUrls = new ArrayList<>();
                if (car.getColorImages() != null) {
                    for (List<String> urls : car.getColorImages().values()) {
                        imageUrls.addAll(urls);
                    }
                }
                CarHelper.getInstance().deleteCar(car.getId(), imageUrls, new CarHelper.CarCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        loadCarsByMake(spinnerCarMake.getSelectedItem().toString());
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
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


        recyclerView.setAdapter(adapter);
        loadCarMakes();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCarMakes(); // Tải lại hãng xe mỗi khi quay lại
    }

    private void loadCarMakes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("car_makes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    carMakes.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null) {
                            carMakes.add(name);
                        }
                    }
                    if (getContext() != null) {
                        carMakeAdapter.notifyDataSetChanged();
                        if (!carMakes.isEmpty()) {
                            spinnerCarMake.setSelection(0);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi tải hãng xe: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadCarsByMake(String make) {
        if (getContext() == null) return;
        CarHelper.getInstance().getAllCars(new CarHelper.CarsListCallback() {
            @Override
            public void onSuccess(List<Car> cars) {
                carList.clear();
                for (Car car : cars) {
                    if (make.equalsIgnoreCase(car.getMake())) {
                        carList.add(car);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi tải danh sách: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
