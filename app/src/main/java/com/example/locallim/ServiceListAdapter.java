package com.example.locallim;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ServiceListAdapter extends RecyclerView.Adapter<ServiceListAdapter.ImageViewHolder> {
    private Context mContext;
    private List<ServiceListActivity> mServices;  // Use ServiceListActivity instead of Service

    public ServiceListAdapter(Context context, List<ServiceListActivity> services) {
        mContext = context;
        mServices = services;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.service_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ServiceListActivity serviceCur = mServices.get(position);
        holder.ser_Title.setText(serviceCur.getService_title());
        holder.ser_Location.setText(serviceCur.getService_location());
        Picasso.get()
                .load(serviceCur.getService_image())
                .placeholder(R.drawable.img_placeholder)
                .fit()
                .centerCrop()
                .into(holder.ser_Image);  // Load image into ImageView
    }

    @Override
    public int getItemCount() {
        return mServices.size();  // Fix: Return the correct size of the list
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView ser_Title, ser_Location;
        public ImageView ser_Image;  // Change to ImageView for loading images

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ser_Title = itemView.findViewById(R.id.serTitle);
            ser_Image = itemView.findViewById(R.id.serImage);  // This should be an ImageView
            ser_Location = itemView.findViewById(R.id.serLocation);
        }
    }
}
