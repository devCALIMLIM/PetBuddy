package com.ucucite.petbuddy;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VeterinaryActivity extends AppCompatActivity {
    private Map<String, VeterinaryService> serviceMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veterinary);

        // Initialize service map
        serviceMap = new HashMap<>();
        serviceMap.put("Distemper", new VeterinaryService("Distemper", 700, "Veterinary"));
        serviceMap.put("Calicivirus", new VeterinaryService("Calicivirus", 500, "Veterinary"));
        serviceMap.put("Rabies Vaccine", new VeterinaryService("Rabies Vaccine", 400, "Veterinary"));

        // Book Now for Distemper
        LinearLayout book1 = findViewById(R.id.book1);
        if (book1 != null) {
            book1.setOnClickListener(v -> showPetSelectionDialog(serviceMap.get("Distemper")));
        }

        // Book Now for Calicivirus
        LinearLayout book2 = findViewById(R.id.book2);
        if (book2 != null) {
            book2.setOnClickListener(v -> showPetSelectionDialog(serviceMap.get("Calicivirus")));
        }

        // Book Now for Rabies
        LinearLayout book3 = findViewById(R.id.book3);
        if (book3 != null) {
            book3.setOnClickListener(v -> showPetSelectionDialog(serviceMap.get("Rabies Vaccine")));
        }

        // Navigation buttons
        View homeBtn = findViewById(R.id.home_layout);
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, Homepage.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        View groomBtn = findViewById(R.id.to_groom);
        if (groomBtn != null) {
            groomBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, GroomActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        View boardingBtn = findViewById(R.id.to_boarding);
        if (boardingBtn != null) {
            boardingBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, BoardingActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        View notifBtn = findViewById(R.id.notif);
        if (notifBtn != null) {
            notifBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        View profBtn = findViewById(R.id.prof);
        if (profBtn != null) {
            profBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void showPetSelectionDialog(VeterinaryService service) {
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
        listView.setItemChecked(0, true);

        final int[] selectedIdx = {0};
        listView.setOnItemClickListener((parent, view, position, id) -> selectedIdx[0] = position);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            Pet selectedPet = petList.get(selectedIdx[0]);
            Intent intent = new Intent(VeterinaryActivity.this, BookAppointmentActivity.class);
            intent.putExtra("pet_name", selectedPet.name);     // send pet name
            intent.putExtra("service", service);               // send service with all fields
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        // Go to Homepage with fade animation instead of default back
        Intent intent = new Intent(VeterinaryActivity.this, Homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}