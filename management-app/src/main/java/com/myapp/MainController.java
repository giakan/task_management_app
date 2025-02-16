package com.myapp;

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

    private ObservableList<Task> taskList;

    @FXML
    public void initialize() {

        // Φόρτωση των tasks από JSON
        Task.loadTasksFromJson();

        // Αρχικοποίηση των στηλών
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Φορτώνουμε τα δεδομένα
        taskList = FXCollections.observableArrayList(Task.getAllTasks());
        taskTable.setItems(taskList);
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
        Task.addTask(newTask); // Προσθήκη στη λίστα
        taskList.add(newTask); // Ενημέρωση TableView

        clearFields();
    }

    @FXML
    private void handleEditTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("Error", "No task selected.");
            return;
        }

        // Ενημέρωση του Task
        boolean modified = Task.modifyTask(
                selectedTask.getId(),
                titleField.getText().isEmpty() ? selectedTask.getTitle() : titleField.getText(),
                selectedTask.getDescription(),
                categoryField.getValue() == null ? selectedTask.getCategory() : categoryField.getValue(),
                priorityField.getValue() == null ? selectedTask.getPriority() : priorityField.getValue(),
                deadlineField.getValue() == null ? selectedTask.getDeadline() : deadlineField.getValue(),
                statusField.getValue() == null ? selectedTask.getStatus() : statusField.getValue()
        );

        if (modified) {
            taskTable.refresh();
            clearFields();
        } else {
            showAlert("Error", "Task modification failed.");
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("Error", "No task selected.");
            return;
        }

        boolean deleted = Task.deleteTask(selectedTask.getId());
        if (deleted) {
            taskList.remove(selectedTask);
        } else {
            showAlert("Error", "Task deletion failed.");
        }
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
