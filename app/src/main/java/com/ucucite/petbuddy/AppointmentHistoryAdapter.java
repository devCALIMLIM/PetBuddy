package com.ucucite.petbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppointmentHistoryAdapter extends RecyclerView.Adapter<AppointmentHistoryAdapter.HistoryViewHolder> {
    private final List<Appointment> appointments;

    public AppointmentHistoryAdapter(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Appointment appt = appointments.get(position);

        // 1. Pet Name
        holder.pet.setText("Pet Name: " + appt.getPetName());

        // 2. Care Type (pretty print)
        holder.careType.setText("Care Type: " + getCareTypeDisplay(appt.getServiceType()));

        // 3. Service
        holder.service.setText("Service: " + appt.getServiceName());

        // 4. Date & Time
        holder.datetime.setText("Date & Time: " + appt.getDateTime());

        // 5. Total (always show, and use color #FFD600 for total as in your past XML)
        holder.total.setVisibility(View.VISIBLE);
        holder.total.setText("Total: " + appt.getTotal());
        // Set color for total (matches #FFD600 from your XML)
        Context context = holder.total.getContext();
        holder.total.setTextColor(ContextCompat.getColor(context, R.color.total_color));

        // 6. Hide status and payment status
        holder.status.setVisibility(View.GONE);
        holder.paymentStatus.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void setAppointments(List<Appointment> newAppointments) {
        appointments.clear();
        appointments.addAll(newAppointments);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView pet, careType, service, datetime, total, status, paymentStatus;
        HistoryViewHolder(View v) {
            super(v);
            pet = v.findViewById(R.id.history_pet);
            careType = v.findViewById(R.id.history_care_type);
            service = v.findViewById(R.id.history_service);
            datetime = v.findViewById(R.id.history_datetime);
            total = v.findViewById(R.id.history_total);
            status = v.findViewById(R.id.history_status);
            paymentStatus = v.findViewById(R.id.history_payment_status);
        }
    }

    private String getCareTypeDisplay(String raw) {
        if (raw == null) return "";
        String value = raw.trim().toLowerCase();
        switch (value) {
            case "vet":
            case "veterinary":
            case "veterinarian":
                return "Veterinary";
            case "groom":
            case "grooming":
                return "Grooming";
            case "boarding":
                return "Boarding";
            default:
                return value.substring(0, 1).toUpperCase() + value.substring(1) + " Care";
        }
    }
}