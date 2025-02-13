package com.myapp;

import com.myapp.data.TaskManager;
import com.myapp.model.Task;
import com.myapp.model.Category;
import com.myapp.model.PriorityLevel;
import com.myapp.model.TaskStatus;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;

public class MainController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> categoryColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    @FXML private TableColumn<Task, String> deadlineColumn;
    @FXML private TableColumn<Task, String> statusColumn;

    @FXML private TextField titleField;
    @FXML private ComboBox<Category> categoryField;
    @FXML private ComboBox<PriorityLevel> priorityField;
    @FXML private DatePicker deadlineField;
    @FXML private ComboBox<TaskStatus> statusField;

    private TaskManager taskManager;

    public MainController() {
        this.taskManager = new TaskManager();
    }
    

    @FXML
    private void handleAddTask() {
        String title = titleField.getText();
        Category category = categoryField.getValue();
        PriorityLevel priority = priorityField.getValue();
        LocalDate deadline = deadlineField.getValue();
        TaskStatus status = statusField.getValue();

        if (title == null || title.isEmpty() || category == null || priority == null || deadline == null || status == null) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        Task newTask = new Task(title, "", category, priority, deadline, status);
        taskManager.addTask(newTask);
        taskTable.getItems().add(newTask);

        clearFields();
    }

    @FXML
    private void handleEditTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("Error", "No task selected.");
            return;
        }

        if (!titleField.getText().isEmpty()) selectedTask.setTitle(titleField.getText());
        if (categoryField.getValue() != null) selectedTask.setCategory(categoryField.getValue());
        if (priorityField.getValue() != null) selectedTask.setPriority(priorityField.getValue());
        if (deadlineField.getValue() != null) selectedTask.setDeadline(deadlineField.getValue());
        if (statusField.getValue() != null) selectedTask.setStatus(statusField.getValue());

        taskManager.saveTasks();
        taskTable.refresh();
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("Error", "No task selected.");
            return;
        }

        taskManager.removeTask(selectedTask);
        taskTable.getItems().remove(selectedTask);
    }

    private void clearFields() {
        titleField.clear();
        categoryField.getSelectionModel().clearSelection();
        priorityField.getSelectionModel().clearSelection();
        deadlineField.setValue(null);
        statusField.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
