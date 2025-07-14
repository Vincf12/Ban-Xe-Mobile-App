# Banner ViewPager Feature

## Mô tả
Tính năng banner ViewPager cho phép hiển thị 2-3 ảnh banner có thể kéo qua lại với các tính năng:

- **Auto-scroll**: Tự động chuyển banner mỗi 3 giây
- **Manual scroll**: Người dùng có thể kéo qua lại bằng tay
- **Indicator dots**: Hiển thị vị trí banner hiện tại
- **Click handling**: Xử lý sự kiện click vào banner
- **Infinite scroll**: Cuộn vô hạn (loop)

## Cấu trúc file

### 1. BannerAdapter.java
- Adapter cho ViewPager2
- Quản lý danh sách ảnh banner và text overlay
- Xử lý sự kiện click

### 2. item_banner.xml
- Layout cho mỗi item banner
- Bao gồm ImageView và text overlay

### 3. fragment_home.xml
- Chứa ViewPager2 và indicator dots
- Được cập nhật để hỗ trợ banner

### 4. HomeFragment.java
- Khởi tạo và quản lý banner ViewPager
- Xử lý auto-scroll và indicator updates

## Cách sử dụng

### Thêm ảnh banner mới:
1. Thêm ảnh vào thư mục `res/drawable/`
2. Cập nhật danh sách `bannerImages` trong `HomeFragment.java`:

```java
private final List<Integer> bannerImages = Arrays.asList(
    R.drawable.banner_1,
    R.drawable.banner_2,
    R.drawable.banner_3,
    R.drawable.banner_4  // Thêm ảnh mới
);
```

### Thay đổi text banner:
Cập nhật trong `BannerAdapter.java`:

```java
this.bannerTitles = Arrays.asList(
    "Tiêu đề 1",
    "Tiêu đề 2", 
    "Tiêu đề 3"
);

this.bannerSubtitles = Arrays.asList(
    "Mô tả 1",
    "Mô tả 2",
    "Mô tả 3"
);
```

### Thay đổi thời gian auto-scroll:
Cập nhật trong `HomeFragment.java`:

```java
handler.postDelayed(this, 5000); // 5 giây thay vì 3 giây
```

## Tính năng

### Auto-scroll
- Tự động chuyển banner mỗi 3 giây
- Dừng khi fragment bị destroy

### Manual scroll
- Người dùng có thể kéo qua lại bằng tay
- Hỗ trợ infinite scroll

### Indicator dots
- Hiển thị vị trí banner hiện tại
- Tự động cập nhật khi scroll

### Click handling
- Xử lý sự kiện click vào banner
- Có thể mở detail page hoặc thực hiện action khác

## Tùy chỉnh

### Thay đổi style indicator:
Cập nhật trong `fragment_home.xml`:

```xml
<ImageView
    android:id="@+id/dot_1"
    android:layout_width="12dp"  <!-- Thay đổi kích thước -->
    android:layout_height="12dp"
    android:backgroundTint="@color/your_color"  <!-- Thay đổi màu -->
    android:alpha="1.0" />
```

### Thay đổi layout banner:
Cập nhật trong `item_banner.xml`:

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_gravity="center"  <!-- Thay đổi vị trí text -->
    android:orientation="vertical">
```

## Lưu ý

1. **Performance**: Sử dụng `Integer.MAX_VALUE` cho infinite scroll có thể ảnh hưởng performance với số lượng lớn
2. **Memory**: Cleanup adapter trong `onDestroyView()` để tránh memory leak
3. **Images**: Sử dụng ảnh có kích thước phù hợp để tránh lag
4. **Testing**: Test trên nhiều thiết bị khác nhau để đảm bảo responsive 