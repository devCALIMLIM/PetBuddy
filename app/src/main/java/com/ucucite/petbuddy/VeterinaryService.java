package com.ucucite.petbuddy;

import java.io.Serializable;

public class VeterinaryService implements Serializable {
    public String name;
    public int price;
    public String type; // "Veterinary", "Grooming", "Boarding"

    public VeterinaryService(String name, int price, String type) {
        this.name = name;
        this.price = price;
        this.type = type;
    }
}