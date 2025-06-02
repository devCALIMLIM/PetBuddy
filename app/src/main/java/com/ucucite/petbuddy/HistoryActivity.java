package com.ucucite.petbuddy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView historyRecycler;
    private AppointmentHistoryAdapter historyAdapter;
    private AppointmentDatabaseHelper dbHelper;
    private LinearLayout emptyView;
    private Button clearBtn;
    private ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyRecycler = findViewById(R.id.recycler_history);
        emptyView = findViewById(R.id.empty_history);
        clearBtn = findViewById(R.id.clear_history_btn);
        backBtn = findViewById(R.id.back_btn);
        dbHelper = new AppointmentDatabaseHelper(this);

        loadHistory();

        clearBtn.setOnClickListener(v -> {
            if (historyAdapter.getItemCount() == 0) return;
            new AlertDialog.Builder(this)
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to clear all appointment history?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        dbHelper.clearAllHistory(); // Implement this in your db helper!
                        loadHistory();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        backBtn.setOnClickListener(v -> finish());
    }

    private void loadHistory() {
        List<Appointment> allAppointments = dbHelper.getAllAppointments(); // Should return all history, empty if none
        if (historyAdapter == null) {
            historyAdapter = new AppointmentHistoryAdapter(new ArrayList<>(allAppointments));
            historyRecycler.setLayoutManager(new LinearLayoutManager(this));
            historyRecycler.setAdapter(historyAdapter);
        } else {
            historyAdapter.setAppointments(allAppointments);
            historyAdapter.notifyDataSetChanged();
        }

        if (allAppointments.isEmpty()) {
            historyRecycler.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            historyRecycler.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}