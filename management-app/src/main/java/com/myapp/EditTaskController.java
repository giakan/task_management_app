package com.myapp;

import com.myapp.model.Category;
import com.myapp.model.PriorityLevel;
import com.myapp.model.Reminder;
import com.myapp.model.Task;
import com.myapp.model.TaskStatus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class EditTaskController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<Category> categoryField;
    @FXML private ComboBox<PriorityLevel> priorityField;
    @FXML private DatePicker deadlineField;
    @FXML private ComboBox<TaskStatus> statusField;
    private Task taskToEdit;

    public void setTaskData(Task task) {
        this.taskToEdit = task;

        // Φόρτωση των υπαρχόντων δεδομένων στο UI
        titleField.setText(task.getTitle());
        descriptionField.setText(task.getDescription());
        categoryField.setItems(FXCollections.observableArrayList(Category.loadCategories()));
        categoryField.setValue(task.getCategory());
        priorityField.setItems(FXCollections.observableArrayList(PriorityLevel.loadPriorities()));
        priorityField.setValue(task.getPriority());
        deadlineField.setValue(task.getDeadline());
        statusField.setItems(FXCollections.observableArrayList(TaskStatus.values()));
        statusField.setValue(task.getStatus());
    }

    @FXML
    private void handleSaveChanges() {
        if (taskToEdit == null) {
            showAlert("Error", "No task selected.", Alert.AlertType.ERROR);
            return;
        }

        // Ενημερώνουμε τα δεδομένα του Task
        taskToEdit.setTitle(titleField.getText());
        taskToEdit.setDescription(descriptionField.getText());
        taskToEdit.setCategory(categoryField.getValue());
        taskToEdit.setPriority(priorityField.getValue());
        taskToEdit.setDeadline(deadlineField.getValue());
        taskToEdit.setStatus(statusField.getValue());

        // Αποθήκευση των αλλαγών στο JSON
        Task.saveTasksToJson();

        showAlert("Success", "Task updated successfully!", Alert.AlertType.INFORMATION);
        closeWindow();
    }

    @FXML
    private void handleAddReminder() {
        if (taskToEdit == null) {
            showAlert("Error", "No task selected.", Alert.AlertType.ERROR);
            return;
        }

        if (taskToEdit.getStatus() == TaskStatus.COMPLETED) {
            showAlert("Error", "Cannot add reminder to a completed task.", Alert.AlertType.ERROR);
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("One Day Before", "One Day Before", "One Week Before", "One Month Before", "Custom Date");
        dialog.setTitle("Add Reminder");
        dialog.setHeaderText("Select Reminder Type:");
        dialog.setContentText("Reminder:");

        dialog.showAndWait().ifPresent(selected -> {
            LocalDate reminderDate = null;
            LocalDate deadline = taskToEdit.getDeadline();

            switch (selected) {
                case "One Day Before":
                    reminderDate = deadline.minusDays(1);
                    break;
                case "One Week Before":
                    reminderDate = deadline.minusWeeks(1);
                    break;
                case "One Month Before":
                    reminderDate = deadline.minusMonths(1);
                    break;
                case "Custom Date":
                    DatePicker datePicker = new DatePicker();
                    Alert customDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    customDialog.setTitle("Select Reminder Date");
                    customDialog.setHeaderText("Pick a date:");
                    customDialog.getDialogPane().setContent(datePicker);
                    customDialog.showAndWait();
                    reminderDate = datePicker.getValue();
                    break;
            }

            if (reminderDate != null && reminderDate.isAfter(deadline)) {
                showAlert("Error", "Reminder date must be before task deadline.", Alert.AlertType.ERROR);
                return;
            }

            // Προσθήκη διαλόγου για σχόλιο
            TextInputDialog commentDialog = new TextInputDialog();
            commentDialog.setTitle("Add Comment");
            commentDialog.setHeaderText("Enter a comment for this reminder (optional):");
            commentDialog.setContentText("Comment:");

            String comment = commentDialog.showAndWait().orElse(""); // Αν δεν πληκτρολογηθεί κάτι, μένει κενό

            // Δημιουργία υπενθύμισης με σχόλιο
            Reminder newReminder = new Reminder(taskToEdit.getId(), reminderDate, comment);
            Reminder.addReminder(newReminder);
            showAlert("Success", "Reminder added successfully!", Alert.AlertType.INFORMATION);
        });
    }


    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

