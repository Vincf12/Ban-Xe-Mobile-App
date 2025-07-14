# Tính năng Chọn Vị trí (Location Feature)

## Mô tả
Tính năng cho phép người dùng chọn tỉnh/thành phố từ danh sách 63 tỉnh thành Việt Nam và lưu vào database.

## Các file đã tạo/cập nhật

### 1. Model
- `app/src/main/java/com/example/carsale/Model/Province.java` - Model cho tỉnh thành
- `app/src/main/java/com/example/carsale/Model/User.java` - Đã thêm trường address, gender, age

### 2. API Service
- `app/src/main/java/com/example/carsale/API/ProvinceApiService.java` - Interface cho API tỉnh thành

### 3. Adapter
- `app/src/main/java/com/example/carsale/Adapter/ProvinceAdapter.java` - Adapter cho RecyclerView hiển thị danh sách tỉnh thành

### 4. Activity
- `app/src/main/java/com/example/carsale/LocationActivity.java` - Activity chính cho việc chọn vị trí

### 5. Layout
- `app/src/main/res/layout/activity_location.xml` - Layout cho LocationActivity
- `app/src/main/res/layout/item_province.xml` - Layout cho item tỉnh thành

### 6. Database Helper
- `app/src/main/java/com/example/carsale/Database/FirebaseHelper.java` - Đã thêm methods getUserById() và updateUser()

### 7. Fragment
- `app/src/main/java/com/example/carsale/AccountFragment.java` - Đã cập nhật để xử lý click vào location

### 8. Manifest
- `app/src/main/AndroidManifest.xml` - Đã thêm LocationActivity

### 9. Dependencies
- `app/build.gradle.kts` - Đã thêm Retrofit dependencies

## Cách sử dụng

### 1. Truy cập tính năng
- Vào tab "Account" trong ứng dụng
- Click vào icon edit bên cạnh mục "Vị trí"
- Sẽ chuyển sang màn hình chọn tỉnh/thành phố

### 2. Chọn vị trí
- Sử dụng thanh tìm kiếm để tìm nhanh tỉnh/thành phố
- Click vào tỉnh/thành phố muốn chọn
- Click nút "Lưu" để lưu vị trí

### 3. Kết quả
- Vị trí sẽ được lưu vào Firebase Firestore
- Hiển thị thông báo thành công
- Quay về màn hình Account với vị trí đã cập nhật

## API sử dụng
- **Base URL**: `https://provinces.open-api.vn/api/`
- **Endpoint**: `/provinces`
- **Method**: GET
- **Response**: JSON array chứa thông tin 63 tỉnh thành Việt Nam

## Cấu trúc dữ liệu Province
```json
{
  "code": 1,
  "name": "Hà Nội",
  "name_en": "Ha Noi",
  "full_name": "Thành phố Hà Nội",
  "full_name_en": "Ha Noi City",
  "code_name": "ha_noi",
  "administrative_unit_id": 1,
  "administrative_region_id": 3
}
```

## Lưu ý
- Cần có kết nối internet để tải danh sách tỉnh thành
- Dữ liệu được cache trong bộ nhớ để tìm kiếm nhanh
- Vị trí được lưu theo user ID trong Firebase Firestore
- Hỗ trợ tìm kiếm theo tên tiếng Việt

## Dependencies cần thiết
```kotlin
implementation ("com.squareup.retrofit2:retrofit:2.9.0")
implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
``` 