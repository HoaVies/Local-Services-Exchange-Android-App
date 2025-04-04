package com.example.locallim;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MyServicesAdapter extends RecyclerView.Adapter<MyServicesAdapter.ServiceViewHolder> {

    private Context context;
    private List<ServiceListActivity> services;
    private ServiceDeleteListener deleteListener;

    // Image cache similar to ServiceListAdapter
    private static final LruCache<String, Bitmap> memoryCache;

    static {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public MyServicesAdapter(Context context, List<ServiceListActivity> services, ServiceDeleteListener listener) {
        this.context = context;
        this.services = services;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_service_item, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceListActivity service = services.get(position);

        holder.titleTextView.setText(service.getService_title());
        holder.locationTextView.setText(service.getService_location());

        // Load image like in ServiceListAdapter
        holder.imageView.setImageResource(R.drawable.img_placeholder);

        String imageUrl = service.getService_image();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Bitmap cachedBitmap = getBitmapFromMemoryCache(imageUrl);
            if (cachedBitmap != null) {
                holder.imageView.setImageBitmap(cachedBitmap);
            } else {
                new LoadImageTask(holder.imageView).execute(imageUrl);
            }
        }

        // Setup delete button
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onServiceDelete(service.getId());
            }
        });

        // Setup item click to view details
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ServiceDetailsActivity.class);
            intent.putExtra("title", service.getService_title());
            intent.putExtra("description", service.getService_description());
            intent.putExtra("author", service.getService_author());
            intent.putExtra("location", service.getService_location());
            intent.putExtra("specific_location", service.getService_specific_location());
            intent.putExtra("telephone", service.getService_telephone());
            intent.putExtra("image_url", service.getService_image());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView, locationTextView;
        Button deleteButton;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.my_service_image);
            titleTextView = itemView.findViewById(R.id.my_service_title);
            locationTextView = itemView.findViewById(R.id.my_service_location);
            deleteButton = itemView.findViewById(R.id.btn_delete_service);
        }
    }

    // Helper methods for image cache
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        return memoryCache.get(key);
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
            Bitmap bitmap = null;

            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                if (bitmap != null) {
                    addBitmapToMemoryCache(imageUrl, bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }
}