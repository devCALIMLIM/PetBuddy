package com.ucucite.petbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SignUp extends AppCompatActivity {
    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private TextView usernameError, emailError, passwordError, confirmPasswordError, termsError;
    private CheckBox termsCheckBox;
    private Button signupButton;
    private UserDatabaseHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);

        usernameError = findViewById(R.id.username_error);
        emailError = findViewById(R.id.email_error);
        passwordError = findViewById(R.id.password_error);
        confirmPasswordError = findViewById(R.id.confirm_password_error);
        termsError = findViewById(R.id.terms_error);

        termsCheckBox = findViewById(R.id.terms);
        signupButton = findViewById(R.id.signup_button);
        userDbHelper = new UserDatabaseHelper(this);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearErrors();

                String username = usernameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                boolean valid = true;

                // Full Name Validation (non-empty, only letters and spaces)
                if (username.isEmpty()) {
                    usernameError.setText("Full name is required");
                    usernameError.setVisibility(View.VISIBLE);
                    valid = false;
                } else if (!username.matches("^[A-Za-z ]+$")) {
                    usernameError.setText("Full name must only contain letters and spaces");
                    usernameError.setVisibility(View.VISIBLE);
                    valid = false;
                }

                // Email Validation (non-empty, valid format)
                if (email.isEmpty()) {
                    emailError.setText("Email address is required");
                    emailError.setVisibility(View.VISIBLE);
                    valid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError.setText("Please enter a valid email address");
                    emailError.setVisibility(View.VISIBLE);
                    valid = false;
                }

                // Password Validation (non-empty, at least 6 characters)
                if (password.isEmpty()) {
                    passwordError.setText("Password is required");
                    passwordError.setVisibility(View.VISIBLE);
                    valid = false;
                } else if (password.length() < 6) {
                    passwordError.setText("Password must be at least 6 characters");
                    passwordError.setVisibility(View.VISIBLE);
                    valid = false;
                }

                // Confirm Password Validation
                if (confirmPassword.isEmpty()) {
                    confirmPasswordError.setText("Please confirm your password");
                    confirmPasswordError.setVisibility(View.VISIBLE);
                    valid = false;
                } else if (!password.equals(confirmPassword)) {
                    confirmPasswordError.setText("Passwords do not match");
                    confirmPasswordError.setVisibility(View.VISIBLE);
                    valid = false;
                }

                // Terms/Conditions
                if (!termsCheckBox.isChecked()) {
                    termsError.setText("You must accept the terms and conditions");
                    termsError.setVisibility(View.VISIBLE);
                    valid = false;
                }

                if (!valid) return;

                if (userDbHelper.userExists(username, email)) {
                    usernameError.setText("Account already exists with this username or email.");
                    usernameError.setVisibility(View.VISIBLE);
                    emailError.setText("Account already exists with this username or email.");
                    emailError.setVisibility(View.VISIBLE);
                    return;
                }
                boolean success = userDbHelper.addUser(username, email, password);
                if (success) {
                    Intent loginIntent = new Intent(SignUp.this, LogIn.class);
                    loginIntent.putExtra("username", username);
                    loginIntent.putExtra("email", email);
                    startActivity(loginIntent);
                    finish();
                } else {
                    usernameError.setText("Sign Up Failed. Please try again.");
                    usernameError.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void clearErrors() {
        usernameError.setText("");
        usernameError.setVisibility(View.GONE);
        emailError.setText("");
        emailError.setVisibility(View.GONE);
        passwordError.setText("");
        passwordError.setVisibility(View.GONE);
        confirmPasswordError.setText("");
        confirmPasswordError.setVisibility(View.GONE);
        termsError.setText("");
        termsError.setVisibility(View.GONE);
    }
}