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
    private final boolean isAdmin;
    private final String currentUserId;

    public interface OnCarActionListener {
        void onEdit(Car car);
        void onDelete(Car car);
    }

    public CarAdapter(Context context, List<Car> carList, boolean isAdmin, String currentUserId, OnCarActionListener listener) {
        this.context = context;
        this.carList = carList;
        this.listener = listener;
        this.isAdmin = isAdmin;
        this.currentUserId = currentUserId;
        setHasStableIds(true); // Tối ưu hiệu suất nếu có thể
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng LayoutInflater từ parent.getContext() để tránh memory leak
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

        // Dùng Glide/Picasso nếu có, nếu không fallback ImageUtils (nên dùng thư viện ảnh cho mượt)
        if (car.getImageUrls() != null && !car.getImageUrls().isEmpty()) {
            ImageUtils.displayImage(holder.imgCar.getContext(), car.getImageUrls().get(0), holder.imgCar);
        } else {
            holder.imgCar.setImageResource(android.R.drawable.ic_dialog_alert);
        }

        // Tránh leak memory/lỗi double click bằng setOnClickListener mới mỗi lần bind
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(car);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(car);
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    // Nếu có trường id duy nhất, override getItemId để RecyclerView tối ưu
    @Override
    public long getItemId(int position) {
        Car car = carList.get(position);
        // Nếu Car có id (String), hash nó. Nếu không, fallback về position.
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