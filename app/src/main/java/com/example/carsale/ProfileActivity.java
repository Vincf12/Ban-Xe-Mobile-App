package com.example.carsale;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carsale.Database.FirebaseHelper;
import com.example.carsale.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private EditText etFullname, etAge, etEmail, etPhone, etcccd;
    private RadioGroup rgGender;
    private Button btnSave;
    private ImageButton btnBack;

    private FirebaseHelper firebaseHelper;
    private FirebaseAuth auth;
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupFirebase();
        setupListeners();
        loadUserInfo();
    }

    private void initViews() {
        etFullname = findViewById(R.id.et_fullname);
        etAge = findViewById(R.id.et_age);
        etcccd = findViewById(R.id.et_cccd);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        rgGender = findViewById(R.id.rg_gender);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupFirebase() {
        firebaseHelper = FirebaseHelper.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProfile());

        // Xử lý nút back
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        // Kiểm tra xem có thay đổi gì chưa được lưu không
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        if (currentUser == null) return false;

        String currentFullname = etFullname.getText().toString().trim();
        String currentAgeStr = etAge.getText().toString().trim();
        int currentAge = 0;
        try {
            currentAge = Integer.parseInt(currentAgeStr);
        } catch (NumberFormatException e) {
            // Ignore
        }
        String currentCccdStr = etcccd.getText().toString().trim();
        int currentCccd = 0;
        try {
            currentCccd = Integer.parseInt(currentCccdStr);
        } catch (NumberFormatException e) {
            // Ignore
        }
        String currentGender = getSelectedGender();
        String currentEmail = etEmail.getText().toString().trim();
        String currentPhone = etPhone.getText().toString().trim();

        // So sánh với dữ liệu gốc
        boolean nameChanged = !currentFullname.equals(currentUser.getFullname() != null ? currentUser.getFullname() : "");
        boolean ageChanged = currentAge != currentUser.getAge();
        boolean cccdChanged = currentCccd != currentUser.getCccd();
        boolean genderChanged = !currentGender.equals(currentUser.getGender() != null ? currentUser.getGender() : "");
        boolean emailChanged = !currentEmail.equals(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        boolean phoneChanged = !currentPhone.equals(currentUser.getPhone() != null ? currentUser.getPhone() : "");

        return nameChanged || ageChanged || cccdChanged || genderChanged || emailChanged || phoneChanged;
    }

    private String getSelectedGender() {
        int checkedId = rgGender.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_male) return "Nam";
        else if (checkedId == R.id.rb_female) return "Nữ";
        else if (checkedId == R.id.rb_other) return "Khác";
        return "";
    }

    private void showUnsavedChangesDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có thay đổi chưa được lưu. Bạn có muốn thoát không?")
                .setPositiveButton("Thoát", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Ở lại", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setNeutralButton("Lưu và thoát", (dialog, which) -> {
                    saveProfile();
                })
                .show();
    }

    private void loadUserInfo() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            // Hiển thị loading
            showLoading(true);

            firebaseHelper.getUserById(firebaseUser.getUid(), new FirebaseHelper.OnUserDataListener() {
                @Override
                public void onSuccess(User user) {
                    showLoading(false);
                    if (user != null) {
                        currentUser = user;
                        populateUserData(user);
                    }
                }

                @Override
                public void onFailure(String error) {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, "Lỗi tải thông tin: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void populateUserData(User user) {
        if (user.getFullname() != null) {
            etFullname.setText(user.getFullname());
        }
        if (user.getAge() > 0) {
            etAge.setText(String.valueOf(user.getAge()));
        }
        if (user.getCccd() > 0) {
            etcccd.setText(String.valueOf(user.getCccd()));
        }
        if (user.getGender() != null) {
            switch (user.getGender()) {
                case "Nam":
                    rgGender.check(R.id.rb_male);
                    break;
                case "Nữ":
                    rgGender.check(R.id.rb_female);
                    break;
                default:
                    rgGender.check(R.id.rb_other);
            }
        }
        if (user.getEmail() != null) {
            etEmail.setText(user.getEmail());
        }
        if (user.getPhone() != null) {
            etPhone.setText(user.getPhone());
        }
    }

    private void showLoading(boolean show) {
        // Implement loading indicator if needed
        btnSave.setEnabled(!show);
        btnSave.setText(show ? "Đang tải..." : "Lưu thông tin");
    }

    private void saveProfile() {
        if (!validateInputs()) return;

        String fullname = etFullname.getText().toString().trim();
        int age = Integer.parseInt(etAge.getText().toString().trim());
        int cccd = 0;
        String cccdStr = etcccd.getText().toString().trim();
        if (!cccdStr.isEmpty()) {
            try {
                cccd = Integer.parseInt(cccdStr);
            } catch (NumberFormatException e) {
                cccd = 0;
            }
        }
        String gender = getSelectedGender();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật user
        if (currentUser == null) currentUser = new User();
        currentUser.setId(firebaseUser.getUid());
        currentUser.setFullname(fullname);
        currentUser.setAge(age);
        currentUser.setCccd(cccd);
        currentUser.setGender(gender);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);

        // Hiển thị loading
        showLoading(true);

        firebaseHelper.updateUser(currentUser, new FirebaseHelper.OnDataListener() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(ProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        String fullname = etFullname.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String cccdStr = etcccd.getText().toString().trim();
        int checkedId = rgGender.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(fullname)) {
            etFullname.setError("Vui lòng nhập họ tên");
            etFullname.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(ageStr)) {
            etAge.setError("Vui lòng nhập tuổi");
            etAge.requestFocus();
            return false;
        }

        try {
            int age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 150) {
                etAge.setError("Tuổi phải từ 1 đến 150");
                etAge.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etAge.setError("Tuổi không hợp lệ");
            etAge.requestFocus();
            return false;
        }

        if (!TextUtils.isEmpty(cccdStr)) {
            try {
                int cccd = Integer.parseInt(cccdStr);
                if (cccd <= 0) {
                    etcccd.setError("CCCD phải lớn hơn 0");
                    etcccd.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                etcccd.setError("CCCD không hợp lệ");
                etcccd.requestFocus();
                return false;
            }
        }

        if (checkedId == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}