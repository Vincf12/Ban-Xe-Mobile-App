package com.example.carsale.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carsale.Model.CarMake;
import com.example.carsale.R;

import java.util.List;

public class CarMakeAdapter extends RecyclerView.Adapter<CarMakeAdapter.ViewHolder> {
    private Context context;
    private List<CarMake> carMakeList;
    private OnCarMakeClickListener listener;

    public interface OnCarMakeClickListener {
        void onCarMakeClick(CarMake carMake);
    }

    public CarMakeAdapter(Context context, List<CarMake> carMakeList, OnCarMakeClickListener listener) {
        this.context = context;
        this.carMakeList = carMakeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car_make, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarMake carMake = carMakeList.get(position);
        Glide.with(context).load(carMake.getLogoPath()).into(holder.logoImageView);

        holder.itemView.setOnClickListener(v -> listener.onCarMakeClick(carMake));
    }

    @Override
    public int getItemCount() {
        return carMakeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView logoImageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            logoImageView = itemView.findViewById(R.id.logoImageView);
        }
    }
}