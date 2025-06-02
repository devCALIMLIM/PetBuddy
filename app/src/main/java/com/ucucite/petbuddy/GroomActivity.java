package com.ucucite.petbuddy;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class GroomActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groom);

        // --- GROOMING PRODUCTS BOOK NOW BUTTONS (IDs: 4, 5, 6) ---
        LinearLayout book4 = findViewById(R.id.book4);
        if (book4 != null) {
            book4.setOnClickListener(v -> showPetSelectionDialog("Basic Groom", 600));
        }

        LinearLayout book5 = findViewById(R.id.book5);
        if (book5 != null) {
            book5.setOnClickListener(v -> showPetSelectionDialog("Deluxe Groom", 900));
        }

        LinearLayout book6 = findViewById(R.id.book6);
        if (book6 != null) {
            book6.setOnClickListener(v -> showPetSelectionDialog("Spa Groom", 1300));
        }

        // --- TOP NAVIGATION ---
        LinearLayout homeLayout = findViewById(R.id.home_layout);
        if (homeLayout != null) {
            homeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(GroomActivity.this, Homepage.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        LinearLayout vetLayout = findViewById(R.id.to_vet);
        if (vetLayout != null) {
            vetLayout.setOnClickListener(v -> {
                Intent intent = new Intent(GroomActivity.this, VeterinaryActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        LinearLayout boardingLayout = findViewById(R.id.to_boarding1);
        if (boardingLayout != null) {
            boardingLayout.setOnClickListener(v -> {
                Intent intent = new Intent(GroomActivity.this, BoardingActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        LinearLayout notifLayout = findViewById(R.id.notif);
        if (notifLayout != null) {
            notifLayout.setOnClickListener(v -> {
                Intent intent = new Intent(GroomActivity.this, NotificationsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        LinearLayout profLayout = findViewById(R.id.prof);
        if (profLayout != null) {
            profLayout.setOnClickListener(v -> {
                Intent intent = new Intent(GroomActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void showPetSelectionDialog(String serviceName, int price) {
        List<Pet> petList = PetRepository.getInstance().getPetList();
        if (petList.isEmpty()) {
            Toast.makeText(this, "No Pets Available. Please Add a Pet Profile first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_pet);

        ListView listView = dialog.findViewById(R.id.list_pets);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_pet);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm_pet);

        String[] petNames = new String[petList.size()];
        for (int i = 0; i < petList.size(); i++) {
            petNames[i] = petList.get(i).name;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, petNames);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(0, true); // Default selection

        final int[] selectedIdx = {0};
        listView.setOnItemClickListener((parent, view, position, id) -> selectedIdx[0] = position);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            VeterinaryService service = new VeterinaryService(serviceName, price, "Grooming");
            Intent intent = new Intent(GroomActivity.this, BookAppointmentActivity.class);
            intent.putExtra("service", service);
            intent.putExtra("pet_name", petNames[selectedIdx[0]]);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        // Go to Homepage with animation instead of default back
        Intent intent = new Intent(GroomActivity.this, Homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        // Optionally finish() if you don't want GroomActivity in the stack
        finish();
    }
}