package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reminder {
    private static final String FILE_PATH = "medialab/reminders.json";
    private static List<Reminder> reminders = new ArrayList<>();

    @JsonProperty("id")
    private int id;
    @JsonProperty("taskId")
    private int taskId;
    @JsonProperty("reminderDate")
    private LocalDate reminderDate;

    private static int nextId = 1;

    public Reminder() {
    }

    public Reminder(int taskId, LocalDate reminderDate) {
        this.id = nextId++;
        this.taskId = taskId;
        this.reminderDate = reminderDate;
    }

    public int getId() {
        return id;
    }

    public int getTaskId() {
        return taskId;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public static List<Reminder> getRemindersForTask(int taskId) {
        List<Reminder> taskReminders = new ArrayList<>();
        for (Reminder r : reminders) {
            if (r.getTaskId() == taskId) {
                taskReminders.add(r);
            }
        }
        return taskReminders;
    }

    public static void addReminder(Reminder reminder) {
        reminders.add(reminder);
        saveRemindersToJson();
    }

    public static boolean deleteReminder(int reminderId) {
        boolean removed = reminders.removeIf(r -> r.getId() == reminderId);
        if (removed) {
            saveRemindersToJson();
        }
        return removed;
    }

    public static void deleteRemindersForTask(int taskId) {
        reminders.removeIf(r -> r.getTaskId() == taskId);
        saveRemindersToJson();
    }

    public static void saveRemindersToJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try {
            mapper.writeValue(new File(FILE_PATH), reminders);
        } catch (IOException e) {
            System.err.println("Error saving reminders to JSON: " + e.getMessage());
        }
    }

    public static void loadRemindersFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                reminders = mapper.readValue(file, new TypeReference<List<Reminder>>() {});
            }
        } catch (IOException e) {
            System.err.println("Error loading reminders from JSON: " + e.getMessage());
        }
    }

    /** Διαγράφει όλα τα reminders που αφορούν συγκεκριμένο task */
    public static void deleteRemindersByTaskId(int taskId) {
        reminders.removeIf(reminder -> reminder.getTaskId() == taskId);
        saveRemindersToJson();
    }


}

