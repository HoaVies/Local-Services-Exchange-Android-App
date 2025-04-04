package com.example.locallim;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServiceDetailsActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView titleTextView, descriptionTextView, authorTextView,
            locationTextView, specificLocationTextView, telephoneTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_details);

        // Initialize views
        imageView = findViewById(R.id.detail_image);
        titleTextView = findViewById(R.id.detail_title);
        descriptionTextView = findViewById(R.id.detail_description);
        authorTextView = findViewById(R.id.detail_author);
        locationTextView = findViewById(R.id.detail_location);
        specificLocationTextView = findViewById(R.id.detail_specific_location);
        telephoneTextView = findViewById(R.id.detail_telephone);

        // Get data from intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String author = getIntent().getStringExtra("author");
        String location = getIntent().getStringExtra("location");
        String specificLocation = getIntent().getStringExtra("specific_location");
        String telephone = getIntent().getStringExtra("telephone");
        String imageUrl = getIntent().getStringExtra("image_url");

        // Set text data
        titleTextView.setText(title);
        descriptionTextView.setText(description);
        authorTextView.setText("Author: " + author);
        locationTextView.setText("Location: " + location);
        specificLocationTextView.setText("Address: " + specificLocation);
        telephoneTextView.setText("Tel: " + telephone);

        // Load image using BitmapFactory
        if (imageUrl != null && !imageUrl.isEmpty()) {
            new LoadImageTask(imageView).execute(imageUrl);
        } else {
            imageView.setImageResource(R.drawable.img_placeholder);
        }
    }

    // AsyncTask to load images using BitmapFactory
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            Bitmap bitmap = null;

            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();

                // Using BitmapFactory to decode the input stream
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                bitmap = BitmapFactory.decodeStream(input, null, options);
                input.close();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ServiceDetailsActivity.this,
                            "Error loading image", Toast.LENGTH_SHORT).show();
                });
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            } else {
                imageView.setImageResource(R.drawable.img_placeholder);
            }
        }
    }
}