package com.example.carsale.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.Model.Car;
import com.example.carsale.R;
import com.example.carsale.ImageUtils;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private final Context context;
    private final List<Car> carList;
    private final OnCarActionListener listener;

    public interface OnCarActionListener {
        void onEdit(Car car);
        void onDelete(Car car);
    }

    public CarAdapter(Context context, List<Car> carList, OnCarActionListener listener) {
        this.context = context;
        this.carList = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.txtCarName.setText(car.getMake() + " " + car.getModel());
        holder.txtCarPrice.setText(String.format("%,.0f USD", car.getPrice()));
        holder.txtFuelType.setText(car.getFuelType());
        holder.txtTransmission.setText(car.getTransmission());

        // Hiển thị ảnh cục bộ
        if (car.getImageUrls() != null && !car.getImageUrls().isEmpty()) {
            ImageUtils.displayImage(context, car.getImageUrls().get(0), holder.imgCar);
        } else {
            holder.imgCar.setImageResource(android.R.drawable.ic_dialog_alert); // Hình mặc định khi lỗi
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(car));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(car));
    }

    @Override
    public int getItemCount() {
        return carList.size();
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