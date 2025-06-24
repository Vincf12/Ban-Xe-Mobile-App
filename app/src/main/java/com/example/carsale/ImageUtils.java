package com.example.carsale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class ImageUtils {
    public static Bitmap loadImageFromPath(String imagePath) {
        try {
            return BitmapFactory.decodeFile(imagePath);
        } catch (Exception e) {
            Log.e("ImageUtils", "Lỗi tải ảnh từ: " + imagePath, e);
            return null;
        }
    }

    public static void displayImage(Context context, String imagePath, ImageView imageView) {
        Bitmap bitmap = loadImageFromPath(imagePath);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(android.R.drawable.ic_dialog_alert); // Hình mặc định khi lỗi
        }
    }
}
