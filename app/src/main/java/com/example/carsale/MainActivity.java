package com.example.carsale;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private boolean isAdmin = false;
    private static final String ADMIN_EMAIL = "phungkhoa425@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFirebase();
        setupBottomNavigation();

    }

    private void initFirebase() {
        // FirebaseApp.initializeApp chỉ cần gọi 1 lần duy nhất cho toàn app (nếu chưa được gọi ở Application)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
        );
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        isAdmin = user != null && ADMIN_EMAIL.equals(user.getEmail());

        bottomNavigationView.getMenu().clear();
        if (isAdmin) {
            bottomNavigationView.inflateMenu(R.menu.menu_admin);
            bottomNavigationView.setSelectedItemId(R.id.nav_home_admin);
            loadFragment(FragmentType.ADMIN);
        } else {
            bottomNavigationView.inflateMenu(R.menu.menu);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            loadFragment(FragmentType.HOME);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            FragmentType type = getFragmentTypeForMenuItem(item.getItemId());
            if (type != null) {
                loadFragment(type);
                return true;
            }
            return false;
        });

        // Đảm bảo bottom nav không bị che khuất bởi thanh điều hướng/IME
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime()).bottom
            );
            return insets;
        });
    }

    private enum FragmentType { HOME, ADMIN, SETTINGS, ACCOUNT }

    private FragmentType getFragmentTypeForMenuItem(int itemId) {
        if (isAdmin) {
            if (itemId == R.id.nav_home_admin) return FragmentType.ADMIN;
            if (itemId == R.id.nav_settings_admin) return FragmentType.SETTINGS;
            if (itemId == R.id.nav_account_admin) return FragmentType.ACCOUNT;
        } else {
            if (itemId == R.id.nav_home) return FragmentType.HOME;
            if (itemId == R.id.nav_settings) return FragmentType.SETTINGS;
            if (itemId == R.id.nav_account) return FragmentType.ACCOUNT;
        }
        return null;
    }

    private void loadFragment(FragmentType type) {
        switch (type) {
            case HOME:
                replaceFragment(new HomeFragment());
                break;
            case ADMIN:
                replaceFragment(new AdminFragment());
                break;
            case SETTINGS:
                // Nếu chưa cần, có thể bỏ comment sau khi tạo fragment
                // replaceFragment(new SettingsFragment());
                break;
            case ACCOUNT:
                // replaceFragment(new AccountFragment());
                break;
        }
    }

    private void replaceFragment(androidx.fragment.app.Fragment fragment) {
        Bundle bundle = getBaseBundle();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitAllowingStateLoss();
    }

    private Bundle getBaseBundle() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isAdmin", isAdmin);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        bundle.putString("userId", user != null ? user.getUid() : null);
        return bundle;
    }
}