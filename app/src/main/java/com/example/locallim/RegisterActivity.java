package com.example.locallim;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    EditText uFirstName, uLastName, uEmail, uPassword, uConfPassword, uContactNo;
    Button btnRegister;
    TextInputLayout userFirstNameWrapper, userLastNameWrapper, userEmailWrapper, userPasswordWrapper, userConfPasswordWrapper, userContactNoWrapper;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        uFirstName = findViewById(R.id.userFirstName);
        uLastName = findViewById(R.id.userLastName);
        uEmail = findViewById(R.id.userEmailAddress);
        uPassword = findViewById(R.id.userPassword);
        uConfPassword = findViewById(R.id.userConfPassword);
        uContactNo = findViewById(R.id.userContactNumber);

        userFirstNameWrapper = findViewById(R.id.userFirstNameWrapper);
        userLastNameWrapper = findViewById(R.id.userLastNameWrapper);
        userEmailWrapper = findViewById(R.id.userEmailWrapper);
        userPasswordWrapper = findViewById(R.id.userPasswordWrapper);
        userConfPasswordWrapper = findViewById(R.id.userConfPasswordWrapper);
        userContactNoWrapper = findViewById(R.id.userContactNoWrapper);

        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = uFirstName.getText().toString().trim();
                String lastName = uLastName.getText().toString().trim();
                String email = uEmail.getText().toString().trim();
                String password = uPassword.getText().toString().trim();
                String confPassword = uConfPassword.getText().toString().trim();
                String contactNo = uContactNo.getText().toString().trim();

                // Validate the inputs
                if (firstName.isEmpty()) {
                    userFirstNameWrapper.setError("First name is required");
                    userFirstNameWrapper.requestFocus();
                    return;
                }
                if (lastName.isEmpty()) {
                    userLastNameWrapper.setError("Last name is required");
                    userLastNameWrapper.requestFocus();
                    return;
                }
                if (email.isEmpty()) {
                    userEmailWrapper.setError("Email is required");
                    userEmailWrapper.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    userPasswordWrapper.setError("Password is required");
                    userPasswordWrapper.requestFocus();
                    return;
                }
                if (confPassword.isEmpty()) {
                    userConfPasswordWrapper.setError("Confirm password is required");
                    userConfPasswordWrapper.requestFocus();
                    return;
                }
                if (contactNo.isEmpty()) {
                    userContactNoWrapper.setError("Contact number is required");
                    userContactNoWrapper.requestFocus();
                    return;
                }
                if (!password.equals(confPassword)) {
                    userConfPasswordWrapper.setError("Password does not match");
                    userConfPasswordWrapper.requestFocus();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Add additional user info to Firebase Database
                            User user = new User(firstName, lastName, email, contactNo);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Registration successful, show success message
                                                 Toast.makeText(RegisterActivity.this, "User has been registered successfully", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                            } else {
                                                // Handle any issues with saving additional info in the database
                                                Toast.makeText(RegisterActivity.this, "Error saving user info: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // Registration failed, show error message
                            Exception exception = task.getException();
                            if (exception != null) {
                                Toast.makeText(RegisterActivity.this, "Registration Failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration Failed: Unknown error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }
}
