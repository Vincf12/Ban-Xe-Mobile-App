package com.example.carsale.Database;

import android.content.Context;
import android.content.Intent;

import com.example.carsale.DangKyActivity;
import com.example.carsale.DangNhapActivity;
import com.example.carsale.Model.User;
import com.example.carsale.R;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface AuthUserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface OnUserDataListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnDataListener {
        void onSuccess();
        void onFailure(String error);
    }


    public void registerUser(String username, String email, String password, AuthCallback callback) {
        // Tạo user với Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser.getUid(), username, email, callback);
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Đăng ký thất bại";
                        callback.onError(error);
                    }
                });
    }

    private void saveUserToFirestore(String uid, String username, String email, AuthCallback callback) {
        User user = new User(username, email);
        user.setId(uid);

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Đăng ký thành công!"))
                .addOnFailureListener(e -> callback.onError("Lỗi lưu dữ liệu: " + e.getMessage()));
    }

    // Khởi tạo Google Sign In
    public void initializeGoogleSignIn(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    // Đăng nhập bằng Email/Password
    public void loginWithEmail(String email, String password, AuthCallback callback) {
        if (email.isEmpty() || password.isEmpty()) {
            callback.onError("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            callback.onSuccess("Đăng nhập thành công!");
                        }
                    } else {
                        String error = getFirebaseAuthError(task.getException());
                        callback.onError(error);
                    }
                });
    }
    // Đăng nhập bằng Google
    public GoogleSignInClient getGoogleSignInClient() {
        return mGoogleSignInClient;
    }

    public void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask, AuthUserCallback callback) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken(), callback);
            }
        } catch (ApiException e) {
            callback.onError("Google Sign In thất bại: " + e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken, AuthUserCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user, callback); // đúng kiểu
                        }
                    } else {
                        callback.onError("Xác thực Google thất bại");
                    }
                });
    }

    // Đăng nhập bằng Facebook
    public void handleFacebookAccessToken(AccessToken token, AuthUserCallback callback) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user, callback); // đúng kiểu
                        }
                    } else {
                        callback.onError("Xác thực Facebook thất bại");
                    }
                });
    }


    // Lưu thông tin user vào Firestore
    private void saveUserToFirestore(FirebaseUser firebaseUser, AuthUserCallback callback) {
        String uid = firebaseUser.getUid();

        // Kiểm tra user đã tồn tại chưa
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Nếu user đã tồn tại, đọc từ Firestore
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            callback.onSuccess(user);
                        } else {
                            callback.onError("Lỗi đọc dữ liệu người dùng");
                        }
                    } else {
                        // Tạo user mới nếu chưa tồn tại
                        User user = new User();
                        user.setId(uid);
                        user.setUsername(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User");
                        user.setEmail(firebaseUser.getEmail());
                        user.setCreatedAt(System.currentTimeMillis());
                        user.setActive(true);
                        user.setAdmin(false); // mặc định không phải admin

                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                                .addOnFailureListener(e -> callback.onError("Lỗi lưu dữ liệu: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onError("Lỗi kiểm tra dữ liệu: " + e.getMessage()));
    }

    // Reset Password
    public void resetPassword(String email, AuthCallback callback) {
        if (email.isEmpty()) {
            callback.onError("Vui lòng nhập email");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Email reset mật khẩu đã được gửi!");
                    } else {
                        callback.onError("Không thể gửi email reset: " +
                                (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"));
                    }
                });
    }

    // Đăng xuất
    public void signOut(Context context) {
        mAuth.signOut();

        // Đăng xuất Google
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut();
        }

        // Đăng xuất Facebook
        LoginManager.getInstance().logOut();
    }

    // Lấy user hiện tại
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // Xử lý lỗi Firebase Auth
    private String getFirebaseAuthError(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    return "Email không hợp lệ";
                case "ERROR_WRONG_PASSWORD":
                    return "Mật khẩu không đúng";
                case "ERROR_USER_NOT_FOUND":
                    return "Tài khoản không tồn tại";
                case "ERROR_USER_DISABLED":
                    return "Tài khoản đã bị vô hiệu hóa";
                case "ERROR_TOO_MANY_REQUESTS":
                    return "Quá nhiều yêu cầu, vui lòng thử lại sau";
                case "ERROR_NETWORK_REQUEST_FAILED":
                    return "Lỗi kết nối mạng";
                default:
                    return "Đăng nhập thất bại: " + exception.getMessage();
            }
        }
        return "Đăng nhập thất bại: " + (exception != null ? exception.getMessage() : "Lỗi không xác định");
    }

    // Lấy thông tin user theo ID
    public void getUserById(String userId, OnUserDataListener listener) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            listener.onSuccess(user);
                        } else {
                            listener.onFailure("Lỗi đọc dữ liệu người dùng");
                        }
                    } else {
                        listener.onFailure("Không tìm thấy người dùng");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure("Lỗi: " + e.getMessage()));
    }

    // Cập nhật thông tin user
    public void updateUser(User user, OnDataListener listener) {
        db.collection("users").document(user.getId()).set(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure("Lỗi cập nhật: " + e.getMessage()));
    }
}
