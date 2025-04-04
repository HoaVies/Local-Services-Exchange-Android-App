package com.example.locallim;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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

        // Load image with Glide - simple one-line solution
        String imageUrl = serviceCur.getService_image();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(mContext)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Skip disk cache as requested
                    .into(holder.serImage);
        } else {
            holder.serImage.setImageResource(R.drawable.img_placeholder);
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
}