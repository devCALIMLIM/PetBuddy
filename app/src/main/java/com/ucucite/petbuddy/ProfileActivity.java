package com.ucucite.petbuddy;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgUserAvatar;
    private TextView txtUserName, txtUserEmail;
    private String backTarget = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get dynamic back target ("Home", "Booking", "Notifications", etc.)
        backTarget = getIntent().getStringExtra("back_target");
        Log.d("ProfileBack", "backTarget = " + backTarget);

        imgUserAvatar = findViewById(R.id.imgUserAvatar);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserEmail = findViewById(R.id.txtUserEmail);

        // --- Persistent session logic ---
        // Try getting user info from intent
        String fullName = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String loginUser = getIntent().getStringExtra("login_user");

        SharedPreferences prefs = getSharedPreferences("petbuddy_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (loginUser != null && !loginUser.isEmpty()) {
            // Save info for future sessions
            editor.putString("username", fullName != null ? fullName : "");
            editor.putString("email", email != null ? email : "");
            editor.putString("login_user", loginUser);
            editor.apply();
        } else {
            // Load from SharedPreferences if intent is missing data
            fullName = prefs.getString("username", "");
            email = prefs.getString("email", "");
            loginUser = prefs.getString("login_user", "");
        }

        // Redirect to login if not logged in
        if (loginUser == null || loginUser.isEmpty()) {
            Intent intent = new Intent(ProfileActivity.this, LogIn.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        if (fullName != null && !fullName.isEmpty()) {
            txtUserName.setText(fullName);
        }
        if (email != null && !email.isEmpty()) {
            txtUserEmail.setText(email);
        }

        // History click
        LinearLayout btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, HistoryActivity.class)));

        LinearLayout bookingLayout = findViewById(R.id.bookings_layout);
        if (bookingLayout != null) {
            bookingLayout.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, VeterinaryActivity.class);
                intent.putExtra("back_target", "Booking");
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        LinearLayout notifLayout = findViewById(R.id.notifications_layout);
        if (notifLayout != null) {
            notifLayout.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, NotificationsActivity.class);
                intent.putExtra("back_target", "Profile");
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        LinearLayout homeLayout = findViewById(R.id.home_layout);
        if (homeLayout != null) {
            homeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, Homepage.class);
                intent.putExtra("back_target", "Home");
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Sign out logic
        TextView txtSignOut = findViewById(R.id.txtSignOut);
        txtSignOut.setOnClickListener(v -> {
            SharedPreferences.Editor logoutEditor = prefs.edit();
            logoutEditor.clear();
            logoutEditor.apply();
            Intent intent = new Intent(ProfileActivity.this, LogIn.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Dynamically determine where to go back based on the selected nav
        Intent intent;
        if ("Booking".equals(backTarget)) {
            intent = new Intent(ProfileActivity.this, VeterinaryActivity.class);
        } else if ("Notifications".equals(backTarget)) {
            intent = new Intent(ProfileActivity.this, NotificationsActivity.class);
        } else {
            intent = new Intent(ProfileActivity.this, Homepage.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("back_target", backTarget);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}