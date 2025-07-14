package com.example.carsale.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carsale.CloudinaryManager;
import com.example.carsale.Model.CarMake;
import com.example.carsale.R;

import java.util.List;

public class CarMakeAdapter extends RecyclerView.Adapter<CarMakeAdapter.ViewHolder> {
    private final List<CarMake> carMakeList;
    private final OnCarMakeClickListener listener;
    private final Context context;

    public interface OnCarMakeClickListener {
        void onCarMakeClick(CarMake carMake);
    }

    public CarMakeAdapter(Context context, List<CarMake> carMakeList, OnCarMakeClickListener listener) {
        this.context = context;
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

        // Nếu carMake.getLogoPath() đã là URL đầy đủ thì dùng luôn
        // Nếu chỉ là public_id thì build URL Cloudinary
        String logoPath = carMake.getLogoPath();
        String imageUrl;

        if (logoPath != null && logoPath.startsWith("http")) {
            imageUrl = logoPath;
        } else {
            // Build URL Cloudinary
            String cloudName = "dk9j2tsbq"; // Giống với CLOUD_NAME trong CloudinaryManager
            imageUrl = "https://res.cloudinary.com/" + cloudName + "/image/upload/" + logoPath + ".jpg";
        }

        Log.d("CarMakeAdapter", "Image URL: " + imageUrl);

        CloudinaryManager.displayImage(context, imageUrl, holder.logoImageView);

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