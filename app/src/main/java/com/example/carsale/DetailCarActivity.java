package com.example.carsale;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carsale.Adapter.ImageThumbnailAdapter;
import com.example.carsale.Model.Car;
import com.example.carsale.Database.CarHelper;

import java.text.NumberFormat;
import java.util.*;

public class DetailCarActivity extends AppCompatActivity {

    private ImageButton btnBack, btnFavorite, btnShare;
    private ImageView imgMainCar;
    private TextView tvStatus, tvCarTitle, tvPrice, tvLocation, tvCreatedDate;
    private TextView tvCarType, tvYear, tvTransmission, tvFuelType, tvEngineCapacity, tvCondition;
    private TextView tvDescription;
    private Button btnCall, btnMessage;
    private RecyclerView rvImageThumbnails;
    private Spinner spinnerColors;
    private TextView tvQuantity, tvDepositPrice;
    private Button btnReserve;

    private Car car;
    private List<String> imageUrls;
    private ImageThumbnailAdapter imageAdapter;
    private int currentImageIndex = 0;

    private static final String PHONE_NUMBER = "0123456789";
    private static final String MESSAGE_TEXT = "Tôi quan tâm đến xe của bạn";

    private Map<String, Integer> colorQuantities = new HashMap<>(); // Số lượng theo màu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_car);

        initViews();
        getCarData();
        setupImageThumbnails();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnShare = findViewById(R.id.btn_share);
        imgMainCar = findViewById(R.id.img_main_car);
        tvStatus = findViewById(R.id.tv_status);
        tvCarTitle = findViewById(R.id.tv_car_title);
        tvPrice = findViewById(R.id.tv_price);
        tvLocation = findViewById(R.id.tv_location);
        tvCreatedDate = findViewById(R.id.tv_created_date);
        tvCarType = findViewById(R.id.tv_car_type);
        tvYear = findViewById(R.id.tv_year);
        tvTransmission = findViewById(R.id.tv_transmission);
        tvFuelType = findViewById(R.id.tv_fuel_type);
        tvEngineCapacity = findViewById(R.id.tv_engine_capacity);
        tvCondition = findViewById(R.id.tv_condition);
        tvDescription = findViewById(R.id.tv_description);
        btnCall = findViewById(R.id.btn_call);
        btnMessage = findViewById(R.id.btn_message);
        rvImageThumbnails = findViewById(R.id.rv_image_thumbnails);
        spinnerColors = findViewById(R.id.spinner_colors);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvDepositPrice = findViewById(R.id.tv_deposit_price);
        btnReserve = findViewById(R.id.btn_reserve);
    }

    private void getCarData() {
        Intent intent = getIntent();
        car = (Car) intent.getSerializableExtra("car");
        if (car == null) {
            Toast.makeText(this, "Không tìm thấy thông tin xe!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        runOnUiThread(() -> {
            setupImageThumbnails();
            displayCarData();
        });
    }

    private Car createSampleCar() {
        Car sampleCar = new Car();
        sampleCar.setId("1");
        sampleCar.setMake("Toyota");
        sampleCar.setModel("Camry");
        sampleCar.setYear(2022);
        sampleCar.setPrice(850000000);
        sampleCar.setCondition("Đã qua sử dụng");
        sampleCar.setCarType("Sedan");
        sampleCar.setTransmission("Tự động");
        sampleCar.setFuelType("Xăng");
        sampleCar.setEngineCapacity("2.0L");
        sampleCar.setDescription("Xe Toyota Camry màu trắng, nội thất da, bảo dưỡng đầy đủ.");
        sampleCar.setLocation("TP. Hồ Chí Minh");
        sampleCar.setStatus("available");
        sampleCar.setCreatedAt(System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)); // 2 days ago

        // Tạo dữ liệu mẫu cho ảnh theo màu
        List<String> whiteImages = new ArrayList<>();
        whiteImages.add("https://via.placeholder.com/400x300?text=Camry+1");
        whiteImages.add("https://via.placeholder.com/400x300?text=Camry+2");

        Map<String, List<String>> colorImages = new HashMap<>();
        colorImages.put("Trắng", whiteImages);
        sampleCar.setColorImages(colorImages);

        return sampleCar;
    }

    private void setupImageThumbnails() {
        imageUrls = new ArrayList<>();
        List<String> colorList = new ArrayList<>();
        colorQuantities.clear();
        if (car.getColorImages() != null && !car.getColorImages().isEmpty()) {
            for (Map.Entry<String, List<String>> entry : car.getColorImages().entrySet()) {
                colorList.add(entry.getKey());
                // Số lượng thực tế từ database nếu có
                colorQuantities.put(entry.getKey(), car.getQuantity());
            }
            ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colorList);
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerColors.setAdapter(colorAdapter);
            spinnerColors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedColor = colorList.get(position);
                    imageUrls = car.getColorImages().get(selectedColor);
                    if (imageUrls == null || imageUrls.isEmpty()) {
                        imageUrls = Collections.singletonList("https://via.placeholder.com/400x300?text=No+Image");
                    }
                    imageAdapter.updateImages(imageUrls);
                    loadMainImage(imageUrls.get(0));
                    imageAdapter.setSelectedPosition(0);

                    int quantity = colorQuantities.getOrDefault(selectedColor, 0);
                    if (quantity <= 0) {
                        tvQuantity.setText("Hết xe");
                    } else {
                        tvQuantity.setText(quantity + " chiếc");
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
            // Khởi tạo lần đầu
            String firstColor = colorList.get(0);
            imageUrls = car.getColorImages().get(firstColor);
            int quantity = colorQuantities.getOrDefault(firstColor, 0);
            if (quantity <= 0) {
                tvQuantity.setText("Hết xe");
            } else {
                tvQuantity.setText(quantity + " chiếc");
            }
        } else {
            imageUrls = Collections.singletonList("https://via.placeholder.com/400x300?text=No+Image");
            spinnerColors.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Không có màu"}));
            tvQuantity.setText("Hết xe");
        }
        if (imageAdapter == null) {
            imageAdapter = new ImageThumbnailAdapter(imageUrls, position -> {
                currentImageIndex = position;
                loadMainImage(imageUrls.get(position));
                imageAdapter.setSelectedPosition(position);
            });
            rvImageThumbnails.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvImageThumbnails.setAdapter(imageAdapter);
        } else {
            imageAdapter.updateImages(imageUrls);
        }
        loadMainImage(imageUrls.get(0));
        imageAdapter.setSelectedPosition(0);
    }

    private void loadMainImage(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_google)
                .error(R.drawable.ic_google)
                .centerCrop()
                .into(imgMainCar);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFavorite.setOnClickListener(v -> {
            boolean isFavorite = btnFavorite.getTag() != null && (Boolean) btnFavorite.getTag();
            btnFavorite.setTag(!isFavorite);
            Toast.makeText(this, isFavorite ? "Đã xóa khỏi yêu thích" : "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        });
        btnShare.setOnClickListener(v -> shareCar());
        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + PHONE_NUMBER));
            startActivity(intent);
        });
        btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + PHONE_NUMBER));
            intent.putExtra("sms_body", MESSAGE_TEXT);
            startActivity(intent);
        });
        btnReserve.setOnClickListener(v -> {
            Intent intent = new Intent(DetailCarActivity.this, ReserveActivity.class);
            intent.putExtra("carId", car.getId());
            startActivity(intent);
        });
    }

    private void displayCarData() {
        if (car == null) return;
        tvCarTitle.setText(car.getMake() + " " + car.getModel() + " " + car.getYear());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvPrice.setText(formatter.format(car.getPrice()));
        // Hiển thị giá đặt cọc từ database
        tvDepositPrice.setText("Đặt cọc: " + formatter.format(car.getDepositPrice()));
        btnReserve.setText("Đặt cọc ngay - " + formatter.format(car.getDepositPrice()));
        tvLocation.setText(car.getLocation());
        tvCreatedDate.setText(getTimeAgo(car.getCreatedAt()));
        tvStatus.setText(getStatusText(car.getStatus()));
        tvStatus.setBackgroundResource(getStatusBackground(car.getStatus()));
        tvCarType.setText(car.getCarType());
        tvYear.setText(String.valueOf(car.getYear()));
        tvTransmission.setText(car.getTransmission());
        tvFuelType.setText(car.getFuelType());
        tvEngineCapacity.setText(car.getEngineCapacity());
        tvCondition.setText(car.getCondition());
        tvDescription.setText(car.getDescription());
    }

    private String getTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long days = diff / (1000 * 60 * 60 * 24);
        long hours = diff / (1000 * 60 * 60);
        long minutes = diff / (1000 * 60);

        if (days > 0) return days + " ngày trước";
        else if (hours > 0) return hours + " giờ trước";
        else if (minutes > 0) return minutes + " phút trước";
        else return "Vừa xong";
    }

    private String getStatusText(String status) {
        switch (status) {
            case "available": return "Có sẵn";
            case "sold": return "Đã bán";
            case "reserved": return "Đã đặt";
            default: return "Không xác định";
        }
    }

    private int getStatusBackground(String status) {
        switch (status) {
            case "available": return R.drawable.bg_status_available;
            case "sold": return R.drawable.bg_status_sold;
            case "reserved": return R.drawable.bg_status_reserved;
            default: return R.drawable.bg_status_available;
        }
    }

    private void shareCar() {
        String text = "Xe " + car.getMake() + " " + car.getModel() + " " + car.getYear()
                + "\nGiá: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(car.getPrice())
                + "\nĐịa điểm: " + car.getLocation();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ thông tin xe"));
    }
}
