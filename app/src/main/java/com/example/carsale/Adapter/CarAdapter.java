package com.example.carsale.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.CloudinaryManager;
import com.example.carsale.Database.SalesHelper;
import com.example.carsale.Model.Car;
import com.example.carsale.R;

import java.util.List;
import java.util.ArrayList;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private final Context context;
    private final List<Car> carList;
    private final OnCarActionListener listener;
    private final boolean isAdmin;
    private final String currentUserId;

    public interface OnCarActionListener {
        void onEdit(Car car);
        void onDelete(Car car);
        void onCarClick(Car car);
    }

    public CarAdapter(Context context, List<Car> carList, boolean isAdmin, String currentUserId, OnCarActionListener listener) {
        this.context = context;
        this.carList = carList;
        this.listener = listener;
        this.isAdmin = isAdmin;
        this.currentUserId = currentUserId;
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return isAdmin ? 1 : 0;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == 1) ? R.layout.item_car_admin : R.layout.item_car;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        final Car car = carList.get(position);

        // Bind basic information
        holder.txtCarName.setText(car.getMake() + " " + car.getModel());
        holder.txtCarPrice.setText(String.format("Giá: %,.0f VNĐ", car.getPrice()));

        // Bind fuel type and transmission only if views are visible (for full layout)
        if (holder.txtFuelType != null && holder.txtFuelType.getVisibility() == View.VISIBLE) {
            holder.txtFuelType.setText(car.getFuelType());
        }
        if (holder.txtTransmission != null && holder.txtTransmission.getVisibility() == View.VISIBLE) {
            holder.txtTransmission.setText(car.getTransmission());
        }

        // Handle edit/delete button visibility
        boolean canEdit = isAdmin || (car.getUserId() != null && car.getUserId().equals(currentUserId));
        if (holder.btnEdit != null) {
            holder.btnEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        }
        if (holder.btnDelete != null) {
            holder.btnDelete.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        }

        // Load image from Cloudinary
        if (car.getColorImages() != null && !car.getColorImages().isEmpty()) {
            String firstImageUrl = null;
            for (List<String> urls : car.getColorImages().values()) {
                if (urls != null && !urls.isEmpty()) {
                    firstImageUrl = urls.get(0);
                    break;
                }
            }
            if (firstImageUrl != null) {
                CloudinaryManager.displayImage(context, firstImageUrl, holder.imgCar);
            } else {
                holder.imgCar.setImageResource(android.R.drawable.ic_dialog_alert);
            }
        } else {
            holder.imgCar.setImageResource(android.R.drawable.ic_dialog_alert);
        }

        // Set click listeners
        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(v -> {
                // Kiểm tra xe có đang được đặt cọc hay không trước khi sửa
                SalesHelper.getInstance().isCarReserved(car.getId(), new SalesHelper.SaleCallback() {
                    @Override
                    public void onSuccess(String message) {
                        // Xe không được đặt cọc, có thể sửa
                        if (listener != null) listener.onEdit(car);
                    }

                    @Override
                    public void onError(String error) {
                        // Xe đã được đặt cọc, không thể sửa
                        Toast.makeText(context, "Xe đã được đặt cọc, không thể sửa!", Toast.LENGTH_LONG).show();
                    }
                });
            });
        }

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(car);
            });
        }

        // Item click listener for opening detail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCarClick(car);
        });

        // Image click listener
        if (holder.imgCar != null) {
            holder.imgCar.setOnClickListener(v -> {
                Log.d("CarAdapter", "imgCar clicked, carId=" + (car != null ? car.getId() : "null"));
                if (listener != null) listener.onCarClick(car);
            });
        }
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    @Override
    public long getItemId(int position) {
        Car car = carList.get(position);
        return car.getId() != null ? car.getId().hashCode() : position;
    }

    // Method to update the entire list
    public void updateCarList(List<Car> newCarList) {
        this.carList.clear();
        this.carList.addAll(newCarList);
        notifyDataSetChanged();
    }

    // Method to add a single car
    public void addCar(Car car) {
        this.carList.add(car);
        notifyItemInserted(carList.size() - 1);
    }

    // Method to remove a car
    public void removeCar(int position) {
        if (position >= 0 && position < carList.size()) {
            this.carList.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Method to update a single car
    public void updateCar(int position, Car updatedCar) {
        if (position >= 0 && position < carList.size()) {
            this.carList.set(position, updatedCar);
            notifyItemChanged(position);
        }
    }

    // Method to find car position by ID
    public int findCarPosition(String carId) {
        for (int i = 0; i < carList.size(); i++) {
            if (carList.get(i).getId() != null && carList.get(i).getId().equals(carId)) {
                return i;
            }
        }
        return -1;
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCar;
        TextView txtCarName, txtCarPrice, txtFuelType, txtTransmission;
        ImageButton btnEdit, btnDelete;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCar = itemView.findViewById(R.id.imgCar);
            txtCarName = itemView.findViewById(R.id.txtCarName);
            txtCarPrice = itemView.findViewById(R.id.txtCarPrice);
            txtFuelType = itemView.findViewById(R.id.txtFuelType);
            txtTransmission = itemView.findViewById(R.id.txtTransmission);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}