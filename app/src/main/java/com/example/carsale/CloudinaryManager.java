package com.example.carsale;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CloudinaryManager {

    private static final String CLOUD_NAME = "dk9j2tsbq";
    private static final String TAG = "Cloudinary";

    private OkHttpClient client;

    public CloudinaryManager() {
        client = new OkHttpClient();
    }

    public void uploadImage(File file, UploadCallback callback) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("image/*")))
                .addFormDataPart("upload_preset", "image_img") // preset đúng
                .build();

        String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resStr = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(resStr);
                        String secureUrl = jsonObject.getString("secure_url");
                        Log.d(TAG, "Secure URL: " + secureUrl);
                        callback.onSuccess(secureUrl);
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                } else {
                    callback.onFailure(new Exception("Error code: " + response.code()));
                }
            }
        });
    }

    public static void displayImage(Context context, String imageUrl, ImageView imageView) {
        Glide.with(context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(imageView);
    }

    public interface UploadCallback {
        void onSuccess(String response);
        void onFailure(Exception e);
    }
}
