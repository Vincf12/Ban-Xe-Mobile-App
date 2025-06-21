package com.example.carsale.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.Model.Car;
import com.example.carsale.R;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private List<Car> carList;
    private boolean isAdmin;

    public CarAdapter(List<Car> carList, boolean isAdmin) {
        this.carList = carList;
        this.isAdmin = isAdmin;
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView txtMakeModel, txtPrice;
        LinearLayout adminButtons;
        Button btnEdit, btnDelete;

        public CarViewHolder(View itemView) {
            super(itemView);
            txtMakeModel = itemView.findViewById(R.id.txtMakeModel);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            adminButtons = itemView.findViewById(R.id.adminButtons);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.car_item, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.txtMakeModel.setText(car.getMake() + " " + car.getModel());
        holder.txtPrice.setText("Giá: " + car.getPrice());

        if (isAdmin) {
            holder.adminButtons.setVisibility(View.VISIBLE);
            // gắn sự kiện btnEdit, btnDelete
        }
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }
}

