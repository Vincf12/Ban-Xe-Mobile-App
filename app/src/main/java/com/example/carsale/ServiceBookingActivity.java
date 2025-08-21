package com.example.carsale;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carsale.Database.CarHelper;
import com.example.carsale.Database.ServiceBookingHelper;
import com.example.carsale.Model.Car;
import com.example.carsale.Model.ServiceBooking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ServiceBookingActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private LinearLayout llCarSelector, llServiceSelector, llDateSelector, llTimeSelector;
    private TextView tvSelectedCar, tvSelectedService, tvSelectedDate, tvSelectedTime, btnSubmitBooking;
    private EditText etDescription;
    
    private CarHelper carHelper;
    private ServiceBookingHelper serviceBookingHelper;
    private FirebaseAuth auth;
    
    private List<Car> userCars = new ArrayList<>();
    private String selectedCarId = "";
    private String selectedServiceType = "";
    private long selectedDate = 0;
    private String selectedTime = "";
    
    private final String[] serviceTypes = {
        "Bảo dưỡng định kỳ",
        "Sửa chữa động cơ",
        "Thay dầu nhớt",
        "Sửa chữa phanh",
        "Sửa chữa điện",
        "Thay lốp xe",
        "Sửa chữa điều hòa",
        "Khác"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_booking);
        
        initViews();
        initData();
        setupListeners();
        loadUserCars();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        llCarSelector = findViewById(R.id.ll_car_selector);
        llServiceSelector = findViewById(R.id.ll_service_selector);
        llDateSelector = findViewById(R.id.ll_date_selector);
        llTimeSelector = findViewById(R.id.ll_time_selector);
        tvSelectedCar = findViewById(R.id.tv_selected_car);
        tvSelectedService = findViewById(R.id.tv_selected_service);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tvSelectedTime = findViewById(R.id.tv_selected_time);
        etDescription = findViewById(R.id.et_description);
        btnSubmitBooking = findViewById(R.id.btn_submit_booking);
    }

    private void initData() {
        carHelper = CarHelper.getInstance();
        serviceBookingHelper = ServiceBookingHelper.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        llCarSelector.setOnClickListener(v -> showCarSelectionDialog());
        llServiceSelector.setOnClickListener(v -> showServiceSelectionDialog());
        llDateSelector.setOnClickListener(v -> showDatePicker());
        llTimeSelector.setOnClickListener(v -> showTimePicker());
        
        btnSubmitBooking.setOnClickListener(v -> submitBooking());
    }

    private void loadUserCars() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Load cars owned by current user
            carHelper.getAllCars(new CarHelper.CarsListCallback() {
                @Override
                public void onSuccess(List<Car> cars) {
                    userCars.clear();
                    // Filter cars that belong to current user (you may need to adjust this logic)
                    for (Car car : cars) {
                        // For demo, we'll show all available cars
                        if ("available".equalsIgnoreCase(car.getStatus())) {
                            userCars.add(car);
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ServiceBookingActivity.this, 
                        "Lỗi tải danh sách xe: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showCarSelectionDialog() {
        if (userCars.isEmpty()) {
            Toast.makeText(this, "Không có xe nào để chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] carNames = new String[userCars.size()];
        for (int i = 0; i < userCars.size(); i++) {
            Car car = userCars.get(i);
            carNames[i] = car.getMake() + " " + car.getModel() + " (" + car.getYear() + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn xe")
                .setItems(carNames, (dialog, which) -> {
                    Car selectedCar = userCars.get(which);
                    selectedCarId = selectedCar.getId();
                    tvSelectedCar.setText(carNames[which]);
                    tvSelectedCar.setTextColor(getResources().getColor(R.color.text_primary));
                })
                .show();
    }

    private void showServiceSelectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Chọn loại dịch vụ")
                .setItems(serviceTypes, (dialog, which) -> {
                    selectedServiceType = serviceTypes[which];
                    tvSelectedService.setText(selectedServiceType);
                    tvSelectedService.setTextColor(getResources().getColor(R.color.text_primary));
                })
                .show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDate = selectedCalendar.getTimeInMillis();
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateString = sdf.format(new Date(selectedDate));
                    tvSelectedDate.setText(dateString);
                    tvSelectedDate.setTextColor(getResources().getColor(R.color.text_primary));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    tvSelectedTime.setText(selectedTime);
                    tvSelectedTime.setTextColor(getResources().getColor(R.color.text_primary));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void submitBooking() {
        // Validate inputs
        if (selectedCarId.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn xe", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedServiceType.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn loại dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedDate == 0) {
            Toast.makeText(this, "Vui lòng chọn ngày hẹn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn thời gian hẹn", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etDescription.getText().toString().trim();
        if (description.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mô tả vấn đề", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find selected car info
        String carInfo = "";
        for (Car car : userCars) {
            if (car.getId().equals(selectedCarId)) {
                carInfo = car.getMake() + " " + car.getModel() + " (" + car.getYear() + ")";
                break;
            }
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create service booking
        ServiceBooking booking = new ServiceBooking(
                currentUser.getUid(),
                selectedCarId,
                carInfo,
                selectedServiceType,
                description,
                String.valueOf(selectedDate),
                selectedTime
        );

        // Set user info
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
        userInfo.put("email", currentUser.getEmail());
        userInfo.put("phone", ""); // Can be updated later
        booking.setUserInfo(userInfo);

        // Show loading
        btnSubmitBooking.setText("Đang xử lý...");
        btnSubmitBooking.setEnabled(false);

        serviceBookingHelper.createServiceBooking(booking, new ServiceBookingHelper.ServiceBookingCallback() {
            @Override
            public void onSuccess(ServiceBooking booking) {
                runOnUiThread(() -> {
                    Toast.makeText(ServiceBookingActivity.this, 
                        "Đặt lịch thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ServiceBookingActivity.this, 
                        "Lỗi đặt lịch: " + error, Toast.LENGTH_SHORT).show();
                    btnSubmitBooking.setText("Đặt lịch");
                    btnSubmitBooking.setEnabled(true);
                });
            }
        });
    }
} 