package com.ucucite.petbuddy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "petbuddy_appointments.db";
    private static final int DATABASE_VERSION = 4; // Bump version for schema change!

    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String COLUMN_ID = "_id"; // Now a String UUID
    public static final String COLUMN_PET_NAME = "petName";
    public static final String COLUMN_SERVICE_TYPE = "serviceType";
    public static final String COLUMN_SERVICE_NAME = "serviceName";
    public static final String COLUMN_DATE_TIME = "dateTime";
    public static final String COLUMN_TOTAL = "total";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_PAYMENT_STATUS = "paymentStatus";
    public static final String COLUMN_CREATED_TIME = "createdTimeMillis";
    public static final String COLUMN_NOTIFIED_CONFIRMED = "notifiedConfirmed";
    public static final String COLUMN_NOTIFIED_INPROGRESS = "notifiedInProgress";
    public static final String COLUMN_NOTIFIED_COMPLETED = "notifiedCompleted";
    public static final String COLUMN_NOTIFIED_REMINDER = "notifiedReminder";

    public AppointmentDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_APPOINTMENTS + " ("
                + COLUMN_ID + " TEXT PRIMARY KEY, "
                + COLUMN_PET_NAME + " TEXT, "
                + COLUMN_SERVICE_TYPE + " TEXT, "
                + COLUMN_SERVICE_NAME + " TEXT, "
                + COLUMN_DATE_TIME + " TEXT, "
                + COLUMN_TOTAL + " TEXT, "
                + COLUMN_STATUS + " TEXT, "
                + COLUMN_PAYMENT_STATUS + " TEXT, "
                + COLUMN_CREATED_TIME + " INTEGER, "
                + COLUMN_NOTIFIED_CONFIRMED + " INTEGER DEFAULT 0, "
                + COLUMN_NOTIFIED_INPROGRESS + " INTEGER DEFAULT 0, "
                + COLUMN_NOTIFIED_COMPLETED + " INTEGER DEFAULT 0, "
                + COLUMN_NOTIFIED_REMINDER + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        onCreate(db);
    }

    public void clearAllAppointments() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPOINTMENTS, null, null);
        db.close();
    }

    public void clearAllHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPOINTMENTS, COLUMN_STATUS + " IN (?, ?)", new String[]{"COMPLETED", "CANCELLED"});
        db.close();
    }

    public void insertAppointment(Appointment appt) {
        if (appt == null) return;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, appt.getId()); // UUID string
        values.put(COLUMN_PET_NAME, appt.getPetName());
        values.put(COLUMN_SERVICE_TYPE, appt.getServiceType());
        values.put(COLUMN_SERVICE_NAME, appt.getServiceName());
        values.put(COLUMN_DATE_TIME, appt.getDateTime());
        values.put(COLUMN_TOTAL, appt.getTotal());
        values.put(COLUMN_STATUS, appt.getStatus() != null ? appt.getStatus().name() : null);
        values.put(COLUMN_PAYMENT_STATUS, appt.getPaymentStatus() != null ? appt.getPaymentStatus().name() : null);
        values.put(COLUMN_CREATED_TIME, appt.getCreatedTimeMillis());
        values.put(COLUMN_NOTIFIED_CONFIRMED, appt.wasNotifiedConfirmed() ? 1 : 0);
        values.put(COLUMN_NOTIFIED_INPROGRESS, appt.wasNotifiedInProgress() ? 1 : 0);
        values.put(COLUMN_NOTIFIED_COMPLETED, appt.wasNotifiedCompleted() ? 1 : 0);
        values.put(COLUMN_NOTIFIED_REMINDER, appt.wasNotifiedReminder() ? 1 : 0);
        db.insert(TABLE_APPOINTMENTS, null, values);
        db.close();
    }

    public void updateAppointment(Appointment appt) {
        if (appt == null) return;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PET_NAME, appt.getPetName());
        values.put(COLUMN_SERVICE_TYPE, appt.getServiceType());
        values.put(COLUMN_SERVICE_NAME, appt.getServiceName());
        values.put(COLUMN_DATE_TIME, appt.getDateTime());
        values.put(COLUMN_TOTAL, appt.getTotal());
        values.put(COLUMN_STATUS, appt.getStatus() != null ? appt.getStatus().name() : null);
        values.put(COLUMN_PAYMENT_STATUS, appt.getPaymentStatus() != null ? appt.getPaymentStatus().name() : null);
        values.put(COLUMN_CREATED_TIME, appt.getCreatedTimeMillis());
        values.put(COLUMN_NOTIFIED_CONFIRMED, appt.wasNotifiedConfirmed() ? 1 : 0);
        values.put(COLUMN_NOTIFIED_INPROGRESS, appt.wasNotifiedInProgress() ? 1 : 0);
        values.put(COLUMN_NOTIFIED_COMPLETED, appt.wasNotifiedCompleted() ? 1 : 0);
        values.put(COLUMN_NOTIFIED_REMINDER, appt.wasNotifiedReminder() ? 1 : 0);
        db.update(TABLE_APPOINTMENTS, values, COLUMN_ID + "=?", new String[]{appt.getId()});
        db.close();
    }

    public void updateAppointmentStatus(Appointment appt) {
        if (appt == null) return;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, appt.getStatus() != null ? appt.getStatus().name() : null);
        db.update(TABLE_APPOINTMENTS, values, COLUMN_ID + "=?", new String[]{appt.getId()});
        db.close();
    }

    public void updateAppointmentNotificationFlags(Appointment appt) {
        if (appt == null) return;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFIED_CONFIRMED, appt.wasNotifiedConfirmed() ? 1 : 0);
        values.put(COLUMN_NOTIFIED_INPROGRESS, appt.wasNotifiedInProgress() ? 1 : 0);
        values.put(COLUMN_NOTIFIED_COMPLETED, appt.wasNotifiedCompleted() ? 1 : 0);
        values.put(COLUMN_NOTIFIED_REMINDER, appt.wasNotifiedReminder() ? 1 : 0);
        db.update(TABLE_APPOINTMENTS, values, COLUMN_ID + "=?", new String[]{appt.getId()});
        db.close();
    }

    public void deleteAppointment(Appointment appt) {
        if (appt == null) return;
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPOINTMENTS, COLUMN_ID + "=?", new String[]{appt.getId()});
        db.close();
    }

    public void deleteAppointmentByDate(String dateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPOINTMENTS, COLUMN_DATE_TIME + "=?", new String[]{dateTime});
        db.close();
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENTS, null, null, null, null, null, COLUMN_CREATED_TIME + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Appointment appt = new Appointment();
                appt.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))); // UUID
                appt.setPetName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PET_NAME)));
                appt.setServiceType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVICE_TYPE)));
                appt.setServiceName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVICE_NAME)));
                appt.setDateTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME)));
                appt.setTotal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOTAL)));
                String statusStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
                if (statusStr != null) {
                    try {
                        appt.setStatus(Appointment.Status.valueOf(statusStr));
                    } catch (IllegalArgumentException e) {
                        appt.setStatus(null);
                    }
                }
                String paymentStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_STATUS));
                if (paymentStr != null) {
                    try {
                        appt.setPaymentStatus(Appointment.PaymentStatus.valueOf(paymentStr));
                    } catch (IllegalArgumentException e) {
                        appt.setPaymentStatus(null);
                    }
                }
                appt.setCreatedTimeMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_TIME)));
                appt.setWasNotifiedConfirmed(getIntColumnSafe(cursor, COLUMN_NOTIFIED_CONFIRMED) == 1);
                appt.setWasNotifiedInProgress(getIntColumnSafe(cursor, COLUMN_NOTIFIED_INPROGRESS) == 1);
                appt.setWasNotifiedCompleted(getIntColumnSafe(cursor, COLUMN_NOTIFIED_COMPLETED) == 1);
                appt.setWasNotifiedReminder(getIntColumnSafe(cursor, COLUMN_NOTIFIED_REMINDER) == 1);
                list.add(appt);
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    public List<Appointment> getAllHistory() {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENTS, null,
                COLUMN_STATUS + " IN (?, ?)",
                new String[]{"COMPLETED", "CANCELLED"},
                null, null, COLUMN_CREATED_TIME + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Appointment appt = new Appointment();
                appt.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))); // UUID
                appt.setPetName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PET_NAME)));
                appt.setServiceType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVICE_TYPE)));
                appt.setServiceName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVICE_NAME)));
                appt.setDateTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME)));
                appt.setTotal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOTAL)));
                String statusStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
                if (statusStr != null) {
                    try {
                        appt.setStatus(Appointment.Status.valueOf(statusStr));
                    } catch (IllegalArgumentException e) {
                        appt.setStatus(null);
                    }
                }
                String paymentStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_STATUS));
                if (paymentStr != null) {
                    try {
                        appt.setPaymentStatus(Appointment.PaymentStatus.valueOf(paymentStr));
                    } catch (IllegalArgumentException e) {
                        appt.setPaymentStatus(null);
                    }
                }
                appt.setCreatedTimeMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_TIME)));
                appt.setWasNotifiedConfirmed(getIntColumnSafe(cursor, COLUMN_NOTIFIED_CONFIRMED) == 1);
                appt.setWasNotifiedInProgress(getIntColumnSafe(cursor, COLUMN_NOTIFIED_INPROGRESS) == 1);
                appt.setWasNotifiedCompleted(getIntColumnSafe(cursor, COLUMN_NOTIFIED_COMPLETED) == 1);
                appt.setWasNotifiedReminder(getIntColumnSafe(cursor, COLUMN_NOTIFIED_REMINDER) == 1);
                list.add(appt);
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    private int getIntColumnSafe(Cursor cursor, String column) {
        int idx = cursor.getColumnIndex(column);
        if (idx == -1) return 0;
        return cursor.getInt(idx);
    }
}