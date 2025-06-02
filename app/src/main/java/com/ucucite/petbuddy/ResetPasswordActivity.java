package com.ucucite.petbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText edtNewPassword, edtConfirmPassword;
    private Button btnSavePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        edtNewPassword = findViewById(R.id.new_password);
        edtConfirmPassword = findViewById(R.id.confirm_password);
        btnSavePassword = findViewById(R.id.save_password_btn);

        btnSavePassword.setOnClickListener(v -> {
            String newPassword = edtNewPassword.getText().toString();
            String confirmPassword = edtConfirmPassword.getText().toString();

            if (TextUtils.isEmpty(newPassword)) {
                edtNewPassword.setError("Enter new password");
                edtNewPassword.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                edtConfirmPassword.setError("Confirm your password");
                edtConfirmPassword.requestFocus();
                return;
            }
            if (newPassword.length() < 6) {
                edtNewPassword.setError("Password must be at least 6 characters");
                edtNewPassword.requestFocus();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                edtConfirmPassword.setError("Passwords do not match");
                edtConfirmPassword.requestFocus();
                return;
            }

            String email = getIntent().getStringExtra("email");
            UserDatabaseHelper dbHelper = new UserDatabaseHelper(this);
            boolean updated = dbHelper.updatePassword(email, newPassword);

            if (updated) {
                Toast.makeText(this, "Password reset successful. Please log in.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, LogIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Error: Could not reset password. Try again.", Toast.LENGTH_LONG).show();
            }
        });
    }
}