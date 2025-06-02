package com.ucucite.petbuddy;

public class NotificationItem {
    public String title;
    public String message;
    public long timeMillis; // Store as epoch millis
    public int iconResId;

    public NotificationItem(String title, String message, long timeMillis, int iconResId) {
        this.title = title;
        this.message = message;
        this.timeMillis = timeMillis;
        this.iconResId = iconResId;
    }
}