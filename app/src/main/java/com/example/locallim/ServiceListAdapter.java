package com.example.locallim;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ServiceListAdapter extends RecyclerView.Adapter<ServiceListAdapter.ImageViewHolder> {
    private Context mContext;
    private List<ServiceListActivity> mServices;

    // Image cache
    private static final LruCache<String, Bitmap> memoryCache;

    static {
        // Get max available VM memory, exceeding this amount will throw an OutOfMemory exception
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        return memoryCache.get(key);
    }

    public ServiceListAdapter(Context context, List<ServiceListActivity> services) {
        mContext = context;
        mServices = services;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.service_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ServiceListActivity serviceCur = mServices.get(position);
        holder.serTitle.setText(serviceCur.getService_title());
        holder.serLocation.setText(serviceCur.getService_location());

        holder.serImage.setImageResource(R.drawable.img_placeholder);

        // Get image URL from Firestore
        String imageUrl = serviceCur.getService_image();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Check if image is in cache
            Bitmap cachedBitmap = getBitmapFromMemoryCache(imageUrl);
            if (cachedBitmap != null) {
                holder.serImage.setImageBitmap(cachedBitmap);
            } else {
                // Use AsyncTask to download and process image
                new LoadImageTask(holder.serImage).execute(imageUrl);
            }
        }

        // Set click listener for item
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, ServiceDetailsActivity.class);
            intent.putExtra("title", serviceCur.getService_title());
            intent.putExtra("description", serviceCur.getService_description());
            intent.putExtra("author", serviceCur.getService_author());
            intent.putExtra("location", serviceCur.getService_location());
            intent.putExtra("specific_location", serviceCur.getService_specific_location());
            intent.putExtra("telephone", serviceCur.getService_telephone());
            intent.putExtra("image_url", serviceCur.getService_image());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mServices.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView serImage;
        public TextView serTitle;
        public TextView serLocation;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            serImage = itemView.findViewById(R.id.serImage);
            serTitle = itemView.findViewById(R.id.serTitle);
            serLocation = itemView.findViewById(R.id.serLocation);
        }
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;
        private String imageUrl;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            imageUrl = params[0];

            // Check memory cache first
            Bitmap cachedBitmap = getBitmapFromMemoryCache(imageUrl);
            if (cachedBitmap != null) {
                return cachedBitmap;
            }

            Bitmap bitmap = null;

            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(15000); // 15 seconds timeout
                connection.setReadTimeout(15000);    // 15 seconds timeout
                connection.connect();

                InputStream input = connection.getInputStream();

                // First decode with inJustDecodeBounds=true to check dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, options);
                input.close();

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                input = connection.getInputStream();

                int targetWidth = 240;
                int targetHeight = 160;
                options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;

                bitmap = BitmapFactory.decodeStream(input, null, options);
                input.close();

                if (bitmap != null) {
                    addBitmapToMemoryCache(imageUrl, bitmap);
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error loading image: " + e.getMessage());
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            } else {
                // Keep placeholder if loading fails
                imageView.setImageResource(R.drawable.img_placeholder);
            }
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}