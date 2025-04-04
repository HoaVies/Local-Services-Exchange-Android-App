package com.example.locallim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {

    private TextView userNameTextView, userEmailTextView, userContactTextView, servicesCountTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userNameTextView = findViewById(R.id.user_name);
        userEmailTextView = findViewById(R.id.user_email);
        userContactTextView = findViewById(R.id.user_contact);
        servicesCountTextView = findViewById(R.id.services_count);

        setupBottomNavigation();
        loadUserProfile();
        countUserServices();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, BannerActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true; // Already on profile
            } else if (itemId == R.id.nav_my_services) {
                startActivity(new Intent(this, MyServicesActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Reference to the current user in Realtime Database - Note the capital "U" in "Users"
            userRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(currentUser.getUid());

            // Debug line to verify UID
            System.out.println("Checking profile for user ID: " + currentUser.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Debug: Print all data from snapshot
                    System.out.println("DataSnapshot exists: " + dataSnapshot.exists());
                    if (dataSnapshot.exists()) {
                        // Print all children keys and values for debugging
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            System.out.println("Key: " + child.getKey() + ", Value: " + child.getValue());
                        }

                        // Get firstName and lastName
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String lastName = dataSnapshot.child("lastName").getValue(String.class);

                        System.out.println("firstName: " + firstName + ", lastName: " + lastName);

                        // Combine first and last name
                        String fullName = "";
                        if (firstName != null && !firstName.isEmpty()) {
                            fullName += firstName;
                        }
                        if (lastName != null && !lastName.isEmpty()) {
                            if (!fullName.isEmpty()) {
                                fullName += " ";
                            }
                            fullName += lastName;
                        }

                        // Set the full name, or "User" if none available
                        if (!fullName.isEmpty()) {
                            userNameTextView.setText(fullName);
                        } else {
                            userNameTextView.setText("User");
                        }

                        // Set email
                        String email = dataSnapshot.child("email").getValue(String.class);
                        if (email != null && !email.isEmpty()) {
                            userEmailTextView.setText(email);
                        } else {
                            userEmailTextView.setText(currentUser.getEmail());
                        }

                        // Set contact number
                        String contactNo = dataSnapshot.child("contactNo").getValue(String.class);
                        if (contactNo != null && !contactNo.isEmpty()) {
                            userContactTextView.setText(contactNo);
                        } else {
                            userContactTextView.setText("No contact number");
                        }
                    } else {
                        System.out.println("No user data found in database!");
                        userNameTextView.setText("User");
                        userEmailTextView.setText(currentUser.getEmail());
                        userContactTextView.setText("No contact number");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle database error
                    System.out.println("Database Error: " + databaseError.getMessage());
                    userNameTextView.setText("User");
                    userEmailTextView.setText(currentUser.getEmail());
                    userContactTextView.setText("No contact number");
                }
            });
        }
    }

    private void countUserServices() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("services")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                count++;
                            }
                            servicesCountTextView.setText(String.valueOf(count));
                        } else {
                            servicesCountTextView.setText("0");
                        }
                    });
        }
    }
}