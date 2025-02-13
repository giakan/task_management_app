package com.myapp.model;

import java.time.LocalDate;

public class Reminder {
    private Task task;
    private LocalDate reminderDate;

    public Reminder(Task task, LocalDate reminderDate) {
        this.task = task;
        this.reminderDate = reminderDate;
    }

    public Task getTask() { return task; }
    public LocalDate getReminderDate() { return reminderDate; }

    @Override
    public String toString() {
        return "Reminder for task: " + task.getTitle() + " on " + reminderDate;
    }
}
