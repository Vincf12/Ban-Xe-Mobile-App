package com.example.carsale.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.CloudinaryManager;
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

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        final Car car = carList.get(position);

        holder.txtCarName.setText(car.getMake() + " " + car.getModel());
        holder.txtCarPrice.setText(String.format("%,.0f USD", car.getPrice()));
        holder.txtFuelType.setText(car.getEngineCapacity());
        holder.txtTransmission.setText(car.getTransmission());

        boolean canEdit = isAdmin || (car.getUserId() != null && car.getUserId().equals(currentUserId));
        holder.btnEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(canEdit ? View.VISIBLE : View.GONE);

        // Load ảnh từ Cloudinary
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

        // Listener cho nút edit
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(car);
        });

        // Listener cho nút delete
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(car);
        });

        // Listener cho toàn bộ item view để mở detail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCarClick(car);
        });

        holder.imgCar.setOnClickListener(v -> {
            Log.d("CarAdapter", "imgCar clicked, carId=" + (car != null ? car.getId() : "null"));
            listener.onCarClick(car);
        });
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

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCar;
        TextView txtCarName, txtCarPrice, txtFuelType, txtTransmission;
        Button btnEdit;
        ImageButton btnDelete;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCar = itemView.findViewById(R.id.imgCar);
            txtCarName = itemView.findViewById(R.id.txtCarName);
            txtCarPrice = itemView.findViewById(R.id.txtCarPrice);
            txtFuelType = itemView.findViewById(R.id.txtFuel);
            txtTransmission = itemView.findViewById(R.id.txtTransmission);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btDelete);
        }
    }
}
