package com.example.carsale.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.Model.ServiceBooking;
import com.example.carsale.R;
import com.example.carsale.ServiceManagementActivity;

import java.util.List;
import java.util.Map;

public class ServiceBookingAdapter extends RecyclerView.Adapter<ServiceBookingAdapter.ViewHolder> {
    private List<ServiceBooking> bookings;
    private OnBookingActionListener actionListener;

    public interface OnBookingActionListener {
        void onBookingAction(ServiceBooking booking, String action);
    }

    public ServiceBookingAdapter(List<ServiceBooking> bookings, OnBookingActionListener listener) {
        this.bookings = bookings;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceBooking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void updateBookings(List<ServiceBooking> newBookings) {
        try {
            this.bookings = newBookings;
            notifyDataSetChanged();
        } catch (Exception e) {
            System.err.println("Error updating bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatUserInfo(Map<String, Object> userInfo) {
        if (userInfo == null) {
            return "Người dùng";
        }
        
        String username = (String) userInfo.get("username");
        String email = (String) userInfo.get("email");
        String phone = (String) userInfo.get("phone");
        
        StringBuilder sb = new StringBuilder();
        if (username != null && !username.isEmpty()) {
            sb.append(username);
        }
        if (email != null && !email.isEmpty()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(email);
        }
        if (phone != null && !phone.isEmpty()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(phone);
        }
        
        return sb.length() > 0 ? sb.toString() : "Người dùng";
    }

    private String formatAppointmentDate(String dateString) {
        return ServiceManagementActivity.formatDateFromString(dateString);
    }





    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBookingId, tvStatus, tvCarInfo, tvServiceType, tvDateTime, tvUserInfo, tvDescription;
        private TextView btnReject, btnConfirm;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tv_booking_id);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCarInfo = itemView.findViewById(R.id.tv_car_info);
            tvServiceType = itemView.findViewById(R.id.tv_service_type);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvUserInfo = itemView.findViewById(R.id.tv_user_info);
            tvDescription = itemView.findViewById(R.id.tv_description);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnConfirm = itemView.findViewById(R.id.btn_confirm);
        }

        public void bind(ServiceBooking booking) {
            try {
                tvBookingId.setText("Đặt lịch #" + booking.getId().substring(0, 8));
                tvStatus.setText(ServiceManagementActivity.getStatusText(booking.getStatus()));
                tvCarInfo.setText(booking.getCarInfo());
                tvServiceType.setText(booking.getServiceType());
                
                String dateTime = formatAppointmentDate(booking.getAppointmentDate()) + 
                                " - " + booking.getAppointmentTime();
                tvDateTime.setText(dateTime);
                
                tvUserInfo.setText(formatUserInfo(booking.getUserInfo()));
                tvDescription.setText("Mô tả: " + booking.getDescription());

                // Set status background
                switch (booking.getStatus()) {
                    case "pending":
                        tvStatus.setBackground(itemView.getContext().getDrawable(R.drawable.bg_status_pending));
                        break;
                    case "confirmed":
                        tvStatus.setBackground(itemView.getContext().getDrawable(R.drawable.bg_status_available));
                        break;
                    case "completed":
                        tvStatus.setBackground(itemView.getContext().getDrawable(R.drawable.bg_status_sold));
                        break;
                    case "rejected":
                        tvStatus.setBackground(itemView.getContext().getDrawable(R.drawable.bg_status_reserved));
                        break;
                }

                // Show/hide action buttons based on status
                switch (booking.getStatus()) {
                    case "pending":
                        btnReject.setVisibility(View.VISIBLE);
                        btnConfirm.setVisibility(View.VISIBLE);
                        btnConfirm.setText("Xác nhận");
                        break;
                    case "confirmed":
                        btnReject.setVisibility(View.GONE);
                        btnConfirm.setVisibility(View.VISIBLE);
                        btnConfirm.setText("Hoàn thành");
                        break;
                    case "completed":
                    case "rejected":
                        btnReject.setVisibility(View.GONE);
                        btnConfirm.setVisibility(View.GONE);
                        break;
                }

                // Set click listeners
                btnReject.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onBookingAction(booking, "reject");
                    }
                });

                btnConfirm.setOnClickListener(v -> {
                    if (actionListener != null) {
                        if ("pending".equals(booking.getStatus())) {
                            actionListener.onBookingAction(booking, "confirm");
                        } else if ("confirmed".equals(booking.getStatus())) {
                            actionListener.onBookingAction(booking, "complete");
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Error binding booking data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 