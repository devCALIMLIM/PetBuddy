package com.ucucite.petbuddy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.drawable.Drawable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PetDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2; // increment if you change schema!
    private static final String DATABASE_NAME = "petbuddy.db";
    public static final String TABLE_PETS = "pets";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PHOTO_PATH = "photoPath";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_BREED = "breed";
    private static final String COLUMN_SEX = "sex";
    private static final String COLUMN_BIRTHDATE = "birthdate";
    private static final String COLUMN_WEIGHT = "weight";

    public PetDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_PETS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PHOTO_PATH + " TEXT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_TYPE + " TEXT, "
                + COLUMN_BREED + " TEXT, "
                + COLUMN_SEX + " TEXT, "
                + COLUMN_BIRTHDATE + " TEXT, "
                + COLUMN_WEIGHT + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PETS);
        onCreate(db);
    }

    public void insertPet(Pet pet) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTO_PATH, pet.photoPath);
        values.put(COLUMN_NAME, pet.name);
        values.put(COLUMN_TYPE, pet.type);
        values.put(COLUMN_BREED, pet.breed);
        values.put(COLUMN_SEX, pet.sex);
        values.put(COLUMN_BIRTHDATE, pet.birthdate);
        values.put(COLUMN_WEIGHT, pet.weight);
        db.insert(TABLE_PETS, null, values);
        db.close();
    }

    public List<Pet> getAllPets(Context context) {
        List<Pet> petList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PETS, null);
        if (cursor.moveToFirst()) {
            do {
                String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_PATH));
                Drawable photo = null;
                if (photoPath != null && !photoPath.isEmpty()) {
                    File f = new File(photoPath);
                    if (f.exists()) {
                        photo = Drawable.createFromPath(photoPath);
                    }
                }
                if (photo == null) {
                    photo = context.getDrawable(R.drawable.pet_profile); // fallback drawable
                }
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                String breed = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BREED));
                String sex = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEX));
                String birthdate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTHDATE));
                String weight = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT));
                petList.add(new Pet(photo, photoPath, name, type, breed, sex, birthdate, weight));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return petList;
    }

    public void deletePetByName(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PETS, COLUMN_NAME + " = ?", new String[]{name});
        db.close();
    }

    public void updatePet(Pet pet) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTO_PATH, pet.photoPath);
        values.put(COLUMN_TYPE, pet.type);
        values.put(COLUMN_BREED, pet.breed);
        values.put(COLUMN_SEX, pet.sex);
        values.put(COLUMN_BIRTHDATE, pet.birthdate);
        values.put(COLUMN_WEIGHT, pet.weight);
        db.update(TABLE_PETS, values, COLUMN_NAME + " = ?", new String[]{pet.name});
        db.close();
    }
}