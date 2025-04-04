package com.example.locallim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BannerActivity extends AppCompatActivity {
    ViewFlipper imgBanner;
    private RecyclerView mRecyclerView;
    private ServiceListAdapter mAdapter;
    private List<ServiceListActivity> mServices;
    private FloatingActionButton fabAddService;
    private boolean initialLoadDone = false; // Track if initial load has occurred

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_banner);
        imgBanner = findViewById(R.id.imgBanner);
        fabAddService = findViewById(R.id.fab_add_service);

        int sliders[] = {
                R.drawable.banner1, R.drawable.banner2, R.drawable.banner3
        };
        for (int image : sliders) {
            bannerFlipper(image);
        }

        // Set click listener for FAB
        fabAddService.setOnClickListener(view -> {
            Intent intent = new Intent(BannerActivity.this, AddServiceActivity.class);
            startActivity(intent);
        });

        initRecyclerView();

        showListServices();

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only refresh the services list if we've already done the initial load
        if (initialLoadDone) {
            showListServices();
        }
    }
    private void initRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        int numberOfColumns = 2; // Change this to adjust number of columns
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        // Initialize the list and adapter
        mServices = new ArrayList<>();
        mAdapter = new ServiceListAdapter(BannerActivity.this, mServices);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void bannerFlipper(int image){
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(image);
        imgBanner.addView(imageView);
        imgBanner.setFlipInterval(6000);
        imgBanner.setAutoStart(true);
        imgBanner.setInAnimation(this, android.R.anim.fade_in);
        imgBanner.setOutAnimation(this, android.R.anim.fade_out);
    }

    public void showListServices(){
        mServices.clear();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("services")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ServiceListActivity service = document.toObject(ServiceListActivity.class);
                            service.setId(document.getId()); // Store document ID
                            mServices.add(service);
                            count++;

                            // Debug log
                            System.out.println("Service: " + service.getService_title() +
                                    ", Image URL: " + service.getService_image());
                        }

                        System.out.println("Loaded " + count + " services from Firestone");

                        // Notify adapter of data changes
                        mAdapter.notifyDataSetChanged();

                        // Set flag that initial load is complete
                        initialLoadDone = true;

                        if (count == 0) {
                            System.out.println("No services found in Firestone");
                            Toast.makeText(BannerActivity.this, "No services found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        System.out.println("Error getting documents: " + task.getException());
                        Toast.makeText(BannerActivity.this, "Error loading services", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true; // Already on home
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_my_services) {
                startActivity(new Intent(this, MyServicesActivity.class));
                return true;
            }

            return false;
        });
    }
}