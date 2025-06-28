package com.example.carsale;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carsale.AdminFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBottomNavigation();

        FirebaseApp.initializeApp(this);

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
        );
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getEmail().equals("phungkhoa425@gmail.com")) {
            isAdmin = true;
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.menu_admin);
            bottomNavigationView.setSelectedItemId(R.id.nav_home_admin);
            loadAdminFragment();
        } else {
            isAdmin = false;
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.menu);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            loadHomeFragment();
        }

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (isAdmin) {
                    if (itemId == R.id.nav_home_admin) {
                        loadAdminFragment();
                        return true;
                    } else if (itemId == R.id.nav_settings_admin) {
                        loadSettingsFragment();
                        return true;
                    } else if (itemId == R.id.nav_account_admin) {
                        loadAccountFragment();
                        return true;
                    }
                } else {
                    if (itemId == R.id.nav_home) {
                        loadHomeFragment();
                        return true;
                    } else if (itemId == R.id.nav_settings) {
                        loadSettingsFragment();
                        return true;
                    } else if (itemId == R.id.nav_account) {
                        loadAccountFragment();
                        return true;
                    }
                }

                return false;
            }
        });
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

    private void loadHomeFragment() {
        HomeFragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isAdmin", isAdmin);
        bundle.putString("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }

    private void loadAdminFragment() {
        AdminFragment fragment = new AdminFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isAdmin", isAdmin);
        bundle.putString("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }

    private void loadSettingsFragment() {
        // getSupportFragmentManager().beginTransaction()
        //         .replace(R.id.nav_host_fragment, new SettingsFragment())
        //         .commit();
    }

    private void loadAccountFragment() {
        // getSupportFragmentManager().beginTransaction()
        //         .replace(R.id.nav_host_fragment, new AccountFragment())
        //         .commit();
    }
}
