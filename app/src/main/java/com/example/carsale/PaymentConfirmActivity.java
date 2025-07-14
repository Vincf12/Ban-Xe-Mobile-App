package com.example.carsale;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.carsale.Model.Payment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;

public class PaymentConfirmActivity extends AppCompatActivity {
    private LinearLayout layoutPayments;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_confirm);
        layoutPayments = findViewById(R.id.layoutPayments);
        db = FirebaseFirestore.getInstance();
        loadPayments();
    }

    private void loadPayments() {
        layoutPayments.removeAllViews();
        db.collection("payments")
                .whereEqualTo("confirmed", false)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        Payment payment = doc.toObject(Payment.class);
                        addPaymentView(payment, doc.getId());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải danh sách thanh toán", Toast.LENGTH_SHORT).show());
    }

    private void addPaymentView(Payment payment, String paymentId) {
        View view = getLayoutInflater().inflate(R.layout.item_payment_confirm, layoutPayments, false);
        TextView tvUser = view.findViewById(R.id.tvUser);
        TextView tvCar = view.findViewById(R.id.tvCar);
        TextView tvAmount = view.findViewById(R.id.tvAmount);
        TextView tvContent = view.findViewById(R.id.tvContent);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        tvUser.setText("User: " + payment.getUserId());
        tvCar.setText("Car: " + payment.getCarId());
        tvAmount.setText("Số tiền: " + NumberFormat.getInstance().format(payment.getAmount()) + " VNĐ");
        tvContent.setText("Nội dung: " + payment.getContent());

        btnConfirm.setOnClickListener(v -> confirmPayment(paymentId));
        layoutPayments.addView(view);
    }

    private void confirmPayment(String paymentId) {
        db.collection("payments").document(paymentId)
                .update("confirmed", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xác nhận thanh toán!", Toast.LENGTH_SHORT).show();
                    loadPayments();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi xác nhận!", Toast.LENGTH_SHORT).show());
    }
} 