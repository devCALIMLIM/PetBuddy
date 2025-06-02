package com.ucucite.petbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class LogIn extends AppCompatActivity {
    private EditText edtUsername, edtPassword;
    private TextView emailError, passwordError;
    private UserDatabaseHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

        edtUsername = findViewById(R.id.email);
        edtPassword = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);

        emailError = findViewById(R.id.email_error);
        passwordError = findViewById(R.id.password_error);

        TextView signUpText = findViewById(R.id.sign_up);
        TextView forgotPasswordText = findViewById(R.id.forgot_password);

        userDbHelper = new UserDatabaseHelper(this);

        // Hide errors at the beginning
        emailError.setText("");
        emailError.setVisibility(View.GONE);
        passwordError.setText("");
        passwordError.setVisibility(View.GONE);

        loginButton.setOnClickListener(v -> {
            String usernameOrEmail = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString();
            boolean valid = true;

            // Reset errors
            emailError.setText("");
            emailError.setVisibility(View.GONE);
            passwordError.setText("");
            passwordError.setVisibility(View.GONE);

            if (usernameOrEmail.isEmpty()) {
                emailError.setText("Email or username is required");
                emailError.setVisibility(View.VISIBLE);
                valid = false;
            }
            if (password.isEmpty()) {
                passwordError.setText("Password is required");
                passwordError.setVisibility(View.VISIBLE);
                valid = false;
            }

            if (valid) {
                if (userDbHelper.validateUser(usernameOrEmail, password)) {
                    // Fetch user info
                    String username = userDbHelper.getUsernameByEmailOrUsername(usernameOrEmail);
                    String email = userDbHelper.getEmailByEmailOrUsername(usernameOrEmail);

                    // Go to Homepage after login, passing user info and for session, pass login_user
                    Intent intent = new Intent(LogIn.this, Homepage.class);
                    intent.putExtra("username", username);
                    intent.putExtra("email", email);
                    intent.putExtra("login_user", usernameOrEmail); // For session passing
                    startActivity(intent);
                    finish();
                } else {
                    // Show error below password
                    passwordError.setText("Incorrect email or password.");
                    passwordError.setVisibility(View.VISIBLE);
                }
            }
        });

        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUp.class);
            startActivity(intent);
        });

        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}