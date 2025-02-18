package com.myapp;

import com.myapp.model.Category;
import com.myapp.model.PriorityLevel;
import com.myapp.model.Task;
import com.myapp.model.TaskStatus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AddTaskController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Category> categoryField;
    @FXML private ComboBox<PriorityLevel> priorityField;
    @FXML private DatePicker deadlineField;
    @FXML private ComboBox<TaskStatus> statusField;

    private Task newTask;

    @FXML
    public void initialize() {
        categoryField.setItems(FXCollections.observableArrayList(Category.loadCategories()));
        priorityField.setItems(FXCollections.observableArrayList(PriorityLevel.loadPriorities()));
        statusField.setItems(FXCollections.observableArrayList(TaskStatus.values()));

        statusField.setValue(TaskStatus.OPEN);

    }

    @FXML
    private void handleSaveTask() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        Category category = categoryField.getValue();
        PriorityLevel priority = priorityField.getValue();
        LocalDate deadline = deadlineField.getValue();
        TaskStatus status = (statusField.getValue() != null) ? statusField.getValue() : TaskStatus.OPEN;

        if (title == null || title.isEmpty() || category == null || priority == null || deadline == null || status == null) {
            showAlert("Error", "Please fill in all fields.", Alert.AlertType.ERROR);
            return;
        }

        newTask = new Task(title, description, category, priority, deadline, status);

        showAlert("Success", "Task added successfully!", Alert.AlertType.INFORMATION);
        closeWindow();
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

    public Task getNewTask() {
        return newTask;
    }
}
