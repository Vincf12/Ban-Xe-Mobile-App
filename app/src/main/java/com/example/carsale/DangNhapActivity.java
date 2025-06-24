package com.example.carsale;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carsale.Database.FirebaseHelper;
import com.example.carsale.Model.User;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class DangNhapActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;

    // UI Components
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister, tvlogin;
    private ImageButton btnGoogleLogin, btnFacebookLogin;
    private LinearLayout tb_login, ft_sign_up;

    // Firebase
    private FirebaseHelper firebaseHelper;
    private CallbackManager callbackManager;

    // Progress Dialog
    private ProgressDialog progressDialog;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Splash Screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        //Animation
        splashScreen.setOnExitAnimationListener(splashViewProvider -> {
            View splash = splashViewProvider.getView();
            splash.animate()
                    .alpha(0f)
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(500)
                    .withEndAction(splashViewProvider::remove)
                    .start();
        });

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_nhap);

        tvlogin = findViewById(R.id.tvlogin);
        tb_login = findViewById(R.id.tb_login);
        ft_sign_up = findViewById(R.id.ft_sign_up);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce_in);
        Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        tvlogin.startAnimation(animation);
        tb_login.startAnimation(animation1);
        ft_sign_up.startAnimation(animation1);


        initViews();
        initFirebase();
        initFacebook();
        setupClickListeners();
        checkIfUserLoggedIn();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvRegister = findViewById(R.id.tv_register);
        btnGoogleLogin = findViewById(R.id.btn_google_login);
        btnFacebookLogin = findViewById(R.id.btn_facebook_login);

        // Setup Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);
    }

    private void initFirebase() {
        firebaseHelper = FirebaseHelper.getInstance();
        firebaseHelper.initializeGoogleSignIn(this);
    }

    private void initFacebook() {
        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        progressDialog.show();
                        firebaseHelper.handleFacebookAccessToken(loginResult.getAccessToken(),
                                new FirebaseHelper.AuthUserCallback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        progressDialog.dismiss();
                                        Toast.makeText(DangNhapActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                                        if (user.isAdmin()) {
                                            // Nếu là admin → sang trang quản trị
                                            startActivity(new Intent(DangNhapActivity.this, AdminFragment.class));
                                        } else {
                                            // Nếu là user thường → sang giao diện chính
                                            navigateToMainActivity();
                                        }
                                    }

                                    @Override
                                    public void onError(String error) {
                                        progressDialog.dismiss();
                                        Toast.makeText(DangNhapActivity.this, error, Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(DangNhapActivity.this, "Đăng nhập Facebook đã hủy", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(DangNhapActivity.this, "Lỗi Facebook: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void setupClickListeners() {
        // Đăng nhập bằng email/password
        btnLogin.setOnClickListener(v -> handleEmailLogin());

        // Quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        // Chuyển đến đăng ký
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(DangNhapActivity.this, DangKyActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Đăng nhập Google
        btnGoogleLogin.setOnClickListener(v -> handleGoogleSignIn());

        // Đăng nhập Facebook
        btnFacebookLogin.setOnClickListener(v -> handleFacebookSignIn());
    }

    private void handleEmailLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        progressDialog.show();

        firebaseHelper.loginWithEmail(email, password, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                progressDialog.dismiss();
                Toast.makeText(DangNhapActivity.this, message, Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Toast.makeText(DangNhapActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email để reset mật khẩu");
            etEmail.requestFocus();
            return;
        }

        progressDialog.setMessage("Đang gửi email...");
        progressDialog.show();

        firebaseHelper.resetPassword(email, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                progressDialog.dismiss();
                Toast.makeText(DangNhapActivity.this, message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Toast.makeText(DangNhapActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleGoogleSignIn() {
        Intent signInIntent = firebaseHelper.getGoogleSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleFacebookSignIn() {
        LoginManager.getInstance().logInWithReadPermissions(this,
                Arrays.asList("email", "public_profile"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle Google Sign In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            progressDialog.show();

            firebaseHelper.handleGoogleSignInResult(task, new FirebaseHelper.AuthUserCallback() {
                @Override
                public void onSuccess(User user) {
                    progressDialog.dismiss();
                    Toast.makeText(DangNhapActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    // Kiểm tra quyền admin
                    if (user.isAdmin()) {
                        startActivity(new Intent(DangNhapActivity.this, AdminFragment.class));
                    } else {
                        navigateToMainActivity();
                    }
                }
                @Override
                public void onError(String error) {
                    progressDialog.dismiss();
                    Toast.makeText(DangNhapActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Handle Facebook Sign In
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void checkIfUserLoggedIn() {
        FirebaseUser currentUser = firebaseHelper.getCurrentUser();
        if (currentUser != null) {

        }
    }
    private void navigateToMainActivity() {
        Intent intent = new Intent(DangNhapActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
        finish();
    }
}