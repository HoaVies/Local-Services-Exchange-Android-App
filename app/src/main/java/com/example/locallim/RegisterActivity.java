package com.example.locallim;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    EditText uFirstName, uLastName, uEmail, uPassword, uConfPassword, uContactNo;
    Button btnRegister;
    TextInputLayout userFirstNameWrapper, userLastNameWrapper, userEmailWrapper, userPasswordWrapper, userConfPasswordWrapper, userContactNoWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        // Handle the register button click
        btnRegister.setOnClickListener(view -> {
            // Retrieve the input values
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

            // If all validations pass, proceed with registration
        });
    }
}
