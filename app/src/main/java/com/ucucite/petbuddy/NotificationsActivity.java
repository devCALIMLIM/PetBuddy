package com.ucucite.petbuddy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class NotificationsActivity extends AppCompatActivity {
    private static NotificationAdapter adapter;
    private static NotificationDatabaseHelper dbHelper;
    private static List<NotificationItem> notifList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);

        dbHelper = new NotificationDatabaseHelper(this);
        notifList = dbHelper.getAllNotifications();

        RecyclerView rv = findViewById(R.id.notifications_recycler);
        adapter = new NotificationAdapter(notifList);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

        Button btnClearAll = findViewById(R.id.btn_clear_all);
        btnClearAll.setOnClickListener(v -> {
            dbHelper.clearAll();
            notifList.clear();
            adapter.notifyDataSetChanged();
        });

        LinearLayout homeLayout = findViewById(R.id.home_layout);
        if (homeLayout != null) {
            homeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationsActivity.this, Homepage.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        LinearLayout bookingLayout = findViewById(R.id.bookings_layout);
        if (bookingLayout != null) {
            bookingLayout.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationsActivity.this, VeterinaryActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        LinearLayout profLayout = findViewById(R.id.profile_layout);
        if (profLayout != null) {
            profLayout.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationsActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NotificationsActivity.this, Homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    // Call this from anywhere (Homepage etc.)
    public static void addNotificationStatic(NotificationItem item, Context context) {
        if (dbHelper == null) {
            dbHelper = new NotificationDatabaseHelper(context.getApplicationContext());
        }
        dbHelper.insertNotification(item);
        notifList.add(0, item);
        if (adapter != null) {
            adapter.notifyItemInserted(0);
        }
    }
}