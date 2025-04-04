package com.example.locallim;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddServiceActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView serviceImageView;
    private TextInputEditText titleEditText, locationEditText, specificLocationEditText,
            authorEditText, telephoneEditText, descriptionEditText;
    private Button selectImageButton, saveServiceButton;

    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        serviceImageView = findViewById(R.id.service_image);
        titleEditText = findViewById(R.id.et_service_title);
        locationEditText = findViewById(R.id.et_service_location);
        specificLocationEditText = findViewById(R.id.et_specific_location);
        authorEditText = findViewById(R.id.et_author);
        telephoneEditText = findViewById(R.id.et_telephone);
        descriptionEditText = findViewById(R.id.et_description);
        selectImageButton = findViewById(R.id.btn_select_image);
        saveServiceButton = findViewById(R.id.btn_save_service);

        // Set click listeners
        selectImageButton.setOnClickListener(v -> openImagePicker());
        saveServiceButton.setOnClickListener(v -> validateAndSaveService());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {
                // Convert to bitmap using BitmapFactory
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                serviceImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void validateAndSaveService() {

        String title = titleEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String specificLocation = specificLocationEditText.getText().toString().trim();
        String author = authorEditText.getText().toString().trim();
        String telephone = telephoneEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        if (location.isEmpty()) {
            locationEditText.setError("Location is required");
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading service...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Upload image to Firebase Storage
        uploadImage(title, location, specificLocation, author, telephone, description);
    }

    private void uploadImage(String title, String location, String specificLocation,
                             String author, String telephone, String description) {

        String filename = UUID.randomUUID().toString();
        StorageReference storageRef = storage.getReference().child("service_images/" + filename);

        try {

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Compress bitmap
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] data = baos.toByteArray();

            // Upload the compressed image
            storageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Save service data with image URL to Firestore
                            saveServiceToFirestore(title, location, specificLocation,
                                    author, telephone, description, uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(AddServiceActivity.this, "Failed to upload image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveServiceToFirestore(String title, String location, String specificLocation,
                                        String author, String telephone, String description,
                                        String imageUrl) {
        // Create service data map
        Map<String, Object> serviceData = new HashMap<>();
        serviceData.put("service_title", title);
        serviceData.put("service_location", location);
        serviceData.put("service_specific_location", specificLocation);
        serviceData.put("service_author", author);
        serviceData.put("service_telephone", telephone);
        serviceData.put("service_description", description);
        serviceData.put("service_image", imageUrl);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            serviceData.put("user_id", currentUser.getUid());
        }

        db.collection("services")
                .add(serviceData)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddServiceActivity.this, "Service added successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddServiceActivity.this, "Failed to add service: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}