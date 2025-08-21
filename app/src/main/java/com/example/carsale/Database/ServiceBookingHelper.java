package com.example.carsale.Database;

import com.example.carsale.Model.ServiceBooking;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ServiceBookingHelper {
    private static ServiceBookingHelper instance;
    private FirebaseFirestore db;
    private static final String COLLECTION_NAME = "service_bookings";

    private ServiceBookingHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public static ServiceBookingHelper getInstance() {
        if (instance == null) {
            instance = new ServiceBookingHelper();
        }
        return instance;
    }

    // Callback interfaces
    public interface ServiceBookingCallback {
        void onSuccess(ServiceBooking booking);
        void onError(String error);
    }

    public interface ServiceBookingsListCallback {
        void onSuccess(List<ServiceBooking> bookings);
        void onError(String error);
    }

    // Create new service booking
    public void createServiceBooking(ServiceBooking booking, ServiceBookingCallback callback) {
        db.collection(COLLECTION_NAME)
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    booking.setId(documentReference.getId());
                    callback.onSuccess(booking);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Get all service bookings (for admin)
    public void getAllServiceBookings(ServiceBookingsListCallback callback) {
        db.collection(COLLECTION_NAME)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ServiceBooking> bookings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ServiceBooking booking = document.toObject(ServiceBooking.class);
                        if (booking != null) {
                            booking.setId(document.getId());
                            bookings.add(booking);
                        }
                    }
                    callback.onSuccess(bookings);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Get service bookings by status
    public void getServiceBookingsByStatus(String status, ServiceBookingsListCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("status", status)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ServiceBooking> bookings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            ServiceBooking booking = document.toObject(ServiceBooking.class);
                            if (booking != null) {
                                booking.setId(document.getId());
                                bookings.add(booking);
                            }
                        } catch (Exception e) {
                            // Log error but continue processing other documents
                            System.err.println("Error deserializing document " + document.getId() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    callback.onSuccess(bookings);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Get service bookings by user ID
    public void getServiceBookingsByUserId(String userId, ServiceBookingsListCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ServiceBooking> bookings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ServiceBooking booking = document.toObject(ServiceBooking.class);
                        if (booking != null) {
                            booking.setId(document.getId());
                            bookings.add(booking);
                        }
                    }
                    callback.onSuccess(bookings);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Update service booking status
    public void updateServiceBookingStatus(String bookingId, String status, ServiceBookingCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(bookingId)
                .update("status", status, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    // Get updated booking
                    db.collection(COLLECTION_NAME)
                            .document(bookingId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                ServiceBooking booking = documentSnapshot.toObject(ServiceBooking.class);
                                if (booking != null) {
                                    booking.setId(documentSnapshot.getId());
                                    callback.onSuccess(booking);
                                } else {
                                    callback.onError("Booking not found");
                                }
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Delete service booking
    public void deleteServiceBooking(String bookingId, ServiceBookingCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(bookingId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
} 