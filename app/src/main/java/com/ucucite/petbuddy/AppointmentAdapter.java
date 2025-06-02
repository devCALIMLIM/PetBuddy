package com.ucucite.petbuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.UUID;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    public interface OnCancelClickListener {
        void onCancel(int position);
    }

    private final List<Appointment> appointments;
    private final OnCancelClickListener onCancelClickListener;

    public AppointmentAdapter(List<Appointment> appointments, OnCancelClickListener listener) {
        this.appointments = appointments;
        this.onCancelClickListener = listener;
        assignIdsIfMissing();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment_status, parent, false);
        return new AppointmentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appt = appointments.get(position);

        // Assign unique ID if not set
        if (appt.getId() == null || appt.getId().trim().isEmpty()) {
            appt.setId(UUID.randomUUID().toString());
        }

        // Display order as requested:
        holder.pet.setText("Pet: " + safe(appt.getPetName()));
        holder.careType.setText("Care Type: " + safe(appt.getCareTypeDisplay()));
        holder.service.setText("Service: " + safe(appt.getServiceName()));
        holder.datetime.setText("Date & Time: " + safe(appt.getDateTime()));
        holder.total.setText("Total: " + safe(appt.getTotal()));
        holder.status.setText("Status: " + statusText(appt.getStatus()));

        // Display appointment ID if TextView exists in XML (optional)
        if (holder.id != null) {
            holder.id.setText("ID: " + safe(appt.getId()));
        }

        // Payment status display (just PAID/UNPAID)
        String displayPaymentStatus;
        Appointment.Status status = appt.getStatus();
        Appointment.PaymentStatus realPaymentStatus = appt.getPaymentStatus();
        if (status == Appointment.Status.IN_PROGRESS || status == Appointment.Status.COMPLETED) {
            displayPaymentStatus = "PAID";
        } else if (realPaymentStatus != null) {
            displayPaymentStatus = realPaymentStatus.name();
        } else {
            displayPaymentStatus = "";
        }
        holder.paymentStatus.setText(displayPaymentStatus);

        // Cancel/Delete btn logic
        if (status == Appointment.Status.CANCELLED || status == Appointment.Status.COMPLETED) {
            holder.cancel.setText("Delete");
            holder.cancel.setVisibility(View.VISIBLE);
        } else if (status == Appointment.Status.PENDING) {
            holder.cancel.setText("Cancel");
            holder.cancel.setVisibility(View.VISIBLE);
        } else {
            holder.cancel.setVisibility(View.GONE);
        }
        holder.cancel.setOnClickListener(v -> onCancelClickListener.onCancel(position));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView pet, careType, service, datetime, total, status, paymentStatus, id;
        Button cancel;
        AppointmentViewHolder(View v) {
            super(v);
            pet = v.findViewById(R.id.appointment_pet);
            careType = v.findViewById(R.id.appointment_care_type);
            service = v.findViewById(R.id.appointment_service);
            datetime = v.findViewById(R.id.appointment_datetime);
            total = v.findViewById(R.id.appointment_total);
            status = v.findViewById(R.id.appointment_status_text);
            paymentStatus = v.findViewById(R.id.appointment_payment_status);
            cancel = v.findViewById(R.id.appointment_cancel_btn);
            // This TextView must be present in item_appointment_status.xml if you want to display the ID
            id = v.findViewById(R.id.appointment_id);
        }
    }

    /**
     * Assigns a unique id to appointments that don't have one.
     * Appointment class must have getId() and setId(String) methods!
     */
    private void assignIdsIfMissing() {
        for (Appointment appt : appointments) {
            if (appt.getId() == null || appt.getId().trim().isEmpty()) {
                appt.setId(UUID.randomUUID().toString());
            }
        }
    }

    private String statusText(Appointment.Status status) {
        if (status == null) return "";
        switch (status) {
            case PENDING: return "Pending";
            case CONFIRMED: return "Confirmed";
            case IN_PROGRESS: return "In Progress";
            case COMPLETED: return "Completed";
            case CANCELLED: return "Cancelled";
            default: return "";
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}