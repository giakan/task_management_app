package com.myapp;

import com.myapp.model.Task;
import com.myapp.model.Category;
import com.myapp.model.PriorityLevel;
import com.myapp.model.TaskStatus;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import java.time.LocalDate;
import java.util.List;

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

        // Φορτώνουμε τα task
        taskList = FXCollections.observableArrayList(Task.getAllTasks());
        taskTable.setItems(taskList);
    }

    @FXML
    private void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/myapp/add-task-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Task");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Παίρνουμε το νέο Task αν δημιουργήθηκε
            AddTaskController controller = loader.getController();
            Task newTask = controller.getNewTask();
        
            if (newTask != null) {
                taskList.add(newTask);
                taskTable.refresh();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Add Task window.",Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void handleEditTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("Error", "No task selected.",Alert.AlertType.ERROR);
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
            //clearFields();
        } else {
            showAlert("Error", "Task modification failed.",Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("Error", "No task selected.",Alert.AlertType.ERROR);
            return;
        }

        boolean deleted = Task.deleteTask(selectedTask.getId());
        if (deleted) {
            taskList.remove(selectedTask);
        } else {
            showAlert("Error", "Task deletion failed.",Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Enter the category name:");
        dialog.setContentText("Category:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                List<Category> categories = Category.loadCategories();
                Category newCategory = new Category(name);

                if (!categories.contains(newCategory)) {
                    categories.add(newCategory);
                    Category.saveCategories(categories);
                    categoryField.getItems().add(newCategory);
                } else {
                    showAlert("Error", "Category already exists.",Alert.AlertType.ERROR);
                }
            }
        });
    }


    @FXML
    private void handleDeleteCategory() {
        List<Category> categories = Category.loadCategories();

        if (categories.isEmpty()) {
            showAlert("Error", "There are no categories to delete.",Alert.AlertType.ERROR);
            return;
        }

        // Δημιουργία διαλόγου για επιλογή κατηγορίας προς διαγραφή
        ChoiceDialog<Category> dialog = new ChoiceDialog<>(categories.get(0), categories);
        dialog.setTitle("Delete Category");
        dialog.setHeaderText("Select a category to delete:");
        dialog.setContentText("Category:");

        dialog.showAndWait().ifPresent(selectedCategory -> {
            categories.remove(selectedCategory);
            Category.saveCategories(categories);

            // Αφαίρεση της κατηγορίας από το ComboBox στο UI
            categoryField.getItems().remove(selectedCategory);
        });
    }



    @FXML
    private void handleEditCategory(ActionEvent event) {
        List<Category> categories = Category.loadCategories();
    
        // Δημιουργία του διαλόγου με επιλογή κατηγορίας
        ChoiceDialog<Category> choiceDialog = new ChoiceDialog<>(null, categories);
        choiceDialog.setTitle("Edit Category");
        choiceDialog.setHeaderText("Select a category to edit:");
        choiceDialog.setContentText("Category:");
    
        choiceDialog.showAndWait().ifPresent(selectedCategory -> {
            if (selectedCategory != null) {
                TextInputDialog textInputDialog = new TextInputDialog(selectedCategory.getName());
                textInputDialog.setTitle("Edit Category Name");
                textInputDialog.setHeaderText("Enter new category name:");
                textInputDialog.setContentText("New Name:");
            
                textInputDialog.showAndWait().ifPresent(newName -> {
                    if (!newName.trim().isEmpty()) {
                        // Ενημερώνουμε την υπάρχουσα κατηγορία
                        selectedCategory.setName(newName);
                        Category.saveCategories(categories);
                    
                        showAlert("Success", "Category updated successfully.",Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Error", "New name cannot be empty.",Alert.AlertType.ERROR);
                    }
                });
            }
        });
    }




    @FXML
    private void handleAddPriority(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Priority");
        dialog.setHeaderText("Enter the priority name:");
        dialog.setContentText("Priority:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                List<PriorityLevel> priorities = PriorityLevel.loadPriorities();
                PriorityLevel newPriority = new PriorityLevel(name);

                if (!priorities.contains(newPriority)) {
                    priorities.add(newPriority);
                    PriorityLevel.savePriorities(priorities);

                    // Προσθήκη στο UI
                    priorityField.getItems().add(newPriority);
                    System.out.println("New priority added: " + newPriority);
                } else {
                    showAlert("Error", "Priority already exists.",Alert.AlertType.ERROR);
                }
            }
        });
    }


    @FXML
    private void handleDeletePriority(ActionEvent event) {
        List<PriorityLevel> priorities = PriorityLevel.loadPriorities();

        if (priorities.isEmpty()) {
            showAlert("Error", "No priorities available to delete.",Alert.AlertType.ERROR);
            return;
        }

        // Δημιουργία διαλόγου με drop-down για τις διαθέσιμες προτεραιότητες
        ChoiceDialog<PriorityLevel> dialog = new ChoiceDialog<>(priorities.get(0), priorities);
        dialog.setTitle("Delete Priority");
        dialog.setHeaderText("Select a priority to delete:");
        dialog.setContentText("Priority:");

        dialog.showAndWait().ifPresent(selectedPriority -> {
            priorities.remove(selectedPriority);
            PriorityLevel.savePriorities(priorities);

            // Αν υπάρχει το priorityField στο Add Task, αφαιρούμε το επιλεγμένο
            if (priorityField != null) {
                priorityField.getItems().remove(selectedPriority);
            }
        });
    }


    @FXML
    private void handleEditPriority(ActionEvent event) {
        // Φτιάχνουμε το Dialog για την επιλογή της προτεραιότητας προς επεξεργασία
        ChoiceDialog<PriorityLevel> dialog = new ChoiceDialog<>(null, PriorityLevel.loadPriorities());
        dialog.setTitle("Edit Priority");
        dialog.setHeaderText("Select a priority to edit:");
        dialog.setContentText("Priority:");

        dialog.showAndWait().ifPresent(selectedPriority -> {
            if (selectedPriority == null) {
                showAlert("Error", "No priority selected.", Alert.AlertType.ERROR);
                return;
            }

            // Ζητάμε νέο όνομα μέσω TextInputDialog
            TextInputDialog inputDialog = new TextInputDialog(selectedPriority.getName());
            inputDialog.setTitle("Edit Priority");
            inputDialog.setHeaderText("Enter new priority name:");
            inputDialog.setContentText("New Name:");

            inputDialog.showAndWait().ifPresent(newName -> {
                if (newName.trim().isEmpty()) {
                    showAlert("Error", "New name cannot be empty.", Alert.AlertType.ERROR);
                    return;
                }

                // Ενημερώνουμε τη λίστα
                List<PriorityLevel> priorities = PriorityLevel.loadPriorities();
                for (PriorityLevel priority : priorities) {
                    if (priority.getName().equals(selectedPriority.getName())) {
                        priority.setName(newName); // Αλλάζουμε το όνομα μόνο
                        break;
                    }
                }

                // Αποθηκεύουμε τις αλλαγές
                PriorityLevel.savePriorities(priorities);
                showAlert("Success", "Priority updated successfully!", Alert.AlertType.INFORMATION);
        });
    });
}


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}
