package com.example.carsale;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.carsale.Database.CarHelper;
import com.example.carsale.Model.Car;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.Manifest;
import android.content.pm.PackageManager;

public class AddEditCarActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;

    private EditText etCarModel, etYear, etPrice, etEngineCapacity, etLocation, etDescription;
    private Spinner spinnerCondition, spinnerCarType, spinnerTransmission, spinnerFuelType, spinnerCarMake;
    private Button btnSelectImages, btnSaveCar;
    private LinearLayout layoutImagePreview;

    private Car car;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private List<String> uploadedImageUrls = new ArrayList<>();

    private List<String> carMakes = new ArrayList<>();
    private ArrayAdapter<String> carMakeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_car);

        initViews();
        setupSpinners();
        loadCarMakesFromFirestore();

        car = (Car) getIntent().getSerializableExtra("car");

        if (car != null) {
            etCarModel.setText(car.getModel());
            etYear.setText(String.valueOf(car.getYear()));
            etPrice.setText(String.valueOf(car.getPrice()));
            etEngineCapacity.setText(car.getEngineCapacity());
            etLocation.setText(car.getLocation());
            etDescription.setText(car.getDescription());

            spinnerCondition.setSelection(getSpinnerIndex(spinnerCondition, car.getCondition()));
            spinnerCarType.setSelection(getSpinnerIndex(spinnerCarType, car.getCarType()));
            spinnerTransmission.setSelection(getSpinnerIndex(spinnerTransmission, car.getTransmission()));
            spinnerFuelType.setSelection(getSpinnerIndex(spinnerFuelType, car.getFuelType()));

            if (car.getImageUrls() != null && !car.getImageUrls().isEmpty()) {
                for (String path : car.getImageUrls()) {
                    Uri uri = Uri.fromFile(new File(path));
                    selectedImageUris.add(uri);
                    addImagePreview(uri);
                }
            }
        }

        btnSelectImages.setOnClickListener(v -> openImagePicker());
        btnSaveCar.setOnClickListener(v -> uploadImagesAndSaveCar());

    }
    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0; // Mặc định chọn dòng đầu nếu không khớp
    }

    private void initViews() {
        etCarModel = findViewById(R.id.etCarModel);
        etYear = findViewById(R.id.etYear);
        etPrice = findViewById(R.id.etPrice);
        etEngineCapacity = findViewById(R.id.etEngineCapacity);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);

        spinnerCarMake = findViewById(R.id.spinnerCarMake);
        spinnerCondition = findViewById(R.id.spinnerCondition);
        spinnerCarType = findViewById(R.id.spinnerCarType);
        spinnerTransmission = findViewById(R.id.spinnerTransmission);
        spinnerFuelType = findViewById(R.id.spinnerFuelType);

        btnSelectImages = findViewById(R.id.btnSelectImages);
        btnSaveCar = findViewById(R.id.btnSaveCar);
        layoutImagePreview = findViewById(R.id.layoutImagePreview);
    }

    private void setupSpinners() {
        spinnerCondition.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{"Mới", "Đã qua sử dụng"}));
        spinnerCarType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{"Sedan", "SUV", "Hatchback", "Bán tải"}));
        spinnerTransmission.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{"Tự động", "Số sàn"}));
        spinnerFuelType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{"Xăng", "Dầu", "Điện", "Hybrid"}));

        // Adapter rỗng cho hãng xe
        carMakeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, carMakes);
        spinnerCarMake.setAdapter(carMakeAdapter);
    }

    private void openImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh xe"), PICK_IMAGES_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            Toast.makeText(this, "Cần quyền truy cập ảnh để chọn", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUris.clear();
            layoutImagePreview.removeAllViews();

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                    addImagePreview(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                selectedImageUris.add(imageUri);
                addImagePreview(imageUri);
            }
        }
    }

    private void addImagePreview(Uri uri) {
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(uri);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
        params.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(params);
        layoutImagePreview.addView(imageView);
    }

    private void uploadImagesAndSaveCar() {
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 ảnh xe", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadedImageUrls = saveImagesLocally();

        if (uploadedImageUrls.isEmpty()) {
            Toast.makeText(this, "Không thể lưu ảnh, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        saveCarToFirestore();
    }

    private List<String> saveImagesLocally() {
        List<String> imagePaths = new ArrayList<>();
        File directory = new File(getFilesDir(), "car_images");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e("StorageError", "Không thể tạo thư mục: " + directory.getAbsolutePath());
                return imagePaths;
            }
        }

        for (Uri imageUri : selectedImageUris) {
            try {
                String fileName = UUID.randomUUID().toString() + ".jpg";
                File file = new File(directory, fileName);
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    Log.e("StorageError", "Không thể mở InputStream cho Uri: " + imageUri);
                    continue;
                }

                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();

                imagePaths.add(file.getAbsolutePath());
                Log.d("StorageError", "Ảnh được lưu tại: " + file.getAbsolutePath());
            } catch (IOException e) {
                Log.e("StorageError", "Lỗi lưu ảnh: " + e.getMessage());
                Toast.makeText(this, "Lỗi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return imagePaths;
    }

    private void saveCarToFirestore() {
        String model = etCarModel.getText().toString().trim();
        String make = spinnerCarMake.getSelectedItem().toString();
        String yearStr = etYear.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (TextUtils.isEmpty(model) || TextUtils.isEmpty(make) || TextUtils.isEmpty(yearStr) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ các trường bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        int year = Integer.parseInt(yearStr);
        double price = Double.parseDouble(priceStr);

        if (car == null) {
            car = new Car();
            car.setCreatedAt(System.currentTimeMillis());
            car.setStatus("available");
            car.setUserId("admin");
        }
        car.setModel(model);
        car.setMake(make);
        car.setYear(year);
        car.setPrice(price);
        car.setCondition(spinnerCondition.getSelectedItem().toString());
        car.setCarType(spinnerCarType.getSelectedItem().toString());
        car.setTransmission(spinnerTransmission.getSelectedItem().toString());
        car.setFuelType(spinnerFuelType.getSelectedItem().toString());
        car.setEngineCapacity(etEngineCapacity.getText().toString().trim());
        car.setLocation(etLocation.getText().toString().trim());
        car.setDescription(etDescription.getText().toString().trim());
        car.setStatus("available");
        car.setImageUrls(uploadedImageUrls); // Lưu đường dẫn cục bộ
        car.setUserId("admin"); // hoặc lấy từ người dùng đang đăng nhập
        car.setCreatedAt(System.currentTimeMillis());
        car.setUpdatedAt(System.currentTimeMillis());
        if (TextUtils.isEmpty(car.getId())) {
            // Tạo mới
            CarHelper.getInstance().addCar(car, new CarHelper.CarCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(AddEditCarActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(AddEditCarActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Update
            CarHelper.getInstance().updateCar(car, new CarHelper.CarCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(AddEditCarActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(AddEditCarActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void loadCarMakesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("car_makes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    carMakes.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null) {
                            carMakes.add(name);
                        }
                    }
                    carMakeAdapter.notifyDataSetChanged();

                    // Nếu đang sửa, gán lại hãng đã chọn
                    if (car != null) {
                        int index = getSpinnerIndex(spinnerCarMake, car.getMake());
                        spinnerCarMake.setSelection(index);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải hãng xe: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}