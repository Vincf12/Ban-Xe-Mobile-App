package com.example.carsale;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getEmail().equals("phungkhoa425@gmail.com")) {
            // Là admin
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.menu_admin);
            loadAdminFragment(); // Gọi fragment admin
        } else {
            // Là người dùng thường
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.menu);
            loadHomeFragment(); // Gọi fragment người dùng thường
        }
        // Thiết lập listener cho sự kiện chọn menu
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // Xử lý khi chọn Trang chủ
                    loadHomeFragment();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    // Xử lý khi chọn Settings
                    loadSettingsFragment();
                    return true;
                } else if (itemId == R.id.nav_account) {
                    // Xử lý khi chọn Tài khoản
                    loadAccountFragment();
                    return true;
                } else if (itemId == R.id.nav_admin) {
                    loadAdminFragment();
                    return true;
                } else if (itemId == R.id.nav_settings_admin){
                    loadAdminFragment();
                    return true;
                } else if (itemId == R.id.nav_account_admin) {
                    loadAdminFragment();
                    return true;
                } else if (itemId ==R.id.nav_home_admin) {
                    loadAdminFragment();
                    return true;
                }

                return false;
            }
        });

        // Đặt item đầu tiên được chọn mặc định
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void loadHomeFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new HomeFragment())
                .commit();
    }

    private void loadSettingsFragment() {
        // Code để load fragment settings
        // getSupportFragmentManager().beginTransaction()
        //     .replace(R.id.nav_host_fragment, new SettingsFragment())
        //     .commit();
    }

    private void loadAccountFragment() {
        // Code để load fragment tài khoản
        // getSupportFragmentManager().beginTransaction()
        //     .replace(R.id.nav_host_fragment, new AccountFragment())
        //     .commit();
    }
    private void loadAdminFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new AdminFragment())
                .commit();
    }
}