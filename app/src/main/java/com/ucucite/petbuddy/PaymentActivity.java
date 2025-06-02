package com.ucucite.petbuddy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.UUID;

public class PaymentActivity extends AppCompatActivity {

    EditText edtCardNumber, edtCardName, edtCardCVC, edtExpMonth, edtExpYear;
    TextView txtTransAmount, txtTransRef;
    Button btnConfirmPayment;
    ImageView cardVisa, cardMC, cardAmex;

    String selectedPaymentType = "";

    Appointment appointment;
    private AppointmentDatabaseHelper appointmentDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        appointmentDbHelper = new AppointmentDatabaseHelper(this);

        // Retrieve appointment fields (not object!) for safety
        String petName = getIntent().getStringExtra("pet_name");
        String serviceType = getIntent().getStringExtra("service_type");
        String serviceName = getIntent().getStringExtra("service_name");
        String dateTime = getIntent().getStringExtra("date_time");
        String total = getIntent().getStringExtra("total");
        String statusStr = getIntent().getStringExtra("status");
        String paymentStatusStr = getIntent().getStringExtra("payment_status");

        Appointment.Status status = statusStr != null ? Appointment.Status.valueOf(statusStr) : Appointment.Status.PENDING;
        Appointment.PaymentStatus paymentStatus = paymentStatusStr != null ? Appointment.PaymentStatus.valueOf(paymentStatusStr) : Appointment.PaymentStatus.PAID;

        appointment = new Appointment(
                petName,
                serviceType,
                serviceName,
                dateTime,
                total,
                paymentStatus,
                status
        );

        edtCardNumber = findViewById(R.id.edtCardNumber);
        edtCardName = findViewById(R.id.edtCardName);
        edtCardCVC = findViewById(R.id.edtCardCVC);
        edtExpMonth = findViewById(R.id.edtExpMonth);
        edtExpYear = findViewById(R.id.edtExpYear);
        txtTransAmount = findViewById(R.id.txtTransAmount);
        txtTransRef = findViewById(R.id.txtTransRef);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        cardVisa = findViewById(R.id.cardVisa);
        cardMC = findViewById(R.id.cardMC);
        cardAmex = findViewById(R.id.cardAmex);

        if (appointment != null && appointment.getTotal() != null) {
            txtTransAmount.setText(" " + appointment.getTotal());
        }

        String transRef = UUID.randomUUID().toString().replace("-", "").substring(0, 13).toUpperCase();
        txtTransRef.setText(transRef);

        edtCardNumber.setEnabled(false);

        setupCardNumberFormatting();

        View.OnClickListener cardSelectListener = view -> {
            resetCardBorders();
            view.setBackground(ContextCompat.getDrawable(this, R.drawable.cr12b91c674));

            if (view == cardVisa) selectedPaymentType = "Visa";
            else if (view == cardMC) selectedPaymentType = "MasterCard";
            else if (view == cardAmex) selectedPaymentType = "Amex";

            updateInputBehavior();
            edtCardNumber.setEnabled(true);
            setCardNumberMaxLength();
        };

        cardVisa.setOnClickListener(cardSelectListener);
        cardMC.setOnClickListener(cardSelectListener);
        cardAmex.setOnClickListener(cardSelectListener);

        btnConfirmPayment.setOnClickListener(v -> validateAndSubmit());
    }

    @Override
    public void onBackPressed() {
        showCancelConfirmation();
    }

    private void showCancelConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Transaction")
                .setMessage("Are you sure you want to cancel this transaction? Your payment will not be processed.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    // Go back to Bookings (VeterinaryActivity)
                    Intent intent = new Intent(PaymentActivity.this, VeterinaryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void resetCardBorders() {
        cardVisa.setBackgroundResource(0);
        cardMC.setBackgroundResource(0);
        cardAmex.setBackgroundResource(0);
    }

    private void updateInputBehavior() {
        edtCardNumber.setText("");
        edtCardNumber.setError(null);
        edtCardNumber.setHint("XXXX XXXX XXXX XXXX");
        edtCardNumber.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    }

    private void setupCardNumberFormatting() {
        edtCardNumber.addTextChangedListener(new TextWatcher() {
            boolean isFormatting;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;
                String digits = s.toString().replace(" ", "");
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ");
                    formatted.append(digits.charAt(i));
                }
                edtCardNumber.setText(formatted.toString());
                edtCardNumber.setSelection(formatted.length());
                isFormatting = false;
            }
        });
    }

    private void setCardNumberMaxLength() {
        int maxLength;
        switch (selectedPaymentType) {
            case "Amex":
                maxLength = 15;
                break;
            case "Visa":
            case "MasterCard":
            default:
                maxLength = 16;
                break;
        }

        int totalLengthWithSpaces = maxLength + (maxLength - 1) / 4;

        edtCardNumber.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(totalLengthWithSpaces)
        });

        edtCardNumber.setHint(generateCardNumberHint(maxLength));
    }

    private String generateCardNumberHint(int digits) {
        StringBuilder hint = new StringBuilder();
        for (int i = 1; i <= digits; i++) {
            hint.append("X");
            if (i % 4 == 0 && i != digits) {
                hint.append(" ");
            }
        }
        return hint.toString();
    }

    private void validateAndSubmit() {
        String cardNum = edtCardNumber.getText().toString().replace(" ", "").trim();
        String name = edtCardName.getText().toString().trim();
        String cvc = edtCardCVC.getText().toString().trim();
        String mm = edtExpMonth.getText().toString().trim();
        String yy = edtExpYear.getText().toString().trim();

        if (TextUtils.isEmpty(selectedPaymentType)) {
            edtCardNumber.setError("Select a card type");
            return;
        }

        int requiredLength;
        switch (selectedPaymentType) {
            case "Amex":
                requiredLength = 15;
                break;
            case "Visa":
            case "MasterCard":
                requiredLength = 16;
                break;
            default:
                edtCardNumber.setError("Select a card type");
                return;
        }

        if (cardNum.length() != requiredLength) {
            edtCardNumber.setError("Enter valid " + requiredLength + "-digit card number");
            return;
        }

        if (TextUtils.isEmpty(cvc) || cvc.length() < 3) {
            edtCardCVC.setError("Enter valid CVC");
            return;
        }

        if (TextUtils.isEmpty(mm)) {
            edtExpMonth.setError("Enter expiry month");
            return;
        }
        try {
            int month = Integer.parseInt(mm);
            if (month < 1 || month > 12) {
                edtExpMonth.setError("Invalid month");
                return;
            }
        } catch (NumberFormatException e) {
            edtExpMonth.setError("Invalid month");
            return;
        }

        if (TextUtils.isEmpty(yy) || yy.length() < 2) {
            edtExpYear.setError("Enter expiry year");
            return;
        }

        if (TextUtils.isEmpty(name)) {
            edtCardName.setError("Enter name on card");
            return;
        }

        // Save the PAID appointment to SQLite before showing success dialog!
        if (appointment != null) {
            appointment.setPaymentStatus(Appointment.PaymentStatus.PAID);

            // Optional: Remove any duplicate for this date/time before inserting (avoid any edge case)
            appointmentDbHelper.deleteAppointmentByDate(appointment.getDateTime());

            appointmentDbHelper.insertAppointment(appointment);
        }

        showPaymentSuccessDialog();
    }

    private void showPaymentSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnGoHome = dialog.findViewById(R.id.btnGoHome);
        btnGoHome.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(PaymentActivity.this, Homepage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        dialog.setCancelable(false);
        dialog.show();
    }
}