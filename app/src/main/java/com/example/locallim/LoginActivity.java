package com.example.locallim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    TextInputLayout userEmailWrapper, userPasswordWrapper;
    EditText userEmail, userPassword;
    Button btnLogin;
    CheckBox rememberMeCheckbox;
    TextView forgotPasswordTextView;
    FirebaseAuth mAuth;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userEmailWrapper = findViewById(R.id.userEmailWrapper);
        userPasswordWrapper = findViewById(R.id.userPasswordWrapper);
        userEmail = findViewById(R.id.userEmail);
        userPassword = findViewById(R.id.userPassword);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        btnLogin = findViewById(R.id.btnUserLogin);

        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences for "Remember Me" functionality
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        if (sharedPreferences.getBoolean(KEY_REMEMBER, false)) {
            userEmail.setText(sharedPreferences.getString(KEY_EMAIL, ""));
            userPassword.setText(sharedPreferences.getString(KEY_PASSWORD, ""));
            rememberMeCheckbox.setChecked(true);
        }

        // Set up "Forgot Password"
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPasswordDialog();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = userEmail.getText().toString().trim();
                String password = userPassword.getText().toString().trim();

                // Validate the inputs
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

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            if (rememberMeCheckbox.isChecked()) {
                                // Save login credentials
                                editor.putString(KEY_EMAIL, email);
                                editor.putString(KEY_PASSWORD, password);
                                editor.putBoolean(KEY_REMEMBER, true);
                            } else {
                                // Clear saved credentials
                                editor.clear();
                            }
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, BannerActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error occurred";
                            Toast.makeText(LoginActivity.this, "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void showForgotPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        final TextInputEditText emailInput = dialogView.findViewById(R.id.reset_email_input);

        // Pre-fill with email if already entered in the login screen
        String currentEmail = userEmail.getText().toString().trim();
        if (!currentEmail.isEmpty()) {
            emailInput.setText(currentEmail);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Enter your email to receive a password reset link")
                .setView(dialogView)
                .setPositiveButton("Send", null) // We'll set this listener later to prevent auto-dismissal
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .create();

        dialog.show();

        // Set positive button click listener after showing dialog to prevent auto-dismissal on validation issues
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                emailInput.setError("Please enter your email address");
                return;
            }

            sendPasswordResetEmail(email, dialog);
        });
    }

    private void sendPasswordResetEmail(String email, AlertDialog dialog) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Failed to send reset email";
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}