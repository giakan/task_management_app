package com.myapp.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Task {
    private static List<Task> taskList = new ArrayList<>();
    private static final String FILE_PATH = "tasks.json";

    private int id;
    private String title;
    private String description;
    private Category category;
    private PriorityLevel priority;
    private LocalDate deadline;
    private TaskStatus status;

    private static int nextId = 1;

    public Task(String title, String description, Category category, PriorityLevel priority, LocalDate deadline, TaskStatus status) {
        this.id = nextId++;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public TaskStatus getStatus() {
        return status;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }


    // -------------------- Λειτουργίες Διαχείρισης Task --------------------

    /** Προσθήκη Task στη λίστα και αποθήκευση στο JSON */
    public static void addTask(Task task) {
        taskList.add(task);
        saveTasksToJson();
    }

    /** Διαγραφή Task με βάση το ID και ενημέρωση του JSON */
    public static boolean deleteTask(int taskId) {
        boolean removed = taskList.removeIf(task -> task.getId() == taskId);
        if (removed) saveTasksToJson();
        return removed;
    }

    /** Τροποποίηση ενός Task και ενημέρωση του JSON */
    public static boolean modifyTask(int taskId, String newTitle, String newDescription, Category newCategory,
                                     PriorityLevel newPriority, LocalDate newDeadline, TaskStatus newStatus) {
        Optional<Task> taskToModify = taskList.stream()
                .filter(task -> task.getId() == taskId)
                .findFirst();

        if (taskToModify.isPresent()) {
            Task task = taskToModify.get();
            task.setTitle(newTitle);
            task.setDescription(newDescription);
            task.setCategory(newCategory);
            task.setPriority(newPriority);
            task.setDeadline(newDeadline);
            task.setStatus(newStatus);
            saveTasksToJson();
            return true;
        }
        return false;
    }

    /** Επιστροφή όλων των Tasks */
    public static List<Task> getAllTasks() {
        return new ArrayList<>(taskList);
    }

    // -------------------- JSON Αποθήκευση και Φόρτωση --------------------

    /** Αποθηκεύει τη λίστα των tasks σε JSON αρχείο */
    public static void saveTasksToJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(FILE_PATH), taskList);
        } catch (IOException e) {
            System.err.println("Error saving tasks to JSON: " + e.getMessage());
        }
    }

    /** Φορτώνει τις εργασίες από JSON αρχείο κατά την εκκίνηση */
    public static void loadTasksFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                taskList = mapper.readValue(file, new TypeReference<List<Task>>() {});
            }
        } catch (IOException e) {
            System.err.println("Error loading tasks from JSON: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Task{" + "id=" + id + ", title='" + title + '\'' + ", status=" + status + '}';
    }
}
