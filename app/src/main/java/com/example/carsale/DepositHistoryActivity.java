package com.example.carsale;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.carsale.Model.Payment;
import com.example.carsale.Model.Car;
import com.example.carsale.Model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DepositHistoryActivity extends AppCompatActivity {
    private LinearLayout layoutDepositHistory;
    private FirebaseFirestore db;
    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_history);
        
        // Initialize views
        layoutDepositHistory = findViewById(R.id.layoutDepositHistory);
        btnBack = findViewById(R.id.btnBack);
        
        // Setup back button
        btnBack.setOnClickListener(v -> finish());
        
        db = FirebaseFirestore.getInstance();
        loadDepositHistory();
    }

    private void loadDepositHistory() {
        layoutDepositHistory.removeAllViews();
        db.collection("payments")
                .whereEqualTo("confirmed", true)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        Payment payment = doc.toObject(Payment.class);
                        loadCarAndUser(payment);
                    }
                });
    }

    private void loadCarAndUser(Payment payment) {
        db.collection("cars").document(payment.getCarId()).get()
                .addOnSuccessListener(carDoc -> {
                    Car car = carDoc.toObject(Car.class);
                    db.collection("users").document(payment.getUserId()).get()
                            .addOnSuccessListener(userDoc -> {
                                User user = userDoc.toObject(User.class);
                                addDepositHistoryView(payment, car, user);
                            });
                });
    }

    private void addDepositHistoryView(Payment payment, Car car, User user) {
        View view = getLayoutInflater().inflate(R.layout.item_deposit_history, layoutDepositHistory, false);
        TextView tvCarName = view.findViewById(R.id.tvCarName);
        TextView tvCarMake = view.findViewById(R.id.tvCarMake);
        TextView tvDepositAmount = view.findViewById(R.id.tvDepositAmount);
        TextView tvDepositDate = view.findViewById(R.id.tvDepositDate);
        TextView tvDepositTime = view.findViewById(R.id.tvDepositTime);
        TextView tvUserInfo = view.findViewById(R.id.tvUserInfo);
        TextView tvDepositStatus = view.findViewById(R.id.tvDepositStatus);

        tvCarName.setText("Tên xe: " + (car != null ? car.getModel() : ""));
        tvCarMake.setText("Hãng xe: " + (car != null ? car.getMake() : ""));
        tvDepositAmount.setText("Giá cọc: " + payment.getAmount() + " VNĐ");
        tvDepositStatus.setText("Trạng thái: Đã đặt cọc");
        String dateStr = "-", timeStr = "-";
        if (payment.getTimestamp() > 0) {
            Date date = new Date(payment.getTimestamp());
            dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
            timeStr = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date);
        }
        tvDepositDate.setText("Ngày cọc: " + dateStr);
        tvDepositTime.setText("Thời gian: " + timeStr);
        tvUserInfo.setText("Người đặt: " + (user != null ? user.getFullname() + " (" + user.getEmail() + ")" : ""));

        layoutDepositHistory.addView(view);
    }
} 