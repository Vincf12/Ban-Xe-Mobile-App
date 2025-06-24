package com.example.carsale.Database;

import android.util.Log;

import com.example.carsale.Model.Car;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class để xử lý các thao tác với collection cars trong Firestore
 */
public class CarHelper {
    private static CarHelper instance;
    private FirebaseFirestore db;
    private static final String COLLECTION_CARS = "cars";

    /**
     * Constructor private để thực hiện pattern Singleton
     */
    private CarHelper() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Lấy instance duy nhất của CarHelper
     * @return instance của CarHelper
     */
    public static synchronized CarHelper getInstance() {
        if (instance == null) {
            instance = new CarHelper();
        }
        return instance;
    }

    /**
     * Interface callback cho các thao tác cơ bản
     */
    public interface CarCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * Interface callback cho việc lấy danh sách xe
     */
    public interface CarsListCallback {
        void onSuccess(List<Car> cars);
        void onError(String error);
    }

    /**
     * Interface callback cho việc lấy thông tin một xe
     */
    public interface CarDetailCallback {
        void onSuccess(Car car);
        void onError(String error);
    }

    /**
     * Thêm xe mới vào database
     * @param car Thông tin xe cần thêm
     * @param callback Callback để xử lý kết quả
     */
    public void addCar(Car car, CarCallback callback) {
        if (car == null) {
            callback.onError("Thông tin xe không hợp lệ");
            return;
        }

        // Validate dữ liệu
        if (car.getMake() == null || car.getMake().isEmpty()) {
            callback.onError("Vui lòng nhập hãng xe");
            return;
        }
        if (car.getModel() == null || car.getModel().isEmpty()) {
            callback.onError("Vui lòng nhập mẫu xe");
            return;
        }
        if (car.getPrice() <= 0) {
            callback.onError("Giá xe phải lớn hơn 0");
            return;
        }
        if (car.getYear() <= 1900 || car.getYear() > 2030) {
            callback.onError("Năm sản xuất không hợp lệ");
            return;
        }

        // Thiết lập thời gian tạo
        car.setCreatedAt(System.currentTimeMillis());
        car.setUpdatedAt(System.currentTimeMillis());

        // Thêm vào Firestore
        db.collection(COLLECTION_CARS)
                .add(car)
                .addOnSuccessListener(documentReference -> {
                    // Cập nhật ID cho xe
                    String carId = documentReference.getId();
                    car.setId(carId);

                    // Cập nhật document với ID
                    documentReference.update("id", carId)
                            .addOnSuccessListener(aVoid -> callback.onSuccess("Thêm xe thành công!"))
                            .addOnFailureListener(e -> callback.onError("Lỗi cập nhật ID xe: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Lỗi thêm xe: " + e.getMessage()));
    }

    /**
     * Cập nhật thông tin xe
     * @param car Thông tin xe đã được cập nhật
     * @param callback Callback để xử lý kết quả
     */
    public void updateCar(Car car, CarCallback callback) {
        if (car == null || car.getId() == null || car.getId().isEmpty()) {
            callback.onError("Thông tin xe không hợp lệ");
            return;
        }

        // Validate dữ liệu
        if (car.getMake() == null || car.getMake().isEmpty()) {
            callback.onError("Vui lòng nhập hãng xe");
            return;
        }
        if (car.getModel() == null || car.getModel().isEmpty()) {
            callback.onError("Vui lòng nhập mẫu xe");
            return;
        }
        if (car.getPrice() <= 0) {
            callback.onError("Giá xe phải lớn hơn 0");
            return;
        }

        // Cập nhật thời gian sửa đổi
        car.setUpdatedAt(System.currentTimeMillis());

        // Cập nhật trong Firestore
        db.collection(COLLECTION_CARS)
                .document(car.getId())
                .set(car)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Cập nhật xe thành công!"))
                .addOnFailureListener(e -> callback.onError("Lỗi cập nhật xe: " + e.getMessage()));
    }

    /**
     * Xóa xe khỏi database
     * @param carId ID của xe cần xóa
     * @param callback Callback để xử lý kết quả
     */
    public void deleteCar(String carId, List<String> imagePaths, CarCallback callback) {
        if (carId == null || carId.isEmpty()) {
            callback.onError("ID xe không hợp lệ");
            return;
        }

        db.collection(COLLECTION_CARS)
                .document(carId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa các file ảnh cục bộ
                    if (imagePaths != null) {
                        for (String path : imagePaths) {
                            java.io.File file = new java.io.File(path);
                            if (file.exists()) {
                                if (file.delete()) {
                                    Log.d("CarHelper", "Xóa file ảnh thành công: " + path);
                                } else {
                                    Log.w("CarHelper", "Không thể xóa file ảnh: " + path);
                                }
                            }
                        }
                    }
                    callback.onSuccess("Xóa xe thành công!");
                })
                .addOnFailureListener(e -> callback.onError("Lỗi xóa xe: " + e.getMessage()));
    }

    /**
     * Lấy danh sách tất cả xe
     * @param callback Callback để xử lý kết quả
     */
    public void getAllCars(CarsListCallback callback) {
        db.collection(COLLECTION_CARS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> cars = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Car car = document.toObject(Car.class);
                        cars.add(car);
                    }
                    callback.onSuccess(cars);
                })
                .addOnFailureListener(e -> callback.onError("Lỗi lấy danh sách xe: " + e.getMessage()));
    }

    /**
     * Lấy danh sách xe có sẵn (trạng thái available)
     * @param callback Callback để xử lý kết quả
     */
    public void getAvailableCars(CarsListCallback callback) {
        db.collection(COLLECTION_CARS)
                .whereEqualTo("status", "available")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> cars = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Car car = document.toObject(Car.class);
                        cars.add(car);
                    }
                    callback.onSuccess(cars);
                })
                .addOnFailureListener(e -> callback.onError("Lỗi lấy danh sách xe: " + e.getMessage()));
    }

    /**
     * Lấy thông tin chi tiết một xe
     * @param carId ID của xe
     * @param callback Callback để xử lý kết quả
     */
    public void getCarById(String carId, CarDetailCallback callback) {
        if (carId == null || carId.isEmpty()) {
            callback.onError("ID xe không hợp lệ");
            return;
        }

        db.collection(COLLECTION_CARS)
                .document(carId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Car car = documentSnapshot.toObject(Car.class);
                        if (car != null) {
                            callback.onSuccess(car);
                        } else {
                            callback.onError("Lỗi đọc thông tin xe");
                        }
                    } else {
                        callback.onError("Xe không tồn tại");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Lỗi lấy thông tin xe: " + e.getMessage()));
    }

    /**
     * Tìm kiếm xe theo hãng
     * @param make Hãng xe
     * @param callback Callback để xử lý kết quả
     */
    public void searchCarsByMake(String make, CarsListCallback callback) {
        if (make == null || make.isEmpty()) {
            callback.onError("Vui lòng nhập hãng xe");
            return;
        }

        db.collection(COLLECTION_CARS)
                .whereEqualTo("make", make)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> cars = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Car car = document.toObject(Car.class);
                        cars.add(car);
                    }
                    callback.onSuccess(cars);
                })
                .addOnFailureListener(e -> callback.onError("Lỗi tìm kiếm xe: " + e.getMessage()));
    }

    /**
     * Tìm kiếm xe theo khoảng giá
     * @param minPrice Giá tối thiểu
     * @param maxPrice Giá tối đa
     * @param callback Callback để xử lý kết quả
     */
    public void searchCarsByPriceRange(double minPrice, double maxPrice, CarsListCallback callback) {
        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            callback.onError("Khoảng giá không hợp lệ");
            return;
        }

        db.collection(COLLECTION_CARS)
                .whereGreaterThanOrEqualTo("price", minPrice)
                .whereLessThanOrEqualTo("price", maxPrice)
                .orderBy("price", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> cars = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Car car = document.toObject(Car.class);
                        cars.add(car);
                    }
                    callback.onSuccess(cars);
                })
                .addOnFailureListener(e -> callback.onError("Lỗi tìm kiếm xe: " + e.getMessage()));
    }

    /**
     * Cập nhật trạng thái xe
     * @param carId ID xe
     * @param status Trạng thái mới (available, sold, reserved)
     * @param callback Callback để xử lý kết quả
     */
    public void updateCarStatus(String carId, String status, CarCallback callback) {
        if (carId == null || carId.isEmpty()) {
            callback.onError("ID xe không hợp lệ");
            return;
        }

        if (status == null || status.isEmpty()) {
            callback.onError("Trạng thái không hợp lệ");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection(COLLECTION_CARS)
                .document(carId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Cập nhật trạng thái thành công!"))
                .addOnFailureListener(e -> callback.onError("Lỗi cập nhật trạng thái: " + e.getMessage()));
    }

    /**
     * Lấy danh sách xe theo trạng thái
     * @param status Trạng thái xe
     * @param callback Callback để xử lý kết quả
     */
    public void getCarsByStatus(String status, CarsListCallback callback) {
        if (status == null || status.isEmpty()) {
            callback.onError("Trạng thái không hợp lệ");
            return;
        }

        db.collection(COLLECTION_CARS)
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> cars = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Car car = document.toObject(Car.class);
                        cars.add(car);
                    }
                    callback.onSuccess(cars);
                })
                .addOnFailureListener(e -> callback.onError("Lỗi lấy danh sách xe: " + e.getMessage()));
    }
}