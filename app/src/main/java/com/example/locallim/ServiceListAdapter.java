package com.example.locallim;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

        // Set default placeholder image
        holder.serImage.setImageResource(R.drawable.img_placeholder);

        // Get image URL from Firestore
        String imageUrl = serviceCur.getService_image();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Use AsyncTask to download and process image
            new LoadImageTask(holder.serImage).execute(imageUrl);
        }
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

                System.out.println("Successfully loaded image with BitmapFactory: " + imageUrl);

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
}