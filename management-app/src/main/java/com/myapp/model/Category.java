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

public class Category {
    private String name;
    private static final String FILE_PATH = "src/main/java/com/myapp/data/categories.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Category(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }

    // Μέθοδος για φόρτωμα κατηγοριών από JSON
    public static List<Category> loadCategories() {
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<JsonObject>() {}.getType();
            JsonObject jsonObject = gson.fromJson(reader, listType);
            List<String> categoryNames = gson.fromJson(jsonObject.get("categories"), new TypeToken<List<String>>() {}.getType());

            List<Category> categories = new ArrayList<>();
            for (String name : categoryNames) {
                categories.add(new Category(name));
            }
            return categories;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    // Μέθοδος για αποθήκευση κατηγοριών σε JSON
    public static void saveCategories(List<Category> categories) {
        JsonObject jsonObject = new JsonObject();
        List<String> categoryNames = new ArrayList<>();
        for (Category c : categories) {
            categoryNames.add(c.getName());
        }
        jsonObject.add("categories", gson.toJsonTree(categoryNames));

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
