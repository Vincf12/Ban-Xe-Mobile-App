package com.example.carsale.Database;

import com.example.carsale.Model.Sale;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesHelper {
    private static SalesHelper instance;
    private FirebaseFirestore db;
    private static final String COLLECTION_SALES = "sales";

    private SalesHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized SalesHelper getInstance() {
        if (instance == null) {
            instance = new SalesHelper();
        }
        return instance;
    }

    public interface SaleCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface SalesListCallback {
        void onSuccess(List<Sale> sales);
        void onError(String error);
    }

    public interface SaleDetailCallback {
        void onSuccess(Sale sale);
        void onError(String error);
    }

    public void createSale(Sale sale, SaleCallback callback) {
        if (sale == null || sale.getCarId() == null || sale.getCarId().isEmpty()
                || sale.getBuyerId() == null || sale.getBuyerId().isEmpty()
                || sale.getSellerId() == null || sale.getSellerId().isEmpty()
                || sale.getSalePrice() <= 0) {
            callback.onError("Thông tin giao dịch không hợp lệ");
            return;
        }

        sale.setCreatedAt(System.currentTimeMillis());
        sale.setUpdatedAt(System.currentTimeMillis());
        if (sale.getStatus() == null || sale.getStatus().isEmpty()) {
            sale.setStatus("pending");
        }

        db.collection(COLLECTION_SALES)
                .add(sale)
                .addOnSuccessListener(documentReference -> {
                    String saleId = documentReference.getId();
                    sale.setId(saleId);
                    documentReference.update("id", saleId)
                            .addOnSuccessListener(aVoid -> {
                                CarHelper.getInstance().updateCarStatus(sale.getCarId(), "reserved", new CarHelper.CarCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        callback.onSuccess("Tạo giao dịch thành công!");
                                    }

                                    @Override
                                    public void onError(String error) {
                                        callback.onError("Tạo giao dịch thành công nhưng không thể cập nhật trạng thái xe: " + error);
                                    }
                                });
                            })
                            .addOnFailureListener(e -> callback.onError("Lỗi cập nhật ID giao dịch: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Lỗi tạo giao dịch: " + e.getMessage()));
    }

    public void updateSaleStatus(String saleId, String status, SaleCallback callback) {
        if (saleId == null || saleId.isEmpty() || status == null || status.isEmpty()) {
            callback.onError("ID hoặc trạng thái giao dịch không hợp lệ");
            return;
        }

        getSaleById(saleId, new SaleDetailCallback() {
            @Override
            public void onSuccess(Sale sale) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", status);
                updates.put("updatedAt", System.currentTimeMillis());
                if ("completed".equals(status)) {
                    updates.put("completedAt", System.currentTimeMillis());
                }

                db.collection(COLLECTION_SALES).document(saleId).update(updates)
                        .addOnSuccessListener(aVoid -> {
                            String carStatus = getCarStatusFromSaleStatus(status);
                            if (carStatus != null) {
                                CarHelper.getInstance().updateCarStatus(sale.getCarId(), carStatus, new CarHelper.CarCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        callback.onSuccess("Cập nhật trạng thái giao dịch thành công!");
                                    }

                                    @Override
                                    public void onError(String error) {
                                        callback.onSuccess("Cập nhật trạng thái giao dịch thành công!");
                                    }
                                });
                            } else {
                                callback.onSuccess("Cập nhật trạng thái giao dịch thành công!");
                            }
                        })
                        .addOnFailureListener(e -> callback.onError("Lỗi cập nhật trạng thái giao dịch: " + e.getMessage()));
            }

            @Override
            public void onError(String error) {
                callback.onError("Lỗi lấy thông tin giao dịch: " + error);
            }
        });
    }

    private String getCarStatusFromSaleStatus(String saleStatus) {
        switch (saleStatus) {
            case "pending": return "reserved";
            case "completed": return "sold";
            case "cancelled": return "available";
            default: return null;
        }
    }

    public void getSaleById(String saleId, SaleDetailCallback callback) {
        if (saleId == null || saleId.isEmpty()) {
            callback.onError("ID giao dịch không hợp lệ");
            return;
        }

        db.collection(COLLECTION_SALES).document(saleId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Sale sale = doc.toObject(Sale.class);
                        if (sale != null) callback.onSuccess(sale);
                        else callback.onError("Lỗi đọc thông tin giao dịch");
                    } else {
                        callback.onError("Giao dịch không tồn tại");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Lỗi lấy thông tin giao dịch: " + e.getMessage()));
    }

    public void getAllSales(SalesListCallback callback) {
        db.collection(COLLECTION_SALES)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<Sale> sales = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Sale sale = doc.toObject(Sale.class);
                        sales.add(sale);
                    }
                    callback.onSuccess(sales);
                })
                .addOnFailureListener(e -> callback.onError("Lỗi lấy danh sách giao dịch: " + e.getMessage()));
    }

    public void getSalesBySeller(String sellerId, SalesListCallback callback) {
        if (sellerId == null || sellerId.isEmpty()) {
            callback.onError("ID người bán không hợp lệ");
            return;
        }

        db.collection(COLLECTION_SALES)
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<Sale> sales = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Sale sale = doc.toObject(Sale.class);
                        sales.add(sale);
                    }
                    callback.onSuccess(sales);
                })
                .addOnFailureListener(e -> callback.onError("Lỗi lấy danh sách giao dịch: " + e.getMessage()));
    }

    public void getSalesByBuyer(String buyerId, SalesListCallback callback) {
        if (buyerId == null || buyerId.isEmpty()) {
            callback.onError("ID người mua không hợp lệ");
            return;
        }

        db.collection(COLLECTION_SALES)
                .whereEqualTo("buyerId", buyerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<Sale> sales = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Sale sale = doc.toObject(Sale.class);
                        sales.add(sale);
                    }
                    callback.onSuccess(sales);
                })
                .addOnFailureListener(e -> callback.onError("Lỗi lấy danh sách giao dịch: " + e.getMessage()));
    }

    public void getSalesByStatus(String status, SalesListCallback callback) {
        if (status == null || status.isEmpty()) {
            callback.onError("Trạng thái không hợp lệ");
            return;
        }

        db.collection(COLLECTION_SALES)
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<Sale> sales = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Sale sale = doc.toObject(Sale.class);
                        sales.add(sale);
                    }
                    callback.onSuccess(sales);
                })
                .addOnFailureListener(e -> callback.onError("Lỗi lấy danh sách giao dịch: " + e.getMessage()));
    }

    public void deleteSale(String saleId, SaleCallback callback) {
        if (saleId == null || saleId.isEmpty()) {
            callback.onError("ID giao dịch không hợp lệ");
            return;
        }

        getSaleById(saleId, new SaleDetailCallback() {
            @Override
            public void onSuccess(Sale sale) {
                db.collection(COLLECTION_SALES).document(saleId).delete()
                        .addOnSuccessListener(aVoid -> {
                            CarHelper.getInstance().updateCarStatus(sale.getCarId(), "available", new CarHelper.CarCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    callback.onSuccess("Xóa giao dịch thành công!");
                                }

                                @Override
                                public void onError(String error) {
                                    callback.onSuccess("Xóa giao dịch thành công!");
                                }
                            });
                        })
                        .addOnFailureListener(e -> callback.onError("Lỗi xóa giao dịch: " + e.getMessage()));
            }

            @Override
            public void onError(String error) {
                callback.onError("Lỗi lấy thông tin giao dịch: " + error);
            }
        });
    }

    public void getMonthlyRevenue(int year, int month, SalesListCallback callback) {
        long startOfMonth = getStartOfMonth(year, month);
        long endOfMonth = getEndOfMonth(year, month);

        db.collection(COLLECTION_SALES)
                .whereEqualTo("status", "completed")
                .whereGreaterThanOrEqualTo("completedAt", startOfMonth)
                .whereLessThanOrEqualTo("completedAt", endOfMonth)
                .get()
                .addOnSuccessListener(query -> {
                    List<Sale> sales = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Sale sale = doc.toObject(Sale.class);
                        sales.add(sale);
                    }
                    callback.onSuccess(sales);
                })
                .addOnFailureListener(e -> callback.onError("Lỗi thống kê doanh thu: " + e.getMessage()));
    }

    private long getStartOfMonth(int year, int month) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfMonth(int year, int month) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        int lastDay = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        calendar.set(year, month - 1, lastDay, 23, 59, 59);
        calendar.set(java.util.Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}