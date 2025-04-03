package com.example.locallim;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BannerActivity extends AppCompatActivity {
    ViewFlipper imgBanner;
    private RecyclerView mRecyclerView;
    private ServiceListAdapter mAdapter;
    private List<ServiceListActivity> mServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_banner);
        imgBanner=findViewById(R.id.imgBanner);

        int sliders[]={
                R.drawable.banner1, R.drawable.banner2, R.drawable.banner3
        };
        for (int image:sliders){
            bannerFlipper(image);
        }
        showListServices();
    }

    public void bannerFlipper(int image){
        ImageView imageView=new ImageView(this);
        imageView.setImageResource(image);
        imgBanner.addView(imageView);
        imgBanner.setFlipInterval(6000);
        imgBanner.setAutoStart(true);
        imgBanner.setInAnimation(this,android.R.anim.fade_in);
        imgBanner.setOutAnimation(this,android.R.anim.fade_out);
    }

    public void showListServices(){
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // Replace LinearLayoutManager with GridLayoutManager
        int numberOfColumns = 2; // Change this to adjust number of columns
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        mServices = new ArrayList<>();

        // Using Firestore instead of Realtime Database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("services")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ServiceListActivity service = document.toObject(ServiceListActivity.class);
                            mServices.add(service);
                            count++;

                            // Debug log
                            System.out.println("Service: " + service.getService_title() +
                                    ", Image URL: " + service.getService_image());
                        }

                        System.out.println("Loaded " + count + " services from Firestore");

                        mAdapter = new ServiceListAdapter(BannerActivity.this, mServices);
                        mRecyclerView.setAdapter(mAdapter);

                        if (count == 0) {
                            System.out.println("No services found in Firestore");
                            Toast.makeText(BannerActivity.this, "No services found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        System.out.println("Error getting documents: " + task.getException());
                        Toast.makeText(BannerActivity.this, "Error loading services", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}