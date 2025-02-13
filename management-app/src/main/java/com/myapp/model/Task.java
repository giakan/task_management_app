package com.myapp.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private int id;
    private String title;
    private String description;
    private Category category;
    private PriorityLevel priority;
    private LocalDate deadline;
    private TaskStatus status;
    private List<Reminder> reminders;

    public Task(String title, String description, Category category, PriorityLevel priority, LocalDate deadline, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
        this.status = TaskStatus.OPEN;
        this.reminders = new ArrayList<>();
    }

    // Getters και Setters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public PriorityLevel getPriority() { return priority; }
    public LocalDate getDeadline() { return deadline; }
    public TaskStatus getStatus() { return status; }
    public List<Reminder> getReminders() { return reminders; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(Category category) { this.category = category; }
    public void setPriority(PriorityLevel priority) { this.priority = priority; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public void addReminder(Reminder reminder) {
        if (status != TaskStatus.COMPLETED) {
            reminders.add(reminder);
        }
    }

    public void removeReminder(Reminder reminder) {
        reminders.remove(reminder);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }
}
