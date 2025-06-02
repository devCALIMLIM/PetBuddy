package com.ucucite.petbuddy;

import java.util.ArrayList;
import java.util.List;

public class PetRepository {
    private static PetRepository instance;
    private final List<Pet> petList = new ArrayList<>();

    private PetRepository() {}

    public static PetRepository getInstance() {
        if (instance == null) {
            instance = new PetRepository();
        }
        return instance;
    }

    public List<Pet> getPetList() {
        return petList;
    }

    public void addPet(Pet pet) {
        petList.add(pet);
    }

    public void removePet(Pet pet) {
        petList.remove(pet);
    }
}