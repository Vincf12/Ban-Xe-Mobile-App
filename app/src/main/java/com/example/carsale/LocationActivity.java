package com.example.carsale;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.Adapter.ProvinceAdapter;
import com.example.carsale.API.ProvinceApiService;
import com.example.carsale.Database.FirebaseHelper;
import com.example.carsale.Model.BaseResponse;
import com.example.carsale.Model.District;
import com.example.carsale.Model.Province;
import com.example.carsale.Model.User;
import com.example.carsale.Model.Ward;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationActivity extends AppCompatActivity {
    private ImageView ivBack;
    private EditText etSearch;
    private Button btnSave;
    private TextView tvCurrentLocation;
    private RecyclerView rvProvinces;
    private ProgressBar progressBar;
    private Spinner spinnerDistrict, spinnerWard;

    private ProvinceAdapter adapter;
    private List<Province> provinces;
    private String selectedLocation = null;
    private FirebaseHelper firebaseHelper;
    private FirebaseAuth auth;
    private Province selectedProvince;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        ivBack = findViewById(R.id.iv_back);
        etSearch = findViewById(R.id.et_search);
        btnSave = findViewById(R.id.btn_save);
        tvCurrentLocation = findViewById(R.id.tv_current_location);
        rvProvinces = findViewById(R.id.rv_provinces);
        progressBar = findViewById(R.id.progress_bar);
        spinnerDistrict = findViewById(R.id.spinner_district);
        spinnerWard = findViewById(R.id.spinner_ward);

        rvProvinces.setLayoutManager(new LinearLayoutManager(this));
        firebaseHelper = FirebaseHelper.getInstance();
        auth = FirebaseAuth.getInstance();

        ivBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveLocation());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                loadProvinces(s.toString());
            }
        });

        loadCurrentLocation();
        loadProvinces(""); // Load all provinces ban đầu
    }

    private void loadProvinces(String query) {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://open.oapi.vn/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ProvinceApiService service = retrofit.create(ProvinceApiService.class);
        Call<BaseResponse<Province>> call = service.getProvinces(0, 100, query);

        call.enqueue(new Callback<BaseResponse<Province>>() {
            @Override
            public void onResponse(Call<BaseResponse<Province>> call, Response<BaseResponse<Province>> response) {
                progressBar.setVisibility(ProgressBar.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    provinces = response.body().getData();
                    adapter = new ProvinceAdapter(LocationActivity.this, provinces, province -> {
                        selectedProvince = province;
                        selectedLocation = province.getName();
                        loadDistricts(province.getId());
                    });
                    rvProvinces.setAdapter(adapter);
                } else {
                    Toast.makeText(LocationActivity.this, "Không thể tải danh sách tỉnh thành", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Province>> call, Throwable t) {
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(LocationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCurrentLocation() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            firebaseHelper.getUserById(currentUser.getUid(), new FirebaseHelper.OnUserDataListener() {
                @Override
                public void onSuccess(User user) {
                    if (user != null && user.getAddress() != null && !user.getAddress().isEmpty()) {
                        tvCurrentLocation.setText(user.getAddress());
                        selectedLocation = user.getAddress();
                    } else {
                        tvCurrentLocation.setText("Chưa có");
                    }
                }
                @Override
                public void onFailure(String error) {
                    tvCurrentLocation.setText("Chưa có");
                }
            });
        }
    }

    private void saveLocation() {
        if (selectedLocation == null) {
            Toast.makeText(this, "Vui lòng chọn một tỉnh/thành phố", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseHelper.getUserById(currentUser.getUid(), new FirebaseHelper.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    String province = selectedProvince != null ? selectedProvince.getName() : "";
                    String district = spinnerDistrict.getSelectedItem() != null ? spinnerDistrict.getSelectedItem().toString() : "";
                    String ward = spinnerWard.getSelectedItem() != null ? spinnerWard.getSelectedItem().toString() : "";
                    String fullAddress = ward + ", " + district + ", " + province;
                    user.setAddress(fullAddress);
                    firebaseHelper.updateUser(user, new FirebaseHelper.OnDataListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(LocationActivity.this, "Đã lưu vị trí thành công", Toast.LENGTH_SHORT).show();
                            tvCurrentLocation.setText(fullAddress);
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("selected_location", fullAddress);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(LocationActivity.this, "Lỗi khi lưu: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(LocationActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(LocationActivity.this, "Lỗi khi tải thông tin người dùng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDistricts(String provinceId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://open.oapi.vn/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ProvinceApiService service = retrofit.create(ProvinceApiService.class);
        service.getDistricts(provinceId).enqueue(new Callback<BaseResponse<District>>() {
            @Override
            public void onResponse(Call<BaseResponse<District>> call, Response<BaseResponse<District>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<District> districts = response.body().getData();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(LocationActivity.this,
                            android.R.layout.simple_spinner_item,
                            districts.stream().map(District::getName).collect(Collectors.toList()));
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDistrict.setAdapter(adapter);

                    spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            District selectedDistrict = districts.get(position);
                            loadWards(selectedDistrict.getId());
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }
            @Override
            public void onFailure(Call<BaseResponse<District>> call, Throwable t) {
                Toast.makeText(LocationActivity.this, "Lỗi tải quận/huyện", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWards(String districtId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://open.oapi.vn/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ProvinceApiService service = retrofit.create(ProvinceApiService.class);
        service.getWards(districtId).enqueue(new Callback<BaseResponse<Ward>>() {
            @Override
            public void onResponse(Call<BaseResponse<Ward>> call, Response<BaseResponse<Ward>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Ward> wards = response.body().getData();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(LocationActivity.this,
                            android.R.layout.simple_spinner_item,
                            wards.stream().map(Ward::getName).collect(Collectors.toList()));
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerWard.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<BaseResponse<Ward>> call, Throwable t) {
                Toast.makeText(LocationActivity.this, "Lỗi tải phường/xã", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 