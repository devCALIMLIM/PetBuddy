package com.ucucite.petbuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<NotificationItem> notifications;

    public NotificationAdapter(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notif = notifications.get(position);
        holder.title.setText(notif.title);
        holder.message.setText(notif.message);
        holder.time.setText(getTimeAgo(notif.timeMillis));
        holder.icon.setImageResource(notif.iconResId);

        // Removed click listener for in-app notification center (no redirection)
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, time;
        ImageView icon;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notification_title);
            message = itemView.findViewById(R.id.notification_message);
            time = itemView.findViewById(R.id.notification_time);
            icon = itemView.findViewById(R.id.icon);
        }
    }

    private String getTimeAgo(long timeMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timeMillis;
        if (diff < 60 * 1000) return "just now";
        else if (diff < 60 * 60 * 1000) return (diff / (60 * 1000)) + " min ago";
        else if (diff < 24 * 60 * 60 * 1000) return (diff / (60 * 60 * 1000)) + " hr ago";
        else return (diff / (24 * 60 * 60 * 1000)) + " days ago";
    }
}