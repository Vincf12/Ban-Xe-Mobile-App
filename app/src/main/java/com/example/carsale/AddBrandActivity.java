package com.example.carsale;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddBrandActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etCarMakeName;
    private ImageView imgLogoPreview;
    private Button btnSelectLogo, btnSaveMake;

    private Uri selectedLogoUri = null;
    private String savedLogoPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_brand);

        etCarMakeName = findViewById(R.id.etCarMakeName);
        imgLogoPreview = findViewById(R.id.imgLogoPreview);
        btnSelectLogo = findViewById(R.id.btnSelectLogo);
        btnSaveMake = findViewById(R.id.btnSaveMake);

        btnSelectLogo.setOnClickListener(v -> openImagePicker());

        btnSaveMake.setOnClickListener(v -> {
            String name = etCarMakeName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Vui lòng nhập tên hãng", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedLogoUri == null) {
                Toast.makeText(this, "Vui lòng chọn logo", Toast.LENGTH_SHORT).show();
                return;
            }

            saveLogoLocallyAndSaveMake(name);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn logo"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedLogoUri = data.getData();
            imgLogoPreview.setImageURI(selectedLogoUri);
        }
    }

    private void saveLogoLocallyAndSaveMake(String name) {
        try {
            File dir = new File(getFilesDir(), "car_make_logos");
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID().toString() + ".jpg";
            File file = new File(dir, fileName);

            InputStream inputStream = getContentResolver().openInputStream(selectedLogoUri);
            if (inputStream == null) throw new IOException("Không thể mở ảnh");

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            savedLogoPath = file.getAbsolutePath();
            saveToFirestore(name, savedLogoPath);

        } catch (IOException e) {
            Toast.makeText(this, "Lỗi lưu ảnh cục bộ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToFirestore(String name, String logoPath) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> make = new HashMap<>();
        make.put("name", name);
        make.put("logoPath", logoPath);  // Đường dẫn local

        db.collection("car_makes")
                .add(make)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Thêm hãng thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lưu Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
