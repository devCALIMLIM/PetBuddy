package com.ucucite.petbuddy;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookAppointmentActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Button btnBook;
    private List<Button> timeButtons = new ArrayList<>();
    private String selectedTime = null;
    private long selectedDate = -1;

    private VeterinaryService selectedService;
    private String selectedPetName;

    private AppointmentDatabaseHelper appointmentDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        appointmentDbHelper = new AppointmentDatabaseHelper(this);

        selectedService = (VeterinaryService) getIntent().getSerializableExtra("service");
        selectedPetName = getIntent().getStringExtra("pet_name");

        calendarView = findViewById(R.id.calendarView);
        btnBook = findViewById(R.id.btn_book);

        int[] buttonIds = {
                R.id.time_930, R.id.time_1030,
                R.id.time_1130, R.id.time_330,
                R.id.time_430, R.id.time_530
        };
        final String[] buttonLabels = {
                "9:30 AM", "10:30 AM",
                "11:30 AM", "3:30 PM",
                "4:30 PM", "5:30 PM"
        };

        for (int i = 0; i < buttonIds.length; i++) {
            Button btn = findViewById(buttonIds[i]);
            btn.setText(buttonLabels[i]);
            btn.setOnClickListener(new TimeButtonClickListener(btn, buttonLabels[i]));
            timeButtons.add(btn);
        }

        if (!timeButtons.isEmpty()) {
            timeButtons.get(0).setSelected(true);
            selectedTime = buttonLabels[0];
        }

        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.HOUR_OF_DAY, 0);
        minDate.set(Calendar.MINUTE, 0);
        minDate.set(Calendar.SECOND, 0);
        minDate.set(Calendar.MILLISECOND, 0);

        Calendar maxDate = (Calendar) minDate.clone();
        maxDate.add(Calendar.MONTH, 1);
        maxDate.set(Calendar.DAY_OF_MONTH, maxDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        maxDate.set(Calendar.HOUR_OF_DAY, 23);
        maxDate.set(Calendar.MINUTE, 59);
        maxDate.set(Calendar.SECOND, 59);
        maxDate.set(Calendar.MILLISECOND, 999);

        calendarView.setMinDate(minDate.getTimeInMillis());
        calendarView.setMaxDate(maxDate.getTimeInMillis());
        selectedDate = minDate.getTimeInMillis();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            selectedDate = selected.getTimeInMillis();
            updateTimeButtonsAvailability();
        });

        updateTimeButtonsAvailability();

        btnBook.setOnClickListener(v -> {
            if (selectedDate == -1) {
                Toast.makeText(BookAppointmentActivity.this, "Please select a date.", Toast.LENGTH_SHORT).show();
            } else if (selectedTime == null) {
                Toast.makeText(BookAppointmentActivity.this, "Please select a time.", Toast.LENGTH_SHORT).show();
            } else if (selectedService == null) {
                Toast.makeText(BookAppointmentActivity.this, "Service is not set.", Toast.LENGTH_SHORT).show();
            } else if (selectedPetName == null) {
                Toast.makeText(BookAppointmentActivity.this, "Pet is not set.", Toast.LENGTH_SHORT).show();
            } else {
                String dateString = getFormattedDate(selectedDate) + " " + selectedTime;
                Appointment appointment = new Appointment(
                        selectedPetName,
                        selectedService.type,
                        selectedService.name,
                        dateString,
                        "₱" + selectedService.price,
                        Appointment.PaymentStatus.PENDING,
                        Appointment.Status.PENDING
                );
                showAppointmentPopup(appointment);
            }
        });
    }

    private class TimeButtonClickListener implements View.OnClickListener {
        private final Button button;
        private final String timeLabel;

        TimeButtonClickListener(Button button, String timeLabel) {
            this.button = button;
            this.timeLabel = timeLabel;
        }

        @Override
        public void onClick(View v) {
            if (!button.isEnabled()) return;
            for (Button b : timeButtons) {
                b.setSelected(false);
            }
            button.setSelected(true);
            selectedTime = timeLabel;
        }
    }

    private void updateTimeButtonsAvailability() {
        Calendar today = Calendar.getInstance();
        Calendar selected = Calendar.getInstance();
        selected.setTimeInMillis(selectedDate);

        boolean isToday = today.get(Calendar.YEAR) == selected.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == selected.get(Calendar.DAY_OF_YEAR);

        for (Button btn : timeButtons) {
            btn.setEnabled(true);

            if (isToday) {
                String label = btn.getText().toString();
                String[] parts = label.replace(" AM", "").replace(" PM", "").split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1].replace(" ", ""));

                if (label.contains("PM") && hour != 12) hour += 12;
                if (label.contains("AM") && hour == 12) hour = 0;

                Calendar slot = Calendar.getInstance();
                slot.set(Calendar.HOUR_OF_DAY, hour);
                slot.set(Calendar.MINUTE, minute);
                slot.set(Calendar.SECOND, 0);
                slot.set(Calendar.MILLISECOND, 0);

                if (slot.before(today)) {
                    btn.setEnabled(false);
                    if (btn.isSelected()) {
                        btn.setSelected(false);
                        selectedTime = null;
                    }
                }
            } else {
                btn.setEnabled(true);
            }
        }
    }

    private String getFormattedDate(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return String.format("%02d/%02d/%d", month, day, year);
    }

    private void showAppointmentPopup(Appointment appointment) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_appointment_details);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvPet = dialog.findViewById(R.id.tv_pet);
        TextView tvCareType = dialog.findViewById(R.id.tv_care_type);
        TextView tvService = dialog.findViewById(R.id.tv_service);
        TextView tvDate = dialog.findViewById(R.id.tv_date);
        TextView tvTotal = dialog.findViewById(R.id.tv_total);

        // Set values and labels in the requested order and style
        if (tvPet != null)      tvPet.setText("Pet Name: " + appointment.getPetName());
        if (tvCareType != null) tvCareType.setText("Care Type: " + appointment.getCareTypeDisplay());
        if (tvService != null)  tvService.setText("Service: " + appointment.getServiceName());
        if (tvDate != null)     tvDate.setText("Date & Time: " + appointment.getDateTime());
        if (tvTotal != null)    tvTotal.setText("Total: " + appointment.getTotal());

        Button btnPayNow = dialog.findViewById(R.id.btn_pay_now);
        Button btnPayCounter = dialog.findViewById(R.id.btn_pay_counter);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        btnPayNow.setOnClickListener(v -> {
            Intent intent = new Intent(BookAppointmentActivity.this, PaymentActivity.class);
            intent.putExtra("pet_name", appointment.getPetName());
            intent.putExtra("service_type", appointment.getServiceType());
            intent.putExtra("service_name", appointment.getServiceName());
            intent.putExtra("date_time", appointment.getDateTime());
            intent.putExtra("total", appointment.getTotal());
            intent.putExtra("status", Appointment.Status.PENDING.name());
            intent.putExtra("payment_status", Appointment.PaymentStatus.PAID.name());
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            dialog.dismiss();
            finish();
        });

        btnPayCounter.setOnClickListener(v -> {
            appointment.setPaymentStatus(Appointment.PaymentStatus.UNPAID);
            appointmentDbHelper.insertAppointment(appointment);
            Intent intent = new Intent(BookAppointmentActivity.this, Homepage.class);
            intent.putExtra("pet_name", appointment.getPetName());
            intent.putExtra("service_type", appointment.getServiceType());
            intent.putExtra("service_name", appointment.getServiceName());
            intent.putExtra("date_time", appointment.getDateTime());
            intent.putExtra("total", appointment.getTotal());
            intent.putExtra("status", Appointment.Status.PENDING.name());
            intent.putExtra("payment_status", Appointment.PaymentStatus.UNPAID.name());
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            dialog.dismiss();
            finish();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}