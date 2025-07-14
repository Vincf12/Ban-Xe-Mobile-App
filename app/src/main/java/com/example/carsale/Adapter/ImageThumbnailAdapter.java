package com.example.carsale.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carsale.R;

import java.util.List;

public class ImageThumbnailAdapter extends RecyclerView.Adapter<ImageThumbnailAdapter.ImageViewHolder> {
    
    private List<String> imageUrls;
    private OnImageClickListener listener;
    private int selectedPosition = 0;
    
    public interface OnImageClickListener {
        void onImageClick(int position);
    }
    
    public ImageThumbnailAdapter(List<String> imageUrls, OnImageClickListener listener) {
        this.imageUrls = imageUrls;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_thumbnail, parent, false);
        return new ImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        
        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_google)
                .error(R.drawable.ic_google)
                .centerCrop()
                .into(holder.imgThumbnail);
        
        // Set selected state
        holder.setSelected(position == selectedPosition);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }
    
    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }
    
    public void updateImages(List<String> images) {
        this.imageUrls = images;
        notifyDataSetChanged();
    }
    
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        View selectedOverlay;
        ImageView selectedIndicator;
        View selectedBorder;
        
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            selectedOverlay = itemView.findViewById(R.id.view_selected_overlay);
            selectedIndicator = itemView.findViewById(R.id.img_selected_indicator);
            selectedBorder = itemView.findViewById(R.id.view_selected_border);
        }
        
        public void setSelected(boolean selected) {
            if (selected) {
                selectedOverlay.setVisibility(View.VISIBLE);
                selectedIndicator.setVisibility(View.VISIBLE);
                selectedBorder.setVisibility(View.VISIBLE);
            } else {
                selectedOverlay.setVisibility(View.GONE);
                selectedIndicator.setVisibility(View.GONE);
                selectedBorder.setVisibility(View.GONE);
            }
        }
    }
} 