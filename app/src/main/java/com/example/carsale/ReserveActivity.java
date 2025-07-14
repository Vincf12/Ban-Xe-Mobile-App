package com.example.carsale;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.carsale.Model.Car;
import com.example.carsale.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.NumberFormat;
import com.google.android.material.button.MaterialButton;

public class ReserveActivity extends AppCompatActivity {
    private TextView carNameTextView, carPriceTextView, depositTextView;
    private TextView userNameTextView, userPhoneTextView, userEmailTextView;
    private TextView userAddressTextView, userAgeTextView, userGenderTextView;
    private TextView BHTextView;
    private TextView CCCDTextView;
    private MaterialButton btnPayDeposit;
    private String carWarrantyInfo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);

        carNameTextView = findViewById(R.id.carNameTextView);
        carPriceTextView = findViewById(R.id.carPriceTextView);
        depositTextView = findViewById(R.id.depositTextView);
        userNameTextView = findViewById(R.id.userNameTextView);
        userPhoneTextView = findViewById(R.id.userPhoneTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        userAddressTextView = findViewById(R.id.userAddressTextView);
        userAgeTextView = findViewById(R.id.userAgeTextView);
        userGenderTextView = findViewById(R.id.userGenderTextView);
        BHTextView = findViewById(R.id.BHTextView);
        CCCDTextView = findViewById(R.id.CCCDTextView);

        btnPayDeposit = findViewById(R.id.btnPayDeposit);
        android.widget.ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        String carId = getIntent().getStringExtra("carId");
        if (carId != null) {
            loadCarInfo(carId);
        }
        loadUserInfo();

        btnPayDeposit.setOnClickListener(v -> {
            // Lấy deposit từ TextView (nếu đã load)
            double deposit = 0;
            try {
                String depositStr = depositTextView.getText().toString().replaceAll("[^0-9]", "");
                if (!depositStr.isEmpty()) deposit = Double.parseDouble(depositStr);
            } catch (Exception ignored) {}
            android.content.Intent intent = new android.content.Intent(ReserveActivity.this, PaymentActivity.class);
            intent.putExtra("carId", carId);
            intent.putExtra("deposit", deposit);
            // Lưu thông tin bảo hành vào Sale
            createSaleWithWarranty(carId, deposit, carWarrantyInfo);
            startActivity(intent);
        });
    }

    private void loadCarInfo(String carId) {
        com.example.carsale.Database.CarHelper.getInstance().getCarById(carId, new com.example.carsale.Database.CarHelper.CarDetailCallback() {
            @Override
            public void onSuccess(Car car) {
                if (car != null) {
                    carNameTextView.setText(car.getMake() + " " + car.getModel());
                    carPriceTextView.setText(NumberFormat.getInstance().format(car.getPrice()) + " VNĐ");
                    depositTextView.setText(NumberFormat.getInstance().format(car.getDepositPrice()) + " VNĐ");
                    BHTextView.setText(car.getEtBH() != null ? car.getEtBH() : "-");
                    carWarrantyInfo = car.getEtBH() != null ? car.getEtBH() : "";
                }
            }
            @Override
            public void onError(String error) {
                android.widget.Toast.makeText(ReserveActivity.this, "Lỗi: " + error, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserInfo() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            com.example.carsale.Database.FirebaseHelper.getInstance().getUserById(userId, new com.example.carsale.Database.FirebaseHelper.OnUserDataListener() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        userNameTextView.setText(user.getFullname());
                        userPhoneTextView.setText(user.getPhone());
                        userEmailTextView.setText(user.getEmail());
                        userAddressTextView.setText(user.getAddress());
                        userAgeTextView.setText(String.valueOf(user.getAge()));
                        userGenderTextView.setText(user.getGender());
                        CCCDTextView.setText(user.getCccd() > 0 ? String.valueOf(user.getCccd()) : "-");
                    }
                }
                @Override
                public void onFailure(String error) {
                    android.widget.Toast.makeText(ReserveActivity.this, "Lỗi: " + error, android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void createSaleWithWarranty(String carId, double deposit, String warrantyInfo) {
        // Lấy thông tin user hiện tại
        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;
        String buyerId = firebaseUser.getUid();
        // Giả sử sellerId là admin hoặc lấy từ car
        String sellerId = "admin";
        com.example.carsale.Model.Sale sale = new com.example.carsale.Model.Sale();
        sale.setCarId(carId);
        sale.setBuyerId(buyerId);
        sale.setSellerId(sellerId);
        sale.setSalePrice(deposit);
        sale.setWarrantyInfo(warrantyInfo);
        com.example.carsale.Database.SalesHelper.getInstance().createSale(sale, new com.example.carsale.Database.SalesHelper.SaleCallback() {
            @Override
            public void onSuccess(String message) {
                // Có thể show thông báo thành công nếu muốn
            }
            @Override
            public void onError(String error) {
                // Có thể show thông báo lỗi nếu muốn
            }
        });
    }
} 