package com.example.carsale;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.carsale.Model.Payment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.NumberFormat;
import java.util.Random;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class PaymentActivity extends AppCompatActivity {
    private TextView tvDeposit, tvContent;
    private ImageView qrImage;
    private Button btnBack, btnPaid;
    private String carId, userId, randomContent;
    private double deposit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvDeposit = findViewById(R.id.tvDeposit);
        tvContent = findViewById(R.id.tvContent);
        qrImage = findViewById(R.id.qrImage);
        btnBack = findViewById(R.id.btnBack);
        btnPaid = findViewById(R.id.btnPaid);

        carId = getIntent().getStringExtra("carId");
        deposit = getIntent().getDoubleExtra("deposit", 0);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvDeposit.setText(NumberFormat.getInstance().format(deposit) + " VNĐ");
        randomContent = "CARSALE-" + (100000 + new Random().nextInt(900000));
        tvContent.setText(randomContent);

        // Tạo QR banking MB Bank động
        String accountNumber = "7994552939";
        String bankCode = "970422";
        String qrUrl = "https://img.vietqr.io/image/" + bankCode + "-" + accountNumber + "-compact2.png"
                + "?amount=" + ((int) deposit)
                + "&addInfo=" + randomContent;

        Glide.with(this)
            .load(qrUrl)
            .into(qrImage);

        btnBack.setOnClickListener(v -> finish());

        btnPaid.setOnClickListener(v -> {
            savePaymentToFirestore();
        });
    }

    private void checkPaymentStatusFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("payments")
          .whereEqualTo("content", randomContent)
          .whereEqualTo("carId", carId)
          .whereEqualTo("userId", userId)
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              if (!queryDocumentSnapshots.isEmpty()) {
                  updateCarQuantityFirestore();
              } else {
                  android.widget.Toast.makeText(PaymentActivity.this, "Chưa xác nhận được thanh toán! Vui lòng kiểm tra lại.", android.widget.Toast.LENGTH_LONG).show();
              }
          })
          .addOnFailureListener(e -> {
              android.widget.Toast.makeText(PaymentActivity.this, "Lỗi kiểm tra thanh toán!", android.widget.Toast.LENGTH_SHORT).show();
          });
    }

    private void savePaymentToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String paymentId = db.collection("payments").document().getId();
        Payment payment = new Payment(paymentId, userId, carId, deposit, System.currentTimeMillis(), randomContent, false);
        db.collection("payments").document(paymentId).set(payment)
            .addOnSuccessListener(aVoid -> {
                updateCarQuantityFirestore();
            });
    }

    private void updateCarQuantityFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cars").document(carId).get()
            .addOnSuccessListener(documentSnapshot -> {
                Long quantityLong = documentSnapshot.getLong("quantity");
                int quantity = quantityLong != null ? quantityLong.intValue() : 0;
                if (quantity > 0) {
                    db.collection("cars").document(carId).update("quantity", quantity - 1);
                }
                Intent intent = new Intent(PaymentActivity.this, PaymentSuccessActivity.class);
                startActivity(intent);
                finish();
            });
    }
} 