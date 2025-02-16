package com.myapp.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.myapp.model.Task;
import com.myapp.utils.LocalDateAdapter;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class TaskManager {
    private static final String FILE_PATH = System.getProperty("user.dir") + "/medialab/tasks.json";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()) // ✅ Προσθήκη TypeAdapter για LocalDate
            .setPrettyPrinting()
            .create();
    private List<Task> tasks;

    public TaskManager() {
        this.tasks = loadTasks();
    }

    private void createDirectoryIfNotExists() {
        File directory = new File("medialab");
        if (!directory.exists()) {
            boolean created = directory.mkdir();
            System.out.println(created ? " Folder 'medialab' created." : " Failed to create folder.");
        }
    }

    // Αποθήκευση εργασιών σε JSON
    public void saveTasks() {
        createDirectoryIfNotExists();
        System.out.println("✅ Saving tasks to: " + FILE_PATH);

        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Φόρτωμα εργασιών από JSON
    private List<Task> loadTasks() {
        File file = new File(FILE_PATH);
    
        //  Αν το αρχείο δεν υπάρχει ή είναι κενό, επιστρέφουμε λίστα
        if (!file.exists() || file.length() == 0) {
            System.out.println(" No valid tasks.json found. Creating a new list.");
            return new ArrayList<>();
        }
    
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Task>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    

    // Προσθήκη εργασίας
    public void addTask(Task task) {
        tasks.add(task);
        saveTasks();
    }

    // Διαγραφή εργασίας
    public void removeTask(Task task) {
        tasks.remove(task);
        saveTasks();
    }

    // Λίστα με όλες τις εργασίες
    public List<Task> getTasks() {
        return tasks;
    }
}
