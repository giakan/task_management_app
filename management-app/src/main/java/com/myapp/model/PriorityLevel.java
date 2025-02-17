package com.myapp.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PriorityLevel {

    public PriorityLevel(){
        
    }

    private String name;
    private static final String FILE_PATH = "medialab/priorities.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final String DEFAULT_PRIORITY = "Default"; // Προκαθορισμένη προτεραιότητα

    public PriorityLevel(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }

    // ---------------- Φόρτωση και Αποθήκευση ----------------

    public static List<PriorityLevel> loadPriorities() {
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<JsonObject>() {}.getType();
            JsonObject jsonObject = gson.fromJson(reader, listType);
            List<String> priorityNames = gson.fromJson(jsonObject.get("priorities"), new TypeToken<List<String>>() {}.getType());

            List<PriorityLevel> priorities = new ArrayList<>();
            for (String name : priorityNames) {
                priorities.add(new PriorityLevel(name));
            }

            // Αν δεν υπάρχει η Default προτεραιότητα, την προσθέτουμε
            if (priorities.stream().noneMatch(p -> p.getName().equals(DEFAULT_PRIORITY))) {
                priorities.add(new PriorityLevel(DEFAULT_PRIORITY));
                savePriorities(priorities);
            }

            return priorities;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static void savePriorities(List<PriorityLevel> priorities) {
        JsonObject jsonObject = new JsonObject();
        List<String> priorityNames = new ArrayList<>();
        for (PriorityLevel p : priorities) {
            priorityNames.add(p.getName());
        }
        jsonObject.add("priorities", gson.toJsonTree(priorityNames));

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean deletePriority(PriorityLevel priority) {
        if (priority.getName().equals(DEFAULT_PRIORITY)) {
            return false; // Δεν μπορεί να διαγραφεί το Default
        }

        List<PriorityLevel> priorities = loadPriorities();
        priorities.removeIf(p -> p.getName().equals(priority.getName()));
        savePriorities(priorities);

        // Αντικατάσταση των σχετικών tasks με Default
        Task.updatePriorityForDeleted(priority);
        return true;
    }
}
