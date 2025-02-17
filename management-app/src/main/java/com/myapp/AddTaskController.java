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
import java.util.List;

public class AddTaskController {

    @FXML private TextField titleField;
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
    }

    @FXML
    private void handleSaveTask() {
        String title = titleField.getText();
        Category category = categoryField.getValue();
        PriorityLevel priority = priorityField.getValue();
        LocalDate deadline = deadlineField.getValue();
        TaskStatus status = statusField.getValue();

        if (title == null || title.isEmpty() || category == null || priority == null || deadline == null || status == null) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        newTask = new Task(title, "", category, priority, deadline, status);
        Task.addTask(newTask);

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Task getNewTask() {
        return newTask;
    }
}
