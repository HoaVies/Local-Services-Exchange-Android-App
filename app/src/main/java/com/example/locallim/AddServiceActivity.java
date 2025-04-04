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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddServiceActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView serviceImageView;
    private TextInputEditText titleEditText, locationEditText, specificLocationEditText,
            authorEditText, telephoneEditText, descriptionEditText;
    private Button selectImageButton, saveServiceButton, selectAppImageButton;

    private Uri imageUri;
    private Bitmap selectedBitmap;
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
        selectAppImageButton = findViewById(R.id.btn_select_app_image);
        saveServiceButton = findViewById(R.id.btn_save_service);

        selectImageButton.setOnClickListener(v -> openImagePicker());
        selectAppImageButton.setOnClickListener(v -> loadCloudImages());
        saveServiceButton.setOnClickListener(v -> validateAndSaveService());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    // Method to fetch images from Firebase storage
    private void loadCloudImages() {
        // Show loading dialog
        ProgressDialog loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Loading available images...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        // Reference to your storage images
        StorageReference imagesRef = storage.getReference().child("selectable_images");

        // List all items in the folder
        imagesRef.listAll()
                .addOnSuccessListener(listResult -> {
                    loadingDialog.dismiss();

                    if (listResult.getItems().isEmpty()) {
                        // If no images found, show message and use fallback to built-in images
                        Toast.makeText(this, "No cloud images available, showing built-in images",
                                Toast.LENGTH_SHORT).show();
                        showBuiltInImageDialog();
                        return;
                    }

                    // Prepare lists for dialog
                    final List<StorageReference> imageRefs = new ArrayList<>();
                    final List<String> imageNames = new ArrayList<>();

                    // Add each image to our lists
                    for (StorageReference item : listResult.getItems()) {
                        imageRefs.add(item);
                        // Use filename as display name
                        String filename = item.getName();
                        imageNames.add(filename);
                    }

                    // Show selection dialog
                    showCloudImageSelectionDialog(imageRefs, imageNames);
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Failed to load cloud images: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // Fallback to built-in images
                    showBuiltInImageDialog();
                });
    }

    private void showCloudImageSelectionDialog(List<StorageReference> imageRefs, List<String> imageNames) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Cloud Image");

        // Convert to array for dialog
        String[] nameArray = imageNames.toArray(new String[0]);

        builder.setItems(nameArray, (dialog, which) -> {
            // Show loading spinner
            ProgressDialog loadingDialog = new ProgressDialog(this);
            loadingDialog.setMessage("Loading image...");
            loadingDialog.setCancelable(false);
            loadingDialog.show();

            // Get the URL for the selected image
            imageRefs.get(which).getDownloadUrl()
                    .addOnSuccessListener(uri -> {

                        imageUri = uri;
                        selectedBitmap = null;

                        // Load the image
                        Glide.with(this)
                                .load(uri)
                                .into(serviceImageView);

                        loadingDialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Failed to load image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });

        builder.show();
    }

    // Fallback method to select built-in images
    private void showBuiltInImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Built-in Image");

        // Add your built-in images to the drawable folder first
        final int[] imageResources = {
                R.drawable.img_placeholder,
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3
        };

        final String[] imageNames = {
                "Placeholder Image",
                "Banner Image 1",
                "Banner Image 2",
                "Banner Image 3"
        };

        builder.setItems(imageNames, (dialog, which) -> {
            // Use BitmapFactory to decode the resource into a Bitmap
            selectedBitmap = BitmapFactory.decodeResource(getResources(), imageResources[which]);

            serviceImageView.setImageBitmap(selectedBitmap);
            imageUri = null;
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            selectedBitmap = null;

            try {
                // Convert to bitmap using MediaStore for display in ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                serviceImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void validateAndSaveService() {
        // Get text values
        String title = titleEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String specificLocation = specificLocationEditText.getText().toString().trim();
        String author = authorEditText.getText().toString().trim();
        String telephone = telephoneEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        // Validate inputs
        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        if (location.isEmpty()) {
            locationEditText.setError("Location is required");
            return;
        }

        // Check if either imageUri OR selectedBitmap is available
        if (imageUri == null && selectedBitmap == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading service...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // If the imageUri is from Firebase Storage
        if (imageUri != null && imageUri.toString().startsWith("https://firebasestorage.googleapis.com")) {
            // Skip upload and use existing URL
            saveServiceToFirestore(title, location, specificLocation, author, telephone, description, imageUri.toString());
            return;
        }

        uploadImage(title, location, specificLocation, author, telephone, description);
    }

    private void uploadImage(String title, String location, String specificLocation,
                             String author, String telephone, String description) {

        String filename = UUID.randomUUID().toString();
        StorageReference storageRef = storage.getReference().child("service_images/" + filename);

        try {
            Bitmap bitmap;

            if (imageUri != null) {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } else {
                bitmap = selectedBitmap;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] data = baos.toByteArray();

            // Upload image
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

        // Store the user ID
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