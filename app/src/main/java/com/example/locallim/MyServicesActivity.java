package com.example.locallim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyServicesActivity extends AppCompatActivity implements ServiceDeleteListener {

    private RecyclerView recyclerView;
    private MyServicesAdapter adapter;
    private List<ServiceListActivity> userServices;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FloatingActionButton fabAddService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_services);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.my_services_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAddService = findViewById(R.id.fab_add_service);
        fabAddService.setOnClickListener(v -> {
            // Navigate to add service activity
            Intent intent = new Intent(MyServicesActivity.this, AddServiceActivity.class);
            startActivity(intent);
        });

        userServices = new ArrayList<>();
        adapter = new MyServicesAdapter(this, userServices, this);
        recyclerView.setAdapter(adapter);

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserServices();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_my_services);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, BannerActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_my_services) {
                return true; // Already on my services
            }

            return false;
        });
    }

    private void loadUserServices() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Query services where the author is the current user
            db.collection("services")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userServices.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ServiceListActivity service = document.toObject(ServiceListActivity.class);
                                service.setId(document.getId()); // Store document ID for delete operation
                                userServices.add(service);
                            }
                            adapter.notifyDataSetChanged();

                            if (userServices.isEmpty()) {
                                Toast.makeText(this, "You haven't added any services yet", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Error loading your services", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onServiceDelete(String serviceId) {
        db.collection("services")
                .document(serviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MyServicesActivity.this, "Service deleted successfully", Toast.LENGTH_SHORT).show();
                    // Reload the list
                    loadUserServices();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyServicesActivity.this, "Failed to delete service", Toast.LENGTH_SHORT).show();
                });
    }
}