package com.example.carsale;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private boolean isAdmin = false;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFirebase();

        // Gán view
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);

        // Ban đầu chỉ hiện loading
        showLoading(true);

        // Check role
        checkUserRoleAndSetupUI();
    }

    private void initFirebase() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
        );
    }

    private void checkUserRoleAndSetupUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("DEBUG_UID", "Current user UID: " + (user != null ? user.getUid() : "null"));
        if (user == null) {
            Log.w(TAG, "Người dùng chưa đăng nhập.");
            showLoading(false);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isAdmin = Boolean.TRUE.equals(documentSnapshot.getBoolean("admin"));
                    setupUIBasedOnRole();
                    showLoading(false); // Hiện layout chính khi đã có role
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Không thể lấy dữ liệu admin", e);
                    isAdmin = false;
                    setupUIBasedOnRole();
                    showLoading(false);
                });
    }

    private void setupUIBasedOnRole() {
        bottomNavigationView.getMenu().clear();

        if (isAdmin) {
            bottomNavigationView.inflateMenu(R.menu.menu_admin);
            bottomNavigationView.setSelectedItemId(R.id.nav_home_admin);
            loadFragment(FragmentType.ADMIN);

            navigationView.setVisibility(View.VISIBLE);
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.menu_admin_drawer);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        } else {
            bottomNavigationView.inflateMenu(R.menu.menu);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            loadFragment(FragmentType.HOME);

            navigationView.setVisibility(View.GONE);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            FragmentType type = getFragmentTypeForMenuItem(item.getItemId());
            
            if (type != null) {
                loadFragment(type);
                return true;
            }
            return false;
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

    private enum FragmentType { HOME, ADMIN, SEARCH, ACCOUNT, PAY, CHAR}

    private FragmentType getFragmentTypeForMenuItem(int itemId) {
        if (isAdmin) {
            if (itemId == R.id.nav_home_admin) return FragmentType.ADMIN;
            if (itemId == R.id.nav_search_admin) return FragmentType.SEARCH;
            if (itemId == R.id.nav_char_admin) return FragmentType.CHAR;
            if (itemId == R.id.nav_pay) return  FragmentType.PAY;
            if (itemId == R.id.nav_account_admin) return FragmentType.ACCOUNT;
        } else {
            if (itemId == R.id.nav_home) return FragmentType.HOME;
            if (itemId == R.id.nav_search) return FragmentType.SEARCH;
            if (itemId == R.id.nav_account) return FragmentType.ACCOUNT;
        }
        return null;
    }

    private void loadFragment(FragmentType type) {
        switch (type) {
            case HOME:
                replaceFragment(new HomeFragment());
                updateDrawerToggleVisibility(false);
                break;
            case ADMIN:
                replaceFragment(new AdminFragment());
                updateDrawerToggleVisibility(true);
                break;
            case SEARCH:
                replaceFragment(new SearchFragment());
                updateDrawerToggleVisibility(false);
                break;
            case ACCOUNT:
                replaceFragment(new AccountFragment());
                updateDrawerToggleVisibility(false);
                break;
            case CHAR:
                replaceFragment(new AdminDashboardFragment());
                updateDrawerToggleVisibility(false);
                break;
            case PAY:
                replaceFragment(new PaymentConfirmFragment());
                updateDrawerToggleVisibility(false);
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

    private void updateDrawerToggleVisibility(boolean showToggle) {
        if (showToggle) {
            if (drawerToggle == null) {
                drawerToggle = new ActionBarDrawerToggle(
                        this,
                        drawerLayout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                );
                drawerLayout.addDrawerListener(drawerToggle);
                drawerToggle.syncState();
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            drawerToggle.setDrawerIndicatorEnabled(true);
        } else {
            if (drawerToggle != null) {
                drawerLayout.removeDrawerListener(drawerToggle);
                drawerToggle.setDrawerIndicatorEnabled(false);
                drawerToggle = null;
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private Bundle getBaseBundle() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isAdmin", isAdmin);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        bundle.putString("userId", user != null ? user.getUid() : null);
        return bundle;
    }

    private void showLoading(boolean show) {
        View loadingView = findViewById(R.id.progress_bar);
        View mainRoot = findViewById(R.id.main_root);

        if (loadingView != null && mainRoot != null) {
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            mainRoot.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
