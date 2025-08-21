package com.example.carsale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.carsale.Model.Payment;
import com.example.carsale.Model.Car;
import com.example.carsale.Model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentConfirmFragment extends Fragment {
    private LinearLayout layoutPayments;
    private FirebaseFirestore db;
    private TextView tabAll, tabPending, tabCompleted, tvPendingBadge;
    private String currentFilter = "all"; // all, pending, completed

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_payment_confirm, container, false);
        layoutPayments = view.findViewById(R.id.layoutPayments);
        db = FirebaseFirestore.getInstance();
        
        // Khởi tạo tabs
        initTabs(view);
        
        // Load dữ liệu ban đầu
        loadPayments();
        
        // Cập nhật badge sau khi tabs đã được khởi tạo
        // Delay một chút để tránh flash
        tvPendingBadge.post(() -> updatePendingBadge());
        return view;
    }

    private void initTabs(View view) {
        tabAll = view.findViewById(R.id.tabAll);
        tabPending = view.findViewById(R.id.tabPending);
        tabCompleted = view.findViewById(R.id.tabCompleted);
        tvPendingBadge = view.findViewById(R.id.tvPendingBadge);
        
        // Set click listeners
        tabAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateTabSelection();
            loadPayments();
        });
        
        tabPending.setOnClickListener(v -> {
            currentFilter = "pending";
            updateTabSelection();
            loadPayments();
        });
        
        tabCompleted.setOnClickListener(v -> {
            currentFilter = "completed";
            updateTabSelection();
            loadPayments();
        });
        
        // Set tab mặc định
        updateTabSelection();
    }
    
    private void updatePendingBadge() {
        if (getContext() == null) return;
        
        // Không thay đổi visibility ngay lập tức
        // Chỉ cập nhật text và visibility khi có kết quả
        db.collection("payments")
                .whereEqualTo("confirmed", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (getContext() == null) return;
                    
                    int pendingCount = querySnapshot.size();
                    if (pendingCount > 0) {
                        tvPendingBadge.setText(pendingCount + " chờ xử lý");
                        // Chỉ hiển thị nếu trước đó đang ẩn
                        if (tvPendingBadge.getVisibility() != View.VISIBLE) {
                            tvPendingBadge.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // Chỉ ẩn nếu trước đó đang hiển thị
                        if (tvPendingBadge.getVisibility() != View.GONE) {
                            tvPendingBadge.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        // Chỉ ẩn nếu trước đó đang hiển thị
                        if (tvPendingBadge.getVisibility() != View.GONE) {
                            tvPendingBadge.setVisibility(View.GONE);
                        }
                    }
                });
    }
    
    private void updateTabSelection() {
        if (getContext() == null) return;
        
        // Reset tất cả tabs
        tabAll.setBackgroundResource(R.drawable.tab_unselected);
        tabPending.setBackgroundResource(R.drawable.tab_unselected);
        tabCompleted.setBackgroundResource(R.drawable.tab_unselected);
        
        tabAll.setTextColor(getResources().getColor(R.color.secondary_text_color));
        tabPending.setTextColor(getResources().getColor(R.color.secondary_text_color));
        tabCompleted.setTextColor(getResources().getColor(R.color.secondary_text_color));
        
        // Set tab được chọn
        switch (currentFilter) {
            case "all":
                tabAll.setBackgroundResource(R.drawable.tab_selected);
                tabAll.setTextColor(getResources().getColor(R.color.primary_color));
                break;
            case "pending":
                tabPending.setBackgroundResource(R.drawable.tab_selected);
                tabPending.setTextColor(getResources().getColor(R.color.primary_color));
                break;
            case "completed":
                tabCompleted.setBackgroundResource(R.drawable.tab_selected);
                tabCompleted.setTextColor(getResources().getColor(R.color.primary_color));
                break;
        }
    }

    private void loadPayments() {
        if (getContext() == null) return;
        
        // Không hiển thị loading state ngay lập tức
        // Chỉ xóa views cũ
        layoutPayments.removeAllViews();
        
        // Tạo query dựa trên filter
        com.google.firebase.firestore.Query query = db.collection("payments");
        
        switch (currentFilter) {
            case "pending":
                query = query.whereEqualTo("confirmed", false);
                break;
            case "completed":
                query = query.whereEqualTo("confirmed", true);
                break;
            case "all":
            default:
                // Không filter, lấy tất cả
                break;
        }
        
        query.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (getContext() == null) return;
                    
                    // Xóa loading state nếu có
                    layoutPayments.removeAllViews();
                    
                    if (querySnapshot.isEmpty()) {
                        TextView emptyText = new TextView(getContext());
                        String emptyMessage = "";
                        switch (currentFilter) {
                            case "pending":
                                emptyMessage = "Không có thanh toán nào chờ xác nhận";
                                break;
                            case "completed":
                                emptyMessage = "Không có thanh toán nào đã hoàn thành";
                                break;
                            default:
                                emptyMessage = "Không có thanh toán nào";
                                break;
                        }
                        emptyText.setText(emptyMessage);
                        emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        emptyText.setPadding(0, 50, 0, 50);
                        layoutPayments.addView(emptyText);
                        return;
                    }
                    
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Payment payment = doc.toObject(Payment.class);
                        if (payment != null) {
                            addPaymentView(payment, doc.getId());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;
                    
                    // Chỉ hiển thị lỗi nếu layout còn trống
                    if (layoutPayments.getChildCount() == 0) {
                        TextView errorText = new TextView(getContext());
                        errorText.setText("Lỗi tải danh sách thanh toán: " + e.getMessage());
                        errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        errorText.setPadding(0, 50, 0, 50);
                        layoutPayments.addView(errorText);
                    }
                    Toast.makeText(getContext(), "Lỗi tải danh sách thanh toán", Toast.LENGTH_SHORT).show();
                });
    }

    private void addPaymentView(Payment payment, String paymentId) {
        if (getContext() == null) return;
        
        View view = getLayoutInflater().inflate(R.layout.item_payment_confirm, layoutPayments, false);
        
        // Tìm các TextView trong layout
        TextView tvUser = view.findViewById(R.id.tvUser);
        TextView tvDateTime = view.findViewById(R.id.tvDateTime);
        TextView tvCar = view.findViewById(R.id.tvCar);
        TextView tvAmount = view.findViewById(R.id.tvAmount);
        TextView tvContent = view.findViewById(R.id.tvContent);
        TextView tvStatus = view.findViewById(R.id.tvStatus);
        TextView tvUserAvatar = view.findViewById(R.id.tvUserAvatar);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        Button btnDecline = view.findViewById(R.id.btnDecline);

        // Set default values
        tvUser.setText("Đang tải...");
        tvCar.setText("Đang tải...");
        tvContent.setText(payment.getContent() != null ? payment.getContent() : "Không có ghi chú");
        tvStatus.setText("Chờ xác nhận");
        
        // Format thời gian
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
            String dateTime = sdf.format(new Date(payment.getTimestamp()));
            tvDateTime.setText(dateTime);
        } catch (Exception e) {
            tvDateTime.setText("N/A");
        }
        
        // Format số tiền
        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedAmount = formatter.format(payment.getAmount());
            tvAmount.setText(formattedAmount);
        } catch (Exception e) {
            tvAmount.setText("0 VNĐ");
        }
        
        // Load thông tin user
        loadUserInfo(payment.getUserId(), tvUser, tvUserAvatar);
        
        // Load thông tin xe
        loadCarInfo(payment.getCarId(), tvCar);
        
        // Ẩn/hiện buttons dựa trên trạng thái thanh toán
        if (payment.isConfirmed()) {
            btnConfirm.setVisibility(View.GONE);
            btnDecline.setVisibility(View.GONE);
            tvStatus.setText("Đã xác nhận");
        } else {
            btnConfirm.setVisibility(View.VISIBLE);
            btnDecline.setVisibility(View.VISIBLE);
            tvStatus.setText("Chờ xác nhận");
            
            // Click listeners với loading state
            btnConfirm.setOnClickListener(v -> {
                btnConfirm.setEnabled(false);
                btnConfirm.setText("Đang xử lý...");
                confirmPayment(paymentId);
            });
            
            btnDecline.setOnClickListener(v -> {
                btnDecline.setEnabled(false);
                btnDecline.setText("Đang xử lý...");
                declinePayment(paymentId);
            });
        }
        
        layoutPayments.addView(view);
    }

    private void loadUserInfo(String userId, TextView tvUser, TextView tvUserAvatar) {
        if (getContext() == null || userId == null) {
            tvUser.setText("Người dùng");
            tvUserAvatar.setText("U");
            return;
        }
        
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getContext() == null) return;
                    
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            String displayName = user.getFullname() != null ? user.getFullname() : 
                                               (user.getUsername() != null ? user.getUsername() : "Người dùng");
                            tvUser.setText(displayName);
                            if (!displayName.isEmpty()) {
                                tvUserAvatar.setText(displayName.substring(0, 1).toUpperCase());
                            } else {
                                tvUserAvatar.setText("U");
                            }
                        } else {
                            tvUser.setText("Người dùng");
                            tvUserAvatar.setText("U");
                        }
                    } else {
                        tvUser.setText("Người dùng");
                        tvUserAvatar.setText("U");
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        tvUser.setText("Người dùng");
                        tvUserAvatar.setText("U");
                    }
                });
    }

    private void loadCarInfo(String carId, TextView tvCar) {
        if (getContext() == null || carId == null) {
            tvCar.setText("Thông tin xe");
            return;
        }
        
        db.collection("cars").document(carId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getContext() == null) return;
                    
                    if (documentSnapshot.exists()) {
                        Car car = documentSnapshot.toObject(Car.class);
                        if (car != null) {
                            StringBuilder carInfo = new StringBuilder();
                            if (car.getMake() != null) carInfo.append(car.getMake());
                            if (car.getModel() != null) carInfo.append(" ").append(car.getModel());
                            if (car.getYear() != null) carInfo.append(" - ").append(car.getYear());
                            
                            String result = carInfo.toString().trim();
                            tvCar.setText(result.isEmpty() ? "Thông tin xe" : result);
                        } else {
                            tvCar.setText("Thông tin xe");
                        }
                    } else {
                        tvCar.setText("Thông tin xe");
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        tvCar.setText("Thông tin xe");
                    }
                });
    }

    private void confirmPayment(String paymentId) {
        if (getContext() == null || paymentId == null) return;
        
        db.collection("payments").document(paymentId)
                .update("confirmed", true)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Đã xác nhận thanh toán!", Toast.LENGTH_SHORT).show();
                        loadPayments();
                        updatePendingBadge(); // Cập nhật badge sau khi xác nhận
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi xác nhận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadPayments(); // Reload để reset button states
                    }
                });
    }

    private void declinePayment(String paymentId) {
        if (getContext() == null || paymentId == null) return;
        
        db.collection("payments").document(paymentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Đã từ chối thanh toán!", Toast.LENGTH_SHORT).show();
                        loadPayments();
                        updatePendingBadge(); // Cập nhật badge sau khi từ chối
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi từ chối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadPayments(); // Reload để reset button states
                    }
                });
    }
} 