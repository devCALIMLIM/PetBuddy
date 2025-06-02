package com.ucucite.petbuddy;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText emailInput;
    private Button sendCodeBtn;
    private String sentOtp;
    private UserDatabaseHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailInput = findViewById(R.id.forgot_email);
        sendCodeBtn = findViewById(R.id.send_verification_btn);
        userDbHelper = new UserDatabaseHelper(this);

        sendCodeBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                emailInput.setError("Email required");
                emailInput.requestFocus();
                return;
            }
            if (!userDbHelper.emailExists(email)) {
                Toast.makeText(this, "No account found with this email.", Toast.LENGTH_SHORT).show();
                return;
            }
            showOtpDialog(email);
        });
    }

    private void showOtpDialog(String email) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_verifocation_code);

        EditText otpEdit = dialog.findViewById(R.id.otp_code);
        Button verifyBtn = dialog.findViewById(R.id.verify_btn);

        sentOtp = String.format(Locale.US, "%06d", new Random().nextInt(1000000));
        int delayMillis = (3 + new Random().nextInt(18)) * 1000;
        new Handler().postDelayed(() -> otpEdit.setText(sentOtp), delayMillis);

        verifyBtn.setOnClickListener(v -> {
            String entered = otpEdit.getText().toString().trim();
            if (entered.equals(sentOtp)) {
                Toast.makeText(this, "Verified! You may now reset your password.", Toast.LENGTH_LONG).show();
                dialog.dismiss();
                Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid or incomplete code.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
        Toast.makeText(this, "Verification code sent to: " + email + "\n(Use: " + sentOtp + ")", Toast.LENGTH_LONG).show();
    }
}