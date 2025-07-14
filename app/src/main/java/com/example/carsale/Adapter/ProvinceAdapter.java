package com.example.carsale.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.Model.Province;
import com.example.carsale.R;

import java.util.ArrayList;
import java.util.List;

public class ProvinceAdapter extends RecyclerView.Adapter<ProvinceAdapter.ProvinceViewHolder> {
    private Context context;
    private List<Province> allProvinces;
    private List<Province> filteredProvinces;
    private OnProvinceClickListener listener;
    private int selectedPosition = -1;

    public interface OnProvinceClickListener {
        void onProvinceClick(Province province);
    }

    public ProvinceAdapter(Context context, List<Province> provinces, OnProvinceClickListener listener) {
        this.context = context;
        this.allProvinces = provinces;
        this.filteredProvinces = new ArrayList<>(provinces);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProvinceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_province, parent, false);
        return new ProvinceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProvinceViewHolder holder, int position) {
        Province province = filteredProvinces.get(position);
        holder.tvProvinceName.setText(province.getName());
        
        // Highlight selected item
        if (position == selectedPosition) {
            holder.ivSelected.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
        } else {
            holder.ivSelected.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            // Update previous selected item
            if (previousSelected != -1 && previousSelected < filteredProvinces.size()) {
                notifyItemChanged(previousSelected);
            }
            
            // Update current selected item
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onProvinceClick(province);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredProvinces.size();
    }

    public void filterProvinces(String query) {
        filteredProvinces.clear();
        if (query.isEmpty()) {
            filteredProvinces.addAll(allProvinces);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Province province : allProvinces) {
                if (province.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredProvinces.add(province);
                }
            }
        }
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public Province getSelectedProvince() {
        if (selectedPosition >= 0 && selectedPosition < filteredProvinces.size()) {
            return filteredProvinces.get(selectedPosition);
        }
        return null;
    }

    public void setSelectedProvince(Province province) {
        for (int i = 0; i < filteredProvinces.size(); i++) {
            if (filteredProvinces.get(i).getCode() == province.getCode()) {
                selectedPosition = i;
                notifyDataSetChanged();
                break;
            }
        }
    }

    static class ProvinceViewHolder extends RecyclerView.ViewHolder {
        TextView tvProvinceName;
        ImageView ivSelected;

        public ProvinceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProvinceName = itemView.findViewById(R.id.tv_province_name);
            ivSelected = itemView.findViewById(R.id.iv_selected);
        }
    }
} 