package com.example.carsale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carsale.Database.FirebaseHelper;
import com.google.android.material.textfield.TextInputEditText;

public class DangKyActivity extends AppCompatActivity {

    private TextInputEditText etUser, etEmail, etPassword, etRtPassword;
    private TextView tvLogin, tvsignup;
    private Button btnSignup;
    private LinearLayout tb_signup, ft_login;
    private FirebaseHelper firebaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_ky);

        initViews();
        firebaseHelper = FirebaseHelper.getInstance();

        btnSignup.setOnClickListener(v -> handleSignup());

        tvLogin = findViewById(R.id.tv_login);
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở màn hình đăng nhập
                Intent intent = new Intent(DangKyActivity.this, DangNhapActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        tvsignup = findViewById(R.id.tvsignup);
        tb_signup = findViewById(R.id.tb_signup);
        ft_login = findViewById(R.id.ft_login);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce_in);
        Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        tvsignup.startAnimation(animation);
        tb_signup.startAnimation(animation1);
        ft_login.startAnimation(animation1);
    }

    private void initViews() {
        etUser = findViewById(R.id.et_user);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etRtPassword = findViewById(R.id.et_Rtpassword);
        btnSignup = findViewById(R.id.btn_signup);
    }

    private void handleSignup() {
        String username = etUser.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String repeatPassword = etRtPassword.getText().toString().trim();

        // Validation
        if (username.isEmpty()) {
            etUser.setError("Vui lòng nhập tên đăng nhập");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (!password.equals(repeatPassword)) {
            etRtPassword.setError("Mật khẩu không khớp");
            return;
        }

        // Đăng ký
        firebaseHelper.registerUser(username, email, password, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(DangKyActivity.this, message, Toast.LENGTH_SHORT).show();
                finish(); // Đóng activity
            }
            @Override
            public void onError(String error) {
                Toast.makeText(DangKyActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}