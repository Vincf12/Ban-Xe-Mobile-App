package com.example.carsale;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.Adapter.ServiceBookingAdapter;
import com.example.carsale.Database.ServiceBookingHelper;
import com.example.carsale.Model.ServiceBooking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ServiceManagementActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tabPending, tabConfirmed, tabCompleted;
    private RecyclerView rvServiceBookings;
    private LinearLayout llEmptyState;
    
    private ServiceBookingHelper serviceBookingHelper;
    private ServiceBookingAdapter adapter;
    private List<ServiceBooking> allBookings = new ArrayList<>();
    private String currentStatus = "pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_management);
        
        initViews();
        initData();
        setupListeners();
        loadBookings("pending");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tabPending = findViewById(R.id.tab_pending);
        tabConfirmed = findViewById(R.id.tab_confirmed);
        tabCompleted = findViewById(R.id.tab_completed);
        rvServiceBookings = findViewById(R.id.rv_service_bookings);
        llEmptyState = findViewById(R.id.ll_empty_state);
    }

    private void initData() {
        try {
            serviceBookingHelper = ServiceBookingHelper.getInstance();
            adapter = new ServiceBookingAdapter(new ArrayList<>(), this::onBookingAction);
            rvServiceBookings.setLayoutManager(new LinearLayoutManager(this));
            rvServiceBookings.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo adapter: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        tabPending.setOnClickListener(v -> {
            currentStatus = "pending";
            updateTabSelection();
            loadBookings("pending");
        });
        
        tabConfirmed.setOnClickListener(v -> {
            currentStatus = "confirmed";
            updateTabSelection();
            loadBookings("confirmed");
        });
        
        tabCompleted.setOnClickListener(v -> {
            currentStatus = "completed";
            updateTabSelection();
            loadBookings("completed");
        });
    }

    private void updateTabSelection() {
        // Reset all tabs
        tabPending.setBackground(getDrawable(R.drawable.tab_unselected));
        tabPending.setTextColor(getResources().getColor(R.color.text_secondary));
        tabConfirmed.setBackground(getDrawable(R.drawable.tab_unselected));
        tabConfirmed.setTextColor(getResources().getColor(R.color.text_secondary));
        tabCompleted.setBackground(getDrawable(R.drawable.tab_unselected));
        tabCompleted.setTextColor(getResources().getColor(R.color.text_secondary));
        
        // Highlight selected tab
        switch (currentStatus) {
            case "pending":
                tabPending.setBackground(getDrawable(R.drawable.tab_selected));
                tabPending.setTextColor(getResources().getColor(R.color.primary_color));
                break;
            case "confirmed":
                tabConfirmed.setBackground(getDrawable(R.drawable.tab_selected));
                tabConfirmed.setTextColor(getResources().getColor(R.color.primary_color));
                break;
            case "completed":
                tabCompleted.setBackground(getDrawable(R.drawable.tab_selected));
                tabCompleted.setTextColor(getResources().getColor(R.color.primary_color));
                break;
        }
    }

    private void loadBookings(String status) {
        try {
            Toast.makeText(this, "Đang tải dữ liệu cho trạng thái: " + status, Toast.LENGTH_SHORT).show();
            
            serviceBookingHelper.getServiceBookingsByStatus(status, new ServiceBookingHelper.ServiceBookingsListCallback() {
                @Override
                public void onSuccess(List<ServiceBooking> bookings) {
                    runOnUiThread(() -> {
                        try {
                            Toast.makeText(ServiceManagementActivity.this, 
                                "Tải thành công: " + bookings.size() + " đặt lịch", Toast.LENGTH_SHORT).show();
                            
                            allBookings.clear();
                            allBookings.addAll(bookings);
                            
                            if (bookings.isEmpty()) {
                                rvServiceBookings.setVisibility(View.GONE);
                                llEmptyState.setVisibility(View.VISIBLE);
                            } else {
                                rvServiceBookings.setVisibility(View.VISIBLE);
                                llEmptyState.setVisibility(View.GONE);
                                adapter.updateBookings(bookings);
                            }
                        } catch (Exception e) {
                            Toast.makeText(ServiceManagementActivity.this, 
                                "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(ServiceManagementActivity.this, 
                            "Lỗi tải dữ liệu: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void onBookingAction(ServiceBooking booking, String action) {
        String message = "";
        String newStatus = "";
        
        switch (action) {
            case "confirm":
                message = "Xác nhận đặt lịch này?";
                newStatus = "confirmed";
                break;
            case "reject":
                message = "Từ chối đặt lịch này?";
                newStatus = "rejected";
                break;
            case "complete":
                message = "Đánh dấu hoàn thành?";
                newStatus = "completed";
                break;
        }
        
        final String finalNewStatus = newStatus;
        final ServiceBooking finalBooking = booking;
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage(message)
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    updateBookingStatus(finalBooking.getId(), finalNewStatus);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateBookingStatus(String bookingId, String newStatus) {
        serviceBookingHelper.updateServiceBookingStatus(bookingId, newStatus, 
            new ServiceBookingHelper.ServiceBookingCallback() {
                @Override
                public void onSuccess(ServiceBooking booking) {
                    runOnUiThread(() -> {
                        Toast.makeText(ServiceManagementActivity.this, 
                            "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        // Reload current tab
                        loadBookings(currentStatus);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(ServiceManagementActivity.this, 
                            "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatDateFromString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Chưa có ngày";
        }
        
        try {
            // Try to parse as timestamp first
            long timestamp = Long.parseLong(dateString);
            return formatDate(timestamp);
        } catch (NumberFormatException e) {
            // If it's not a timestamp, return as is (assuming it's already formatted)
            return dateString;
        }
    }

    public static String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "Chờ xử lý";
            case "confirmed":
                return "Đã xác nhận";
            case "completed":
                return "Hoàn thành";
            case "rejected":
                return "Đã từ chối";
            default:
                return status;
        }
    }
} 