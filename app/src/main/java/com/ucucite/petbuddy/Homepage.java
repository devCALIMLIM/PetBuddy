package com.ucucite.petbuddy;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.provider.MediaStore;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Homepage extends AppCompatActivity {
    private LinearLayout petsContainer;
    private HorizontalScrollView petsScroll;
    private static final int MAX_PETS = 5;

    private static final int REQ_CODE_PET_CAMERA = 201;
    private static final int REQ_CODE_PET_GALLERY = 202;
    private ImageView currentPetImgView;
    private String currentPhotoPath = null;

    private static final List<Appointment> appointments = new ArrayList<>();
    private RecyclerView appointmentsRecycler;
    private AppointmentAdapter appointmentAdapter;
    private ImageView expandIcon;
    private LinearLayout header;

    private Handler periodicStatusHandler = new Handler(Looper.getMainLooper());
    private Runnable statusChecker;

    private PetDatabaseHelper petDbHelper;
    private AppointmentDatabaseHelper appointmentDbHelper;

    // For passing session info to profile and other screens
    private String loggedUsername, loggedEmail, loggedSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Get session info (from Login or SignUp)
        Intent intent = getIntent();
        loggedUsername = intent.getStringExtra("username");
        loggedEmail = intent.getStringExtra("email");
        loggedSession = intent.getStringExtra("login_user");

        petDbHelper = new PetDatabaseHelper(this);
        appointmentDbHelper = new AppointmentDatabaseHelper(this);

        PetRepository.getInstance().getPetList().clear();
        List<Pet> dbPets = petDbHelper.getAllPets(this);
        for (Pet p : dbPets) PetRepository.getInstance().addPet(p);

        appointments.clear();
        List<Appointment> dbAppointments = appointmentDbHelper.getAllAppointments();
        appointments.addAll(dbAppointments);

        petsContainer = findViewById(R.id.pets_container);
        petsScroll = findViewById(R.id.pets_scroll);

        reloadPetCards();

        Button btnCreatePet = findViewById(R.id.btn_create_pet_profile);
        btnCreatePet.setOnClickListener(v -> {
            if (PetRepository.getInstance().getPetList().size() < MAX_PETS) {
                showAddPetDialog();
            } else {
                Toast.makeText(this, "Maximum of five pets only.", Toast.LENGTH_SHORT).show();
            }
        });

        View navCalendar = findViewById(R.id.bottom_navigation_calendar);
        if (navCalendar != null) {
            navCalendar.setOnClickListener(v -> {
                Intent calendarIntent = new Intent(Homepage.this, VeterinaryActivity.class);
                calendarIntent.putExtra("selectedDate", "2025/06/01");
                // Pass session info
                calendarIntent.putExtra("username", loggedUsername);
                calendarIntent.putExtra("email", loggedEmail);
                calendarIntent.putExtra("login_user", loggedSession);
                startActivity(calendarIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });

            View profBtn = findViewById(R.id.prof);
            if (profBtn != null) {
                profBtn.setOnClickListener(v -> {
                    Intent profileIntent = new Intent(Homepage.this, ProfileActivity.class);
                    // Pass session info for profile display
                    profileIntent.putExtra("username", loggedUsername);
                    profileIntent.putExtra("email", loggedEmail);
                    profileIntent.putExtra("login_user", loggedSession);
                    startActivity(profileIntent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });
            }
        }

        appointmentsRecycler = findViewById(R.id.appointments_recycler);
        appointmentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        appointmentAdapter = new AppointmentAdapter(appointments, (position) -> showAppointmentOptionsDialog(position));
        appointmentsRecycler.setAdapter(appointmentAdapter);

        expandIcon = findViewById(R.id.expand_icon);
        header = findViewById(R.id.appointment_status_header);

        appointmentsRecycler.setVisibility(View.GONE);
        if (expandIcon != null) {
            expandIcon.setImageResource(android.R.drawable.arrow_down_float);
        }
        if (header != null) {
            header.setOnClickListener(v -> {
                ViewGroup card = findViewById(R.id.appointment_status_card);
                TransitionManager.beginDelayedTransition(card, new AutoTransition());
                if (appointmentsRecycler.getVisibility() == View.VISIBLE) {
                    appointmentsRecycler.setVisibility(View.GONE);
                    expandIcon.setImageResource(android.R.drawable.arrow_down_float);
                } else {
                    appointmentsRecycler.setVisibility(View.VISIBLE);
                    expandIcon.setImageResource(android.R.drawable.arrow_up_float);
                }
            });
        }

        View notifBtn = findViewById(R.id.notif);
        if (notifBtn != null) {
            notifBtn.setOnClickListener(v -> {
                Intent notifIntent = new Intent(Homepage.this, NotificationsActivity.class);
                notifIntent.putExtra("username", loggedUsername);
                notifIntent.putExtra("email", loggedEmail);
                notifIntent.putExtra("login_user", loggedSession);
                startActivity(notifIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        updateAllAppointmentStatuses();
        if (appointmentAdapter != null) appointmentAdapter.notifyDataSetChanged();

        statusChecker = new Runnable() {
            @Override
            public void run() {
                updateAllAppointmentStatuses();
                if (appointmentAdapter != null) appointmentAdapter.notifyDataSetChanged();
                periodicStatusHandler.postDelayed(this, 1000);
            }
        };
        periodicStatusHandler.post(statusChecker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        periodicStatusHandler.removeCallbacks(statusChecker);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appointments.clear();
        List<Appointment> dbAppointments = appointmentDbHelper.getAllAppointments();
        appointments.addAll(dbAppointments);
        updateAllAppointmentStatuses();
        if (appointmentAdapter != null) appointmentAdapter.notifyDataSetChanged();
    }

    private void updateAllAppointmentStatuses() {
        long now = System.currentTimeMillis();
        boolean statusChanged = false;
        for (Appointment appt : appointments) {
            if (appt.getStatus() == Appointment.Status.CANCELLED || appt.getStatus() == Appointment.Status.COMPLETED) {
                continue;
            }
            Date apptDate = parseAppointmentDate(appt.getDateTime());
            if (apptDate == null) continue;
            long apptMillis = apptDate.getTime();

            if (appt.getStatus() == Appointment.Status.PENDING) {
                long sinceCreated = now - appt.getCreatedTimeMillis();
                long pendingLimit = (appt.getPendingDurationMillis() > 0) ? appt.getPendingDurationMillis() : 10_000;
                if (sinceCreated > pendingLimit) {
                    appt.setStatus(Appointment.Status.CONFIRMED);
                    if (!appt.wasNotifiedConfirmed()) {
                        String message = "🐾 Your appointment for " + appt.getPetName() + " is confirmed for " + appt.getDateTime() + ". You’ll get a reminder before it starts!";
                        sendStatusNotification("Appointment Confirmed", message);
                        NotificationsActivity.addNotificationStatic(
                                new NotificationItem("Appointment Confirmed", message, System.currentTimeMillis(), R.drawable.ic_bell),
                                getApplicationContext()
                        );
                        appt.setWasNotifiedConfirmed(true);
                        appointmentDbHelper.updateAppointmentNotificationFlags(appt);
                    }
                    long reminderTime = apptMillis - 24 * 60 * 60 * 1000L;
                    if (!appt.wasNotifiedReminder() && reminderTime > now) {
                        scheduleLocalReminder(reminderTime - now, "⏰ Appointment Reminder",
                                "⏰ Reminder: " + appt.getPetName() + " has a confirmed " + appt.getServiceName()
                                        + " appointment tomorrow at " + formatTime(appt.getDateTime()) + ". See you then!");
                        appt.setWasNotifiedReminder(true);
                        appointmentDbHelper.updateAppointmentNotificationFlags(appt);
                    }
                    statusChanged = true;
                    break;
                }
            } else if (appt.getStatus() == Appointment.Status.CONFIRMED) {
                if (now >= apptMillis && now < apptMillis + 60 * 60 * 1000L) {
                    appt.setStatus(Appointment.Status.IN_PROGRESS);
                    if (!appt.wasNotifiedInProgress()) {
                        String message = "✂️ " + appt.getServiceName() + " for " + appt.getPetName() + " is now in progress. We’ll notify you once it’s completed!";
                        sendStatusNotification("In Progress", message);
                        NotificationsActivity.addNotificationStatic(
                                new NotificationItem("In Progress", message, System.currentTimeMillis(), R.drawable.ic_bell),
                                getApplicationContext()
                        );
                        appt.setWasNotifiedInProgress(true);
                        appointmentDbHelper.updateAppointmentNotificationFlags(appt);

                        int mins = randomIntInRange(10, 60);
                        scheduleLocalReminder(mins * 60 * 1000L, "⏳ Still Ongoing",
                                "⏳ " + appt.getPetName() + "'s " + appt.getServiceName() + " is still ongoing. We’ll keep you updated!");
                    }
                    statusChanged = true;
                    break;
                } else if (now >= apptMillis + 60 * 60 * 1000L) {
                    appt.setStatus(Appointment.Status.COMPLETED);
                    if (!appt.wasNotifiedCompleted()) {
                        String message = "✅ " + appt.getPetName() + "’s " + appt.getServiceName() + " is now complete! You can now pick them up or check the report.";
                        sendStatusNotification("Service Completed", message);
                        NotificationsActivity.addNotificationStatic(
                                new NotificationItem("Service Completed", message, System.currentTimeMillis(), R.drawable.ic_bell),
                                getApplicationContext()
                        );
                        appt.setWasNotifiedCompleted(true);
                        appointmentDbHelper.updateAppointmentNotificationFlags(appt);

                        int mins = randomIntInRange(10, 60);
                        scheduleLocalReminder(mins * 60 * 1000L, "⭐ How Was It?",
                                "⭐ Hope " + appt.getPetName() + " enjoyed the visit! Don’t forget to rate the service or book the next one!");
                    }
                    statusChanged = true;
                    break;
                }
            } else if (appt.getStatus() == Appointment.Status.IN_PROGRESS) {
                if (now >= apptMillis + 60 * 60 * 1000L) {
                    appt.setStatus(Appointment.Status.COMPLETED);
                    if (!appt.wasNotifiedCompleted()) {
                        String message = "✅ " + appt.getPetName() + "’s " + appt.getServiceName() + " is now complete! You can now pick them up or check the report.";
                        sendStatusNotification("Service Completed", message);
                        NotificationsActivity.addNotificationStatic(
                                new NotificationItem("Service Completed", message, System.currentTimeMillis(), R.drawable.ic_bell),
                                getApplicationContext()
                        );
                        appt.setWasNotifiedCompleted(true);
                        appointmentDbHelper.updateAppointmentNotificationFlags(appt);

                        int mins = randomIntInRange(10, 60);
                        scheduleLocalReminder(mins * 60 * 1000L, "⭐ How Was It?",
                                "⭐ Hope " + appt.getPetName() + " enjoyed the visit! Don’t forget to rate the service or book the next one!");
                    }
                    statusChanged = true;
                    break;
                }
            }
        }

        // Reminder logic
        if (!statusChanged && appointments.stream().noneMatch(a ->
                a.getStatus() == Appointment.Status.CONFIRMED || a.getStatus() == Appointment.Status.IN_PROGRESS)) {
            // This SharedPreferences usage is only for reminders, not session
            android.content.SharedPreferences prefs = getSharedPreferences("petbuddy", MODE_PRIVATE);
            long lastBookNow = prefs.getLong("last_book_now_reminder", 0);
            long timeNow = System.currentTimeMillis();
            if (timeNow - lastBookNow > 5 * 60 * 1000L) {
                List<Pet> pets = PetRepository.getInstance().getPetList();
                if (!pets.isEmpty()) {
                    Pet pet = pets.get(randomIntInRange(0, pets.size() - 1));
                    showRandomBookNowReminder(pet);
                    prefs.edit().putLong("last_book_now_reminder", timeNow).apply();
                }
            }
        }
    }

    // --- PUSH NOTIFICATION REDIRECTION LOGIC ---
    private void sendStatusNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "petbuddy_status_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Status Updates", NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent;
        if (message.toLowerCase().contains("book now")) {
            intent = new Intent(this, VeterinaryActivity.class);
        } else if (message.toLowerCase().contains("appointment")) {
            intent = new Intent(this, NotificationsActivity.class);
        } else {
            intent = new Intent(this, Homepage.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void scheduleLocalReminder(long delayMillis, String title, String message) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> sendStatusNotification(title, message), delayMillis);
    }

    private int randomIntInRange(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    private void showRandomBookNowReminder(Pet pet) {
        String[] templates = {
                "🐶 \"%s is wondering when their next adventure is… time to book something fun?\"",
                "✨ \"Grooming? Vaccines? Or just a cuddle session? Your pet’s ready when you are!\"",
                "🐾 \"Hey! We miss %s! Ready for their next appointment?\"",
                "🧼 \"Smells like it’s time for a pet spa day… book now and treat %s!\"",
                "🎉 \"Fun fact: pets love routines too. Keep %s happy and healthy — book today!\"",
                "🕐 \"Time flies! Has %s had their monthly checkup yet?\"",
                "🦴 \"Tap here to book a treat-filled day for %s. You know they deserve it!\""
        };
        int idx = randomIntInRange(0, templates.length - 1);
        String message = String.format(templates[idx], pet.name);
        sendStatusNotification("📅 Book Now", message);
        NotificationsActivity.addNotificationStatic(
                new NotificationItem("📅 Book Now", message, System.currentTimeMillis(), R.drawable.ic_bell),
                getApplicationContext()
        );
    }

    private String formatTime(String dateTime) {
        Date date = parseAppointmentDate(dateTime);
        if (date == null) return dateTime;
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    private Date parseAppointmentDate(String dateTime) {
        String[] formats = {
                "MM/dd/yyyy h:mm a",
                "MM/dd/yyyy hh:mm a",
                "M/d/yyyy h:mm a",
                "yyyy-MM-dd HH:mm"
        };
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                return sdf.parse(dateTime);
            } catch (ParseException e) {}
        }
        return null;
    }

    private void showAppointmentOptionsDialog(int index) {
        Appointment appt = appointments.get(index);
        if (appt.getStatus() == Appointment.Status.CANCELLED || appt.getStatus() == Appointment.Status.COMPLETED) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Appointment")
                    .setMessage("Do you want to delete this appointment?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Appointment toRemove = appointments.get(index);
                        appointmentDbHelper.deleteAppointment(toRemove);
                        appointments.remove(index);
                        if (appointmentAdapter != null) appointmentAdapter.notifyItemRemoved(index);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            showCancelDialog(index);
        }
    }

    private void showCancelDialog(int index) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Appointment appt = appointments.get(index);
                    appt.setStatus(Appointment.Status.CANCELLED);
                    appointmentDbHelper.updateAppointmentStatus(appt);
                    if (appointmentAdapter != null) appointmentAdapter.notifyItemChanged(index);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void reloadPetCards() {
        petsContainer.removeAllViews();
        for (Pet pet : PetRepository.getInstance().getPetList()) {
            addPetCard(pet);
        }
    }

    private void showAddPetDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_pet, null);

        ImageView imgPetPhoto = dialogView.findViewById(R.id.img_pet_photo);
        EditText editPetName = dialogView.findViewById(R.id.edit_pet_name);
        EditText editPetType = dialogView.findViewById(R.id.edit_pet_type);
        EditText editPetBreed = dialogView.findViewById(R.id.edit_pet_breed);
        EditText editPetSex = dialogView.findViewById(R.id.edit_pet_sex);
        EditText editPetBirthdate = dialogView.findViewById(R.id.edit_pet_birthdate);
        EditText editPetWeight = dialogView.findViewById(R.id.edit_pet_weight);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm_add_edit_pet);

        imgPetPhoto.setOnClickListener(v -> {
            currentPetImgView = imgPetPhoto;
            new AlertDialog.Builder(this)
                    .setTitle("Change Pet Photo")
                    .setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, REQ_CODE_PET_CAMERA);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, REQ_CODE_PET_GALLERY);
                        }
                    })
                    .show();
        });

        editPetWeight.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String input = s.toString();
                if (!input.isEmpty() && !input.endsWith(" kg")) {
                    String numberOnly = input.replaceAll("[^\\d.]", "");
                    if (!numberOnly.isEmpty()) {
                        editPetWeight.removeTextChangedListener(this);
                        editPetWeight.setText(numberOnly + " kg");
                        editPetWeight.setSelection(editPetWeight.getText().length() - 3);
                        editPetWeight.addTextChangedListener(this);
                    }
                }
            }
        });

        // LESS STRICT birthdate validation: Only show a warning if not in dd/mm/yyyy, but allow any input
        editPetBirthdate.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !editPetBirthdate.getText().toString().trim().isEmpty()) {
                String birthdate = editPetBirthdate.getText().toString().trim();
                if (!birthdate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    editPetBirthdate.setError("Recommended format: dd/mm/yyyy (e.g. 01/12/2025)");
                } else {
                    editPetBirthdate.setError(null);
                }
            } else {
                editPetBirthdate.setError(null);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnConfirm.setOnClickListener(v -> {
            boolean hasError = false;
            String name = editPetName.getText().toString().trim();
            String type = editPetType.getText().toString().trim();
            String breed = editPetBreed.getText().toString().trim();
            String sex = editPetSex.getText().toString().trim();
            String birthdate = editPetBirthdate.getText().toString().trim();
            String weight = editPetWeight.getText().toString().trim();

            if (name.isEmpty() || name.length() < 2) {
                editPetName.setError("Name required (min 2 letters)");
                hasError = true;
            } else {
                editPetName.setError(null);
            }
            if (type.isEmpty()) {
                editPetType.setError("Pet type required");
                hasError = true;
            } else {
                editPetType.setError(null);
            }
            if (breed.isEmpty()) {
                editPetBreed.setError("Breed required");
                hasError = true;
            } else {
                editPetBreed.setError(null);
            }
            if (sex.isEmpty()) {
                editPetSex.setError("Sex required");
                hasError = true;
            } else {
                String sexLower = sex.toLowerCase();
                if (!(sexLower.equals("male") || sexLower.equals("female") || sexLower.equals("m") || sexLower.equals("f"))) {
                    editPetSex.setError("Sex must be 'Male' or 'Female'");
                    hasError = true;
                } else {
                    editPetSex.setError(null);
                }
            }
            // No strict validation for birthdate: allow any non-empty input, just show error as a warning
            if (!birthdate.isEmpty()) {
                if (!birthdate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    editPetBirthdate.setError("Recommended format: dd/mm/yyyy (e.g. 01/12/2025)");
                } else {
                    editPetBirthdate.setError(null);
                }
            } else {
                editPetBirthdate.setError(null);
            }
            if (!weight.isEmpty()) {
                String numberOnly = weight.replaceAll("[^\\d.]", "");
                if (numberOnly.isEmpty()) {
                    editPetWeight.setError("Enter a valid number (e.g. 10)");
                    hasError = true;
                } else {
                    try {
                        float weightVal = Float.parseFloat(numberOnly);
                        if (weightVal <= 0) {
                            editPetWeight.setError("Weight must be positive");
                            hasError = true;
                        } else {
                            editPetWeight.setText(numberOnly + " kg");
                            editPetWeight.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        editPetWeight.setError("Weight must be a number");
                        hasError = true;
                    }
                }
            } else {
                editPetWeight.setError(null);
            }
            if (hasError) return;

            Drawable photo = imgPetPhoto.getDrawable();
            String formattedWeight = weight.isEmpty() ? "" : editPetWeight.getText().toString().replaceAll("[^\\d.]", "") + " kg";
            String photoPath = currentPhotoPath;
            Pet pet = new Pet(photo, photoPath, name, type, breed, sex, birthdate, formattedWeight);
            PetRepository.getInstance().addPet(pet);
            petDbHelper.insertPet(pet);

            currentPhotoPath = null;
            reloadPetCards();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void addPetCard(Pet pet) {
        LinearLayout petCard = new LinearLayout(this);
        petCard.setOrientation(LinearLayout.VERTICAL);
        petCard.setGravity(Gravity.CENTER_HORIZONTAL);
        petCard.setPadding(0, 0, 0, 0);

        ImageView petImg = new ImageView(this);
        if (pet.photoPath != null && !pet.photoPath.isEmpty()) {
            Drawable drawable = Drawable.createFromPath(pet.photoPath);
            petImg.setImageDrawable(drawable != null ? drawable : getDrawable(R.drawable.paw));
        } else {
            petImg.setImageDrawable(getDrawable(R.drawable.paw));
        }
        int size = (int) (72 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(size, size);
        imgParams.gravity = Gravity.CENTER_HORIZONTAL;
        petImg.setLayoutParams(imgParams);
        petImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        petCard.addView(petImg);

        TextView nameTv = new TextView(this);
        nameTv.setText(pet.name);
        nameTv.setTextSize(15f);
        nameTv.setGravity(Gravity.CENTER);
        nameTv.setTypeface(null, Typeface.BOLD);
        nameTv.setPadding(0, 8, 0, 0);
        petCard.addView(nameTv);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(12, 0, 12, 0);
        petCard.setLayoutParams(cardParams);

        petCard.setOnClickListener(cardView -> showPetDetailsDialog(pet, petCard));

        petsContainer.addView(petCard);

        petsScroll.post(() -> petsScroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT));
    }

    private void showPetDetailsDialog(Pet pet, View petCard) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View detailView = inflater.inflate(R.layout.dialog_pet_details, null);

        ImageView imgPetPhoto = detailView.findViewById(R.id.img_pet_photo);
        EditText editPetName = detailView.findViewById(R.id.edit_pet_name);
        EditText editPetType = detailView.findViewById(R.id.edit_pet_type);
        EditText editPetBreed = detailView.findViewById(R.id.edit_pet_breed);
        EditText editPetSex = detailView.findViewById(R.id.edit_pet_sex);
        EditText editPetBirthdate = detailView.findViewById(R.id.edit_pet_birthdate);
        EditText editPetWeight = detailView.findViewById(R.id.edit_pet_weight);
        Button btnDelete = detailView.findViewById(R.id.btn_delete_pet);
        Button btnSave = detailView.findViewById(R.id.btn_confirm_add_edit_pet);

        if (pet.photoPath != null && !pet.photoPath.isEmpty()) {
            Drawable drawable = Drawable.createFromPath(pet.photoPath);
            imgPetPhoto.setImageDrawable(drawable != null ? drawable : getDrawable(R.drawable.paw));
        } else {
            imgPetPhoto.setImageDrawable(getDrawable(R.drawable.paw));
        }
        editPetName.setText(pet.name);
        editPetType.setText(pet.type);
        editPetBreed.setText(pet.breed);
        editPetSex.setText(pet.sex);
        editPetBirthdate.setText(pet.birthdate);
        editPetWeight.setText(pet.weight);

        imgPetPhoto.setOnClickListener(v -> {
            currentPetImgView = imgPetPhoto;
            new AlertDialog.Builder(this)
                    .setTitle("Change Pet Photo")
                    .setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, REQ_CODE_PET_CAMERA);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, REQ_CODE_PET_GALLERY);
                        }
                    })
                    .show();
        });

        editPetWeight.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String input = s.toString();
                if (!input.isEmpty() && !input.endsWith(" kg")) {
                    String numberOnly = input.replaceAll("[^\\d.]", "");
                    if (!numberOnly.isEmpty()) {
                        editPetWeight.removeTextChangedListener(this);
                        editPetWeight.setText(numberOnly + " kg");
                        editPetWeight.setSelection(editPetWeight.getText().length() - 3);
                        editPetWeight.addTextChangedListener(this);
                    }
                }
            }
        });

        // LESS STRICT birthdate validation: Only show a warning if not in yyyy/MM/dd, but allow any input
        editPetBirthdate.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !editPetBirthdate.getText().toString().trim().isEmpty()) {
                String birthdate = editPetBirthdate.getText().toString().trim();
                if (!birthdate.matches("\\d{4}/\\d{2}/\\d{2}")) {
                    editPetBirthdate.setError("Recommended format: yyyy/MM/dd (e.g. 2024/05/27)");
                } else {
                    editPetBirthdate.setError(null);
                }
            } else {
                editPetBirthdate.setError(null);
            }
        });

        AlertDialog detailDialog = new AlertDialog.Builder(this)
                .setView(detailView)
                .setCancelable(true)
                .create();

        btnDelete.setOnClickListener(v -> {
            petsContainer.removeView(petCard);
            PetRepository.getInstance().removePet(pet);
            petDbHelper.deletePetByName(pet.name);
            detailDialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            boolean hasError = false;
            String name = editPetName.getText().toString().trim();
            String type = editPetType.getText().toString().trim();
            String breed = editPetBreed.getText().toString().trim();
            String sex = editPetSex.getText().toString().trim();
            String birthdate = editPetBirthdate.getText().toString().trim();
            String weight = editPetWeight.getText().toString().trim();

            if (name.isEmpty() || name.length() < 2) {
                editPetName.setError("Name required (min 2 letters)");
                hasError = true;
            } else {
                editPetName.setError(null);
            }
            if (type.isEmpty()) {
                editPetType.setError("Pet type required");
                hasError = true;
            } else {
                editPetType.setError(null);
            }
            if (breed.isEmpty()) {
                editPetBreed.setError("Breed required");
                hasError = true;
            } else {
                editPetBreed.setError(null);
            }
            if (sex.isEmpty()) {
                editPetSex.setError("Sex required");
                hasError = true;
            } else {
                String sexLower = sex.toLowerCase();
                if (!(sexLower.equals("male") || sexLower.equals("female") || sexLower.equals("m") || sexLower.equals("f"))) {
                    editPetSex.setError("Sex must be 'Male' or 'Female'");
                    hasError = true;
                } else {
                    editPetSex.setError(null);
                }
            }
            // No strict validation for birthdate: allow any non-empty input, just show error as a warning
            if (!birthdate.isEmpty()) {
                if (!birthdate.matches("\\d{4}/\\d{2}/\\d{2}")) {
                    editPetBirthdate.setError("Recommended format: yyyy/MM/dd (e.g. 2024/05/27)");
                } else {
                    editPetBirthdate.setError(null);
                }
            } else {
                editPetBirthdate.setError(null);
            }
            if (!weight.isEmpty()) {
                String numberOnly = weight.replaceAll("[^\\d.]", "");
                if (numberOnly.isEmpty()) {
                    editPetWeight.setError("Enter a valid number (e.g. 10)");
                    hasError = true;
                } else {
                    try {
                        float weightVal = Float.parseFloat(numberOnly);
                        if (weightVal <= 0) {
                            editPetWeight.setError("Weight must be positive");
                            hasError = true;
                        } else {
                            editPetWeight.setText(numberOnly + " kg");
                            editPetWeight.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        editPetWeight.setError("Weight must be a number");
                        hasError = true;
                    }
                }
            } else {
                editPetWeight.setError(null);
            }
            if (hasError) return;

            Drawable photo = imgPetPhoto.getDrawable();
            String formattedWeight = weight.isEmpty() ? "" : editPetWeight.getText().toString().replaceAll("[^\\d.]", "") + " kg";
            String photoPath = currentPhotoPath != null ? currentPhotoPath : pet.photoPath;
            pet.name = name;
            pet.type = type;
            pet.breed = breed;
            pet.sex = sex;
            pet.birthdate = birthdate;
            pet.weight = formattedWeight;
            pet.photo = photo;
            pet.photoPath = photoPath;

            petDbHelper.updatePet(pet);

            reloadPetCards();
            detailDialog.dismiss();
        });

        detailDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && currentPetImgView != null) {
            if (requestCode == REQ_CODE_PET_CAMERA && data != null && data.getExtras() != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    currentPhotoPath = saveImageToInternalStorage(bitmap);
                    currentPetImgView.setImageBitmap(bitmap);
                }
            } else if (requestCode == REQ_CODE_PET_GALLERY && data != null && data.getData() != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                    if (bitmap != null) {
                        currentPhotoPath = saveImageToInternalStorage(bitmap);
                        currentPetImgView.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        File dir = getFilesDir();
        String fileName = "pet_" + System.currentTimeMillis() + ".png";
        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Prevent activity stacking on back press and show confirmation dialog
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit PetBuddy")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
                .setNegativeButton("No", null)
                .show();
    }
}