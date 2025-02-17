package com.myapp.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


public class Task {
    private static List<Task> taskList = new ArrayList<>();
    private static final String FILE_PATH = "medialab/tasks.json";

    @JsonProperty("id")
    private int id;
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("category")
    private Category category;
    @JsonProperty("priority")
    private PriorityLevel priority;
    @JsonProperty("deadline")
    private LocalDate deadline;
    @JsonProperty("status")
    private TaskStatus status;

    private static int nextId = 1;

    public Task(){
        
    }

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


    // -------------------- Λειτουργίες Διαχείρισης Task --------------------


    /** POP UP των Delayed στο άνοιγμα της εφαρμογής */
    public static int getDelayedTasksCount() {
        return (int) taskList.stream()
                .filter(task -> task.getStatus() == TaskStatus.DELAYED)
                .count();
    }    

    /** Προσθήκη Task στη λίστα και αποθήκευση στο JSON */
    public static void addTask(Task task) {
        taskList.add(task);
        saveTasksToJson();
    }

    /** Διαγραφή Task με βάση το ID και ενημέρωση του JSON */
    public static boolean deleteTask(int taskId) {
        boolean removed = taskList.removeIf(task -> task.getId() == taskId);
        
        if (removed) {
            System.out.println("Deleting reminders for Task ID: " + taskId); // 🔵 Debugging
            Reminder.deleteRemindersForTask(taskId); // 🟢 Διαγραφή όλων των reminders για το Task
            saveTasksToJson();
        }
    
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

    private static void updateNextId() {
        if (!taskList.isEmpty()) {
            nextId = taskList.stream().mapToInt(Task::getId).max().orElse(0) + 1;
        } else {
            nextId = 1;
        }
    }
    

    /** Επιστροφή όλων των Tasks */
    public static List<Task> getAllTasks() {
        return new ArrayList<>(taskList);
    }

    /** Ενημερώνει τις εργασίες που έχουν καθυστερήσει */
    private static void checkAndUpdateDelayedTasks() {
        LocalDate today = LocalDate.now();
        for (Task task : taskList) {
            if (task.getDeadline().isBefore(today) && task.getStatus() != TaskStatus.COMPLETED) {
                task.setStatus(TaskStatus.DELAYED);
            }
        }
    }

    /** Διαγράφει όλα τα tasks που ανήκουν σε μια συγκεκριμένη κατηγορία και τα αντίστοιχα reminders */
    public static int deleteTasksByCategory(Category category) {
        int initialSize = taskList.size();
    
        // Debugging: Εκτύπωση των tasks πριν τη διαγραφή
        System.out.println("Tasks before deletion:");
        for (Task task : taskList) {
            System.out.println("Task: " + task.getTitle() + " - Category: " + task.getCategory().getName());
        }
    
        // Εύρεση των tasks που θα διαγραφούν
        List<Task> tasksToDelete = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getCategory().getName().equals(category.getName())) {  // <-- Συγκρίνουμε με το όνομα της κατηγορίας
                tasksToDelete.add(task);
            }
        }
    
        // Debugging: Εκτύπωση των tasks που πρόκειται να διαγραφούν
        System.out.println("Tasks to be deleted:");
        for (Task task : tasksToDelete) {
            System.out.println("Deleting Task: " + task.getTitle() + " - Category: " + task.getCategory().getName());
        }
    
        // Διαγραφή των tasks
        taskList.removeAll(tasksToDelete);
    
        // Διαγραφή των reminders που αφορούν αυτά τα tasks
        for (Task task : tasksToDelete) {
            Reminder.deleteRemindersByTaskId(task.getId());
        }
    
        saveTasksToJson(); // Ενημέρωση JSON μετά τη διαγραφή
    
        System.out.println("Deleted " + (initialSize - taskList.size()) + " tasks and their reminders.");
        return initialSize - taskList.size(); // Επιστρέφουμε τον αριθμό των διαγραμμένων tasks
    }
    
    
    

    
    /** Update στα Task μετά απο διαγραφή προτεραιότητας στην οποία ανήκουν */
    public static void updatePriorityForDeleted(PriorityLevel deletedPriority) {
        boolean updated = false;
        PriorityLevel defaultPriority = new PriorityLevel(PriorityLevel.DEFAULT_PRIORITY);

        for (Task task : taskList) {
            if (task.getPriority().getName().equals(deletedPriority.getName())) {
                task.setPriority(defaultPriority); // Αντικατάσταση με Default
                updated = true;
            }
        }

        if (updated) {
            System.out.println("Updated tasks to default priority."); // Debugging
            saveTasksToJson(); // Αποθήκευση αλλαγών στο JSON
        }
    }

    /** Ενημέρωση των Tasks μετά από επεξεργασία μιας προτεραιότητας */
    public static void updatePriorityForEdit(String oldPriorityName, String newPriorityName) {
        boolean updated = false;

        for (Task task : taskList) {
            if (task.getPriority().getName().equals(oldPriorityName)) {
                System.out.println("Updating task priority: " + task.getTitle() + " -> " + newPriorityName); // Debugging
                task.setPriority(new PriorityLevel(newPriorityName)); // Αλλαγή προτεραιότητας
                updated = true;
            }
        }

        if (updated) {
            System.out.println("Tasks updated successfully.");
            saveTasksToJson();
        } else {
            System.out.println("No tasks needed updating.");
        }
    }

    /** Ενημέρωση των Tasks μετά από επεξεργασία μιας Κατηγορίας */
    public static void updateCategoryForEdit(String oldCategoryName, String newCategoryName) {
        boolean updated = false;

        for (Task task : taskList) {
            if (task.getCategory().getName().equals(oldCategoryName)) {
                System.out.println("Updating task category: " + task.getTitle() + " -> " + newCategoryName); // Debugging
                task.setCategory(new Category(newCategoryName)); // Αλλαγή κατηγορίας
                updated = true;
            }
        }

        if (updated) {
            System.out.println("Tasks updated successfully.");
            saveTasksToJson();
        } else {
            System.out.println("No tasks needed updating.");
        }
    }

    /** Όταν μια εργασία ολοκληρώνεται, διαγράφουμε όλες τις σχετικές υπενθυμίσεις */
    public void setStatus(TaskStatus status) {
        this.status = status;
        if (status == TaskStatus.COMPLETED) {
            Reminder.deleteRemindersForTask(this.id);
        }
    }


    // -------------------- JSON Αποθήκευση και Φόρτωση --------------------

    /** Αποθηκεύει τη λίστα των tasks σε JSON αρχείο */
    public static void saveTasksToJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); 
    
        try {
            System.out.println("Saving tasks: " + taskList);
            mapper.writeValue(new File(FILE_PATH), taskList);
        } catch (IOException e) {
            System.err.println("Error saving tasks to JSON: " + e.getMessage());
        }
    }

    /** Φορτώνει τις εργασίες από JSON αρχείο κατά την εκκίνηση */
    public static void loadTasksFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                taskList = mapper.readValue(file, new TypeReference<List<Task>>() {});
                updateNextId();
                System.out.println("Tasks loaded successfully: " + taskList); 
                checkAndUpdateDelayedTasks();
                saveTasksToJson();
            } else {
                System.out.println("No tasks file found."); 
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
