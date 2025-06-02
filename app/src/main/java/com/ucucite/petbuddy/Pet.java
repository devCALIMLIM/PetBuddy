package com.ucucite.petbuddy;

import android.graphics.drawable.Drawable;
import java.io.Serializable;

public class Pet implements Serializable {
    public Drawable photo;      // Use only for displaying
    public String photoPath;    // Persist this in DB
    public String name, type, breed, sex, birthdate, weight;

    public Pet(Drawable photo, String photoPath, String name, String type, String breed, String sex, String birthdate, String weight) {
        this.photo = photo;
        this.photoPath = photoPath;
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.sex = sex;
        this.birthdate = birthdate;
        this.weight = weight;
    }
}