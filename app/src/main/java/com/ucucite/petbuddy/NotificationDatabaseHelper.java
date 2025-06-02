package com.ucucite.petbuddy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class NotificationDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notifications.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "notifications";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_MESSAGE = "message";
    private static final String COL_TIME = "time";
    private static final String COL_ICON = "icon";

    public NotificationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_MESSAGE + " TEXT, " +
                COL_TIME + " INTEGER, " +    // changed from TEXT to INTEGER
                COL_ICON + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For simplicity drop and recreate. Migrate data in production!
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertNotification(NotificationItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, item.title);
        values.put(COL_MESSAGE, item.message);
        values.put(COL_TIME, item.timeMillis); // Store as long
        values.put(COL_ICON, item.iconResId);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<NotificationItem> getAllNotifications() {
        List<NotificationItem> notifications = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COL_TIME + " DESC");
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
            String message = cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE));
            long timeMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIME));
            int iconResId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ICON));
            notifications.add(new NotificationItem(title, message, timeMillis, iconResId));
        }
        cursor.close();
        db.close();
        return notifications;
    }

    // Delete all notifications permanently from the table
    public void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}