package com.example.carsale.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carsale.Model.CarMake;
import com.example.carsale.R;

import java.util.List;

public class CarMakeAdapter extends RecyclerView.Adapter<CarMakeAdapter.ViewHolder> {
    private final List<CarMake> carMakeList;
    private final OnCarMakeClickListener listener;

    public interface OnCarMakeClickListener {
        void onCarMakeClick(CarMake carMake);
    }

    public CarMakeAdapter(Context context, List<CarMake> carMakeList, OnCarMakeClickListener listener) {
        this.carMakeList = carMakeList;
        this.listener = listener;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Luôn lấy context từ parent để tránh memory leak
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car_make, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarMake carMake = carMakeList.get(position);
        // Dùng Glide với context của imageView để tránh rò rỉ bộ nhớ
        Glide.with(holder.logoImageView.getContext())
                .load(carMake.getLogoPath())
                .placeholder(R.drawable.ic_upload_24) // placeholder khi loading
                .error(R.drawable.ic_launcher_background)      // icon khi ảnh lỗi
                .into(holder.logoImageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCarMakeClick(carMake);
        });
    }

    @Override
    public int getItemCount() {
        return carMakeList.size();
    }

    @Override
    public long getItemId(int position) {
        // Nếu CarMake có trường id duy nhất, hash nó để tối ưu
        CarMake carMake = carMakeList.get(position);
        return carMake.getId() != null ? carMake.getId().hashCode() : position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView logoImageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            logoImageView = itemView.findViewById(R.id.logoImageView);
        }
    }
}