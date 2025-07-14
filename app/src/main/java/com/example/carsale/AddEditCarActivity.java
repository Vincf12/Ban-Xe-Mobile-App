package com.example.carsale;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.carsale.Database.CarHelper;
import com.example.carsale.Model.Car;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AddEditCarActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_IMAGES = 100;
    private static final int PICK_COLOR_IMAGES_REQUEST = 101;

    private EditText etCarModel, etYear, etPrice, etEngineCapacity, etLocation, etDescription, etQuantity, etDepositPrice, etBH;
    private Spinner spinnerCondition, spinnerCarType, spinnerTransmission, spinnerFuelType, spinnerCarMake;
    private Button btnSaveCar, btnSelectColorImages;
    private LinearLayout layoutImagePreview;

    private Car car;
    private final List<String> carMakes = new ArrayList<>();
    private ArrayAdapter<String> carMakeAdapter;

    // Sửa lỗi: Thêm các biến còn thiếu
    private Map<String, List<Uri>> colorImageUris = new HashMap<>();
    private Map<String, List<String>> colorImages = new HashMap<>();
    private List<String> generalImages = new ArrayList<>(); // Thêm biến này
    private String currentColor = null;
    private boolean confirm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_car);

        initViews();
        setupSpinners();
        loadCarMakesFromFirestore();

        Intent intent = getIntent();
        car = (Car) intent.getSerializableExtra("car");
        if (car != null) {
            populateFieldsFromCar(car);
        } else {
            // fallback: lấy carId như cũ
        }

        btnSaveCar.setOnClickListener(v -> uploadColorImagesAndSaveCar());

        ChipGroup chipGroupColors = findViewById(R.id.chipGroupColors);
        for (int i = 0; i < chipGroupColors.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupColors.getChildAt(i);
            chip.setOnClickListener(v -> {
                String color = chip.getText().toString();
                currentColor = color;
                if (!colorImageUris.containsKey(color)) {
                    colorImageUris.put(color, new ArrayList<>());
                }
                updateColorImagePreview(color);
            });
        }

        btnSelectColorImages = findViewById(R.id.btnSelectColorImages);
        btnSelectColorImages.setOnClickListener(v -> {
            if (currentColor == null) {
                Toast.makeText(this, "Hãy chọn màu trước!", Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker();
        });
    }

    private void getCarData() {
        Intent intent = getIntent();
        String carId = intent != null ? intent.getStringExtra("carId") : null;
        if (carId != null && !carId.isEmpty()) {
            CarHelper.getInstance().getCarById(carId, new CarHelper.CarDetailCallback() {
                @Override
                public void onSuccess(Car dbCar) {
                    car = dbCar;
                    runOnUiThread(() -> {
                        populateFieldsFromCar(car);
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddEditCarActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        finish(); // Đóng activity nếu không lấy được dữ liệu
                    });
                }
            });
        } else {
            // Thêm mới: không làm gì, giữ nguyên form trống
        }
    }

    private void populateFieldsFromCar(Car car) {
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

        // Thêm dòng này để set số lượng
        etQuantity.setText(String.valueOf(car.getQuantity()));

        // Thêm dòng này để set giá đặt cọc
        etDepositPrice.setText(String.valueOf(car.getDepositPrice()));
        etBH.setText(car.getEtBH() != null ? car.getEtBH() : "");

        // Sửa lỗi: Xử lý colorImages từ Car object
        if (car.getColorImages() != null) {
            colorImages = car.getColorImages();
            // Convert URLs thành URIs để hiển thị preview
            for (String color : colorImages.keySet()) {
                List<String> urls = colorImages.get(color);
                List<Uri> uris = new ArrayList<>();
                if (urls != null) {
                    for (String url : urls) {
                        // Nếu là URL từ server, chuyển thành URI
                        if (url.startsWith("http")) {
                            uris.add(Uri.parse(url));
                        } else {
                            // Nếu là local file path
                            uris.add(Uri.fromFile(new File(url)));
                        }
                    }
                }
                colorImageUris.put(color, uris);
            }
        }
    }

    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private void initViews() {
        etCarModel = findViewById(R.id.etCarModel);
        etYear = findViewById(R.id.etYear);
        etPrice = findViewById(R.id.etPrice);
        etEngineCapacity = findViewById(R.id.etEngineCapacity);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        etQuantity = findViewById(R.id.etQuantity);
        etDepositPrice = findViewById(R.id.etDepositPrice);
        etBH = findViewById(R.id.etBH);

        spinnerCarMake = findViewById(R.id.spinnerCarMake);
        spinnerCondition = findViewById(R.id.spinnerCondition);
        spinnerCarType = findViewById(R.id.spinnerCarType);
        spinnerTransmission = findViewById(R.id.spinnerTransmission);
        spinnerFuelType = findViewById(R.id.spinnerFuelType);

        btnSaveCar = findViewById(R.id.btnSaveCar);
        btnSelectColorImages = findViewById(R.id.btnSelectColorImages);
        layoutImagePreview = findViewById(R.id.layoutImagePreview);
    }

    private void setupSpinners() {
        setSpinnerAdapter(spinnerCondition, new String[]{"Mới", "Đã qua sử dụng"});
        setSpinnerAdapter(spinnerCarType, new String[]{"Sedan", "SUV", "Hatchback", "Bán tải"});
        setSpinnerAdapter(spinnerTransmission, new String[]{"Tự động", "Số sàn"});
        setSpinnerAdapter(spinnerFuelType, new String[]{"Xăng", "Dầu", "Điện", "Hybrid"});
        carMakeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, carMakes);
        spinnerCarMake.setAdapter(carMakeAdapter);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Arrays.asList(items)));
    }

    // Sửa lỗi: Thêm method openImagePicker
    private void openImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_READ_IMAGES);
        } else {
            pickImagesIntent();
        }
    }

    // Sửa lỗi: Thêm method pickImagesIntent
    private void pickImagesIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh cho màu " + currentColor), PICK_COLOR_IMAGES_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_IMAGES &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImagesIntent();
        } else {
            Toast.makeText(this, "Cần quyền truy cập ảnh để chọn", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_COLOR_IMAGES_REQUEST && resultCode == Activity.RESULT_OK && data != null && currentColor != null) {
            List<Uri> uris = colorImageUris.get(currentColor);
            if (uris == null) {
                uris = new ArrayList<>();
            }

            if (data.getClipData() != null) {
                // Nhiều ảnh được chọn
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    uris.add(imageUri);
                }
            } else if (data.getData() != null) {
                // Một ảnh được chọn
                uris.add(data.getData());
            }

            colorImageUris.put(currentColor, uris);
            updateColorImagePreview(currentColor);
        }
    }

    private void addImagePreview(Uri uri) {
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Sử dụng Glide để load ảnh tốt hơn
        Glide.with(this)
                .load(uri)
                .into(imageView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
        params.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(params);
        layoutImagePreview.addView(imageView);
    }

    private void uploadColorImagesAndSaveCar() {
        boolean hasNewImages = false;
        for (List<Uri> uris : colorImageUris.values()) {
            if (uris != null && !uris.isEmpty()) {
                hasNewImages = true;
                break;
            }
        }

        if (!hasNewImages && car != null && car.getColorImages() != null && !car.getColorImages().isEmpty()) {
            // Không có ảnh mới, dùng lại ảnh cũ
            colorImages = car.getColorImages();
            saveCarToFirestore();
            return;
        }

        if (!hasNewImages) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang upload ảnh theo màu...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        CloudinaryManager cloudinaryManager = new CloudinaryManager();
        colorImages = new HashMap<>();

        new Thread(() -> {
            try {
                for (String color : colorImageUris.keySet()) {
                    List<Uri> uris = colorImageUris.get(color);
                    List<String> urls = new ArrayList<>();

                    if (uris != null && !uris.isEmpty()) {
                        for (Uri uri : uris) {
                            try {
                                // Tạo file tạm
                                String fileName = "temp_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".jpg";
                                File file = new File(getCacheDir(), fileName);

                                // Copy từ URI vào file
                                try (InputStream inputStream = getContentResolver().openInputStream(uri);
                                     FileOutputStream outputStream = new FileOutputStream(file)) {

                                    if (inputStream == null) {
                                        Log.e("Upload", "Cannot open input stream for URI: " + uri);
                                        continue;
                                    }

                                    byte[] buffer = new byte[4096];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                                        outputStream.write(buffer, 0, bytesRead);
                                    }
                                    outputStream.flush();
                                }

                                // Kiểm tra file
                                if (!file.exists() || file.length() == 0) {
                                    Log.e("Upload", "File is empty or not created: " + file.getPath());
                                    continue;
                                }

                                // Upload với CountDownLatch để đợi kết quả
                                CountDownLatch latch = new CountDownLatch(1);
                                final String[] resultUrl = {null};

                                cloudinaryManager.uploadImage(file, new CloudinaryManager.UploadCallback() {
                                    @Override
                                    public void onSuccess(String secureUrl) {
                                        resultUrl[0] = secureUrl;
                                        latch.countDown();
                                        Log.d("Upload", "Upload success: " + secureUrl);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("Upload", "Upload failed: " + e.getMessage(), e);
                                        latch.countDown();
                                    }
                                });

                                // Đợi upload hoàn thành hoặc timeout
                                boolean uploadCompleted = latch.await(30, TimeUnit.SECONDS);

                                if (uploadCompleted && resultUrl[0] != null) {
                                    urls.add(resultUrl[0]);
                                } else {
                                    Log.e("Upload", "Upload timeout or failed for file: " + file.getPath());
                                }

                                // Xóa file tạm
                                if (file.exists()) {
                                    file.delete();
                                }

                            } catch (Exception e) {
                                Log.e("Upload", "Error processing image: " + uri, e);
                                runOnUiThread(() ->
                                        Toast.makeText(AddEditCarActivity.this,
                                                "Lỗi xử lý ảnh: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    }

                    colorImages.put(color, urls);
                }

                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    // Kiểm tra xem có ảnh nào được upload thành công không
                    boolean hasUploadedImages = false;
                    for (List<String> urls : colorImages.values()) {
                        if (urls != null && !urls.isEmpty()) {
                            hasUploadedImages = true;
                            break;
                        }
                    }

                    if (hasUploadedImages) {
                        Toast.makeText(this, "Upload ảnh thành công!", Toast.LENGTH_SHORT).show();
                        saveCarToFirestore();
                    } else {
                        Toast.makeText(this, "Không có ảnh nào được upload thành công!", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Log.e("Upload", "Error in upload thread", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddEditCarActivity.this, "Lỗi upload: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void saveCarToFirestore() {
        String model = etCarModel.getText().toString().trim();
        String make = spinnerCarMake.getSelectedItem() != null ? spinnerCarMake.getSelectedItem().toString() : "";
        String yearStr = etYear.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String etBHStr = etBH.getText().toString().trim();

        if (TextUtils.isEmpty(model) || TextUtils.isEmpty(make) || TextUtils.isEmpty(yearStr) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ các trường bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
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
            car.setUserId("admin");
            car.setUpdatedAt(System.currentTimeMillis());
            car.setColorImages(colorImages);
            car.setEtBH(etBHStr);

            String quantityStr = etQuantity.getText().toString().trim();
            int quantity = 0;
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                // Có thể báo lỗi nếu cần
            }
            car.setQuantity(quantity);

            String depositStr = etDepositPrice.getText().toString().trim();
            double depositPrice = 0;
            try {
                depositPrice = Double.parseDouble(depositStr);
            } catch (NumberFormatException e) {
                // Có thể báo lỗi nếu cần
            }
            car.setDepositPrice(depositPrice);

            Log.d("AddEditCar", "quantity=" + car.getQuantity() + ", depositPrice=" + car.getDepositPrice());

            CarHelper.CarCallback callback = new CarHelper.CarCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(AddEditCarActivity.this,
                            car.getId() == null ? "Thêm xe thành công" : "Cập nhật thành công",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(AddEditCarActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            };

            if (car.getId() == null || car.getId().isEmpty()) {
                CarHelper.getInstance().addCar(car, callback);
            } else {
                CarHelper.getInstance().updateCar(car, callback);
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Năm sản xuất và giá phải là số hợp lệ", Toast.LENGTH_SHORT).show();
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
                        if (name != null) carMakes.add(name);
                    }
                    carMakeAdapter.notifyDataSetChanged();
                    if (car != null) {
                        int index = getSpinnerIndex(spinnerCarMake, car.getMake());
                        spinnerCarMake.setSelection(index);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải hãng xe: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateColorImagePreview(String color) {
        layoutImagePreview.removeAllViews();

        // Ảnh online (Cloudinary)
        List<String> urls = colorImages.get(color);
        if (urls != null && !urls.isEmpty()) {
            for (String url : new ArrayList<>(urls)) { // Duyệt qua bản copy để tránh ConcurrentModificationException
                ImageView imageView = new ImageView(this);
                Glide.with(this).load(url).into(imageView);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
                params.setMargins(8, 8, 8, 8);
                imageView.setLayoutParams(params);
                // Thêm click listener để xóa ảnh online
                imageView.setOnClickListener(v -> {
                    urls.remove(url);
                    colorImages.put(color, urls);
                    updateColorImagePreview(color);
                });
                layoutImagePreview.addView(imageView);
            }
        }

        // Ảnh local (chưa upload)
        List<Uri> uris = colorImageUris.get(color);
        if (uris != null && !uris.isEmpty()) {
            for (Uri uri : new ArrayList<>(uris)) {
                ImageView imageView = new ImageView(this);
                Glide.with(this).load(uri).into(imageView);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
                params.setMargins(8, 8, 8, 8);
                imageView.setLayoutParams(params);
                // Thêm click listener để xóa ảnh local
                imageView.setOnClickListener(v -> {
                    uris.remove(uri);
                    colorImageUris.put(color, uris);
                    updateColorImagePreview(color);
                });
                layoutImagePreview.addView(imageView);
            }
        }
    }

    // Sửa lỗi: Xóa các method không cần thiết và sửa lại
    private void onColorChipClicked(String color) {
        currentColor = color;
        updateColorImagePreview(color);
    }

    private void onAddImageForColor(String imagePath) {
        if (currentColor != null) {
            List<String> images = colorImages.get(currentColor);
            if (images == null) {
                images = new ArrayList<>();
                colorImages.put(currentColor, images);
            }
            images.add(imagePath);
            // Cập nhật preview nếu cần
        }
    }
}