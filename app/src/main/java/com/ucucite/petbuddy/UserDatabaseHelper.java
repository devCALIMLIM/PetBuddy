    package com.ucucite.petbuddy;

    import android.content.Context;
    import android.database.sqlite.*;
    import android.content.ContentValues;
    import android.database.Cursor;

    public class UserDatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "petbuddy_users.db";
        private static final int DATABASE_VERSION = 2; // Bumped version for schema change

        public static final String TABLE_USERS = "users";
        public static final String COL_ID = "id";
        public static final String COL_USERNAME = "username";
        public static final String COL_EMAIL = "email";
        public static final String COL_PASSWORD = "password";
        public static final String COL_PHOTO_PATH = "photo_path";

        public UserDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_USERNAME + " TEXT, "
                    + COL_EMAIL + " TEXT, "
                    + COL_PASSWORD + " TEXT, "
                    + COL_PHOTO_PATH + " TEXT)";
            db.execSQL(CREATE_USERS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Basic migration: add photo_path column if upgrading from v1 to v2
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_PHOTO_PATH + " TEXT");
            }
        }

        public boolean addUser(String username, String email, String password) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_USERNAME, username);
            values.put(COL_EMAIL, email);
            values.put(COL_PASSWORD, password);
            values.put(COL_PHOTO_PATH, "");
            long result = db.insert(TABLE_USERS, null, values);
            db.close();
            return result != -1;
        }

        public boolean userExists(String username, String email) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_USERS, null,
                    COL_USERNAME + "=? OR " + COL_EMAIL + "=?",
                    new String[]{username, email}, null, null, null);
            boolean exists = (cursor.getCount() > 0);
            cursor.close();
            db.close();
            return exists;
        }

        public boolean validateUser(String usernameOrEmail, String password) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_USERS, null,
                    "(" + COL_USERNAME + "=? OR " + COL_EMAIL + "=?) AND " + COL_PASSWORD + "=?",
                    new String[]{usernameOrEmail, usernameOrEmail, password},
                    null, null, null);
            boolean valid = (cursor.getCount() > 0);
            cursor.close();
            db.close();
            return valid;
        }

        public boolean emailExists(String email) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_USERS, null,
                    COL_EMAIL + "=?",
                    new String[]{email},
                    null, null, null);
            boolean exists = (cursor.getCount() > 0);
            cursor.close();
            db.close();
            return exists;
        }

        public boolean updatePassword(String email, String newPassword) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_PASSWORD, newPassword);
            int updated = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
            db.close();
            return updated > 0;
        }

        // --- For profile photo per account ---
        public boolean updateUserPhotoPath(String usernameOrEmail, String photoPath) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_PHOTO_PATH, photoPath);
            int updated = db.update(TABLE_USERS, values,
                    COL_USERNAME + "=? OR " + COL_EMAIL + "=?",
                    new String[]{usernameOrEmail, usernameOrEmail});
            db.close();
            return updated > 0;
        }

        public String getUserPhotoPath(String usernameOrEmail) {
            SQLiteDatabase db = this.getReadableDatabase();
            String path = null;
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COL_PHOTO_PATH},
                    COL_USERNAME + "=? OR " + COL_EMAIL + "=?",
                    new String[]{usernameOrEmail, usernameOrEmail},
                    null, null, null, "1");
            if (cursor.moveToFirst()) {
                path = cursor.getString(0);
            }
            cursor.close();
            db.close();
            return path;
        }

        // --- For getting user info for ProfileActivity ---
        public String getUsernameByEmailOrUsername(String value) {
            SQLiteDatabase db = this.getReadableDatabase();
            String username = "";
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COL_USERNAME},
                    COL_USERNAME + "=? OR " + COL_EMAIL + "=?",
                    new String[]{value, value},
                    null, null, null, "1");
            if (cursor.moveToFirst()) {
                username = cursor.getString(0);
            }
            cursor.close();
            db.close();
            return username;
        }

        public String getEmailByEmailOrUsername(String value) {
            SQLiteDatabase db = this.getReadableDatabase();
            String email = "";
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COL_EMAIL},
                    COL_USERNAME + "=? OR " + COL_EMAIL + "=?",
                    new String[]{value, value},
                    null, null, null, "1");
            if (cursor.moveToFirst()) {
                email = cursor.getString(0);
            }
            cursor.close();
            db.close();
            return email;
        }
    }