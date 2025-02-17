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
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import java.time.LocalDate;
import java.util.List;

public class MainController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> descriptionColumn;
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
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status")); 

        // Φορτώνουμε τα task
        taskList = FXCollections.observableArrayList(Task.getAllTasks());
        taskTable.setItems(taskList);

        // Εκτελούμε τον έλεγχο σε ξεχωριστό thread για να μην καθυστερεί η εκκίνηση
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Καθυστέρηση 1s για πιο φυσική εμπειρία
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(this::checkForDelayedTasks);
        }).start();
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
                Task.addTask(newTask); // Αποθήκευση στο JSON
                taskList.add(newTask); // Προσθήκη στη λίστα
                taskTable.refresh();   // Ανανεώνουμε τον πίνακα
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
            showAlert("Error", "No task selected.", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/myapp/edit-task-view.fxml"));
            Parent root = loader.load();

            // Παίρνουμε τον Controller και περνάμε το επιλεγμένο Task
            EditTaskController controller = loader.getController();
            controller.setTaskData(selectedTask);

            Stage stage = new Stage();
            stage.setTitle("Edit Task");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Μετά το edit, ανανεώνουμε τον πίνακα
            taskTable.refresh();
            Task.saveTasksToJson();  // Ενημέρωση JSON
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Edit Task window.", Alert.AlertType.ERROR);
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
            showAlert("Error", "There are no categories to delete.", Alert.AlertType.ERROR);
            return;
        }

        ChoiceDialog<Category> dialog = new ChoiceDialog<>(categories.get(0), categories);
        dialog.setTitle("Delete Category");
        dialog.setHeaderText("Select a category to delete:");
        dialog.setContentText("Category:");

        dialog.showAndWait().ifPresent(selectedCategory -> {
            System.out.println("Deleting category: " + selectedCategory.getName());

            // Διαγραφή της κατηγορίας
            categories.remove(selectedCategory);
            Category.saveCategories(categories);

            // Διαγραφή των tasks και reminders που έχουν αυτή την κατηγορία
            int deletedCount = Task.deleteTasksByCategory(selectedCategory);
            System.out.println("Deleted " + deletedCount + " tasks and their reminders.");

            // Ενημέρωση UI
            taskList.setAll(Task.getAllTasks());
            taskTable.refresh();

            showAlert("Success", deletedCount + " tasks and their reminders deleted along with the category.", Alert.AlertType.INFORMATION);
        });
    }



    @FXML
    private void handleEditCategory(ActionEvent event) {
        List<Category> categories = Category.loadCategories();

        // Δημιουργία του διαλόγου για επιλογή της κατηγορίας προς επεξεργασία
        ChoiceDialog<Category> dialog = new ChoiceDialog<>(null, categories);
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Select a category to edit:");
        dialog.setContentText("Category:");

        dialog.showAndWait().ifPresent(selectedCategory -> {
            if (selectedCategory == null) {
                showAlert("Error", "No category selected.", Alert.AlertType.ERROR);
                return;
            }

            // Ζητάμε νέο όνομα μέσω TextInputDialog
            TextInputDialog inputDialog = new TextInputDialog(selectedCategory.getName());
            inputDialog.setTitle("Edit Category");
            inputDialog.setHeaderText("Enter new category name:");
            inputDialog.setContentText("New Name:");

            inputDialog.showAndWait().ifPresent(newName -> {
                if (newName.trim().isEmpty()) {
                    showAlert("Error", "New name cannot be empty.", Alert.AlertType.ERROR);
                    return;
                }

                if (categories.stream().anyMatch(c -> c.getName().equals(newName))) {
                    showAlert("Error", "Category name already exists.", Alert.AlertType.ERROR);
                    return;
                }

                // Ενημέρωση της λίστας των Categories
                String oldCategoryName = selectedCategory.getName();
                selectedCategory.setName(newName);

                // Ενημέρωση των Tasks που είχαν την παλιά κατηγορία
                Task.updateCategoryForEdit(oldCategoryName, newName);

                // Αποθήκευση των αλλαγών
                Category.saveCategories(categories);
                taskList.setAll(Task.getAllTasks()); // Ανανεώνουμε τη λίστα των tasks
                taskTable.refresh();

                showAlert("Success", "Category updated successfully!", Alert.AlertType.INFORMATION);
            });
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
            showAlert("Error", "No priorities available to delete.", Alert.AlertType.ERROR);
            return;
        }

        // Δημιουργία διαλόγου με drop-down για επιλογή προτεραιότητας προς διαγραφή
        ChoiceDialog<PriorityLevel> dialog = new ChoiceDialog<>(priorities.get(0), priorities);
        dialog.setTitle("Delete Priority");
        dialog.setHeaderText("Select a priority to delete:");
        dialog.setContentText("Priority:");

        dialog.showAndWait().ifPresent(selectedPriority -> {
            if (selectedPriority.getName().equals(PriorityLevel.DEFAULT_PRIORITY)) {
                showAlert("Error", "Cannot delete the Default priority.", Alert.AlertType.ERROR);
                return;
            }

            System.out.println("Deleting priority: " + selectedPriority.getName()); // Debugging

            // Αντικατάσταση όλων των σχετικών tasks με Default
            Task.updatePriorityForDeleted(selectedPriority);

            // Αφαίρεση της προτεραιότητας από τη λίστα
            priorities.removeIf(p -> p.getName().equals(selectedPriority.getName()));
            PriorityLevel.savePriorities(priorities);

            // Ενημέρωση UI
            taskList.setAll(Task.getAllTasks());
            taskTable.refresh();
            priorityField.getItems().remove(selectedPriority);

            showAlert("Success", "Priority deleted and tasks updated successfully.", Alert.AlertType.INFORMATION);
        });
    }


    @FXML
    private void handleEditPriority(ActionEvent event) {
        List<PriorityLevel> priorities = PriorityLevel.loadPriorities();

        // Δημιουργία του διαλόγου για επιλογή της προτεραιότητας προς επεξεργασία
        ChoiceDialog<PriorityLevel> dialog = new ChoiceDialog<>(null, priorities);
        dialog.setTitle("Edit Priority");
        dialog.setHeaderText("Select a priority to edit:");
        dialog.setContentText("Priority:");

        dialog.showAndWait().ifPresent(selectedPriority -> {
            if (selectedPriority == null) {
                showAlert("Error", "No priority selected.", Alert.AlertType.ERROR);
                return;
            }

            if (selectedPriority.getName().equals(PriorityLevel.DEFAULT_PRIORITY)) {
                showAlert("Error", "Cannot edit the Default priority.", Alert.AlertType.ERROR);
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
                if (priorities.stream().anyMatch(p -> p.getName().equals(newName))) {
                    showAlert("Error", "Priority name already exists.", Alert.AlertType.ERROR);
                    return;
                }

                // Ενημέρωση της λίστας των Priorities
                String oldPriorityName = selectedPriority.getName();
                selectedPriority.setName(newName);

                // Ενημέρωση των Tasks που είχαν την παλιά προτεραιότητα
                Task.updatePriorityForEdit(oldPriorityName, newName);

                // Αποθήκευση των αλλαγών
                PriorityLevel.savePriorities(priorities);
                taskList.setAll(Task.getAllTasks()); // Ανανεώνουμε τη λίστα των tasks
                taskTable.refresh();

                showAlert("Success", "Priority updated successfully!", Alert.AlertType.INFORMATION);
            });
        });
    }

    private void checkForDelayedTasks() {
        int delayedCount = Task.getDelayedTasksCount();
    
        if (delayedCount > 0) {
            showAlert("Delayed Tasks", 
                      "There are " + delayedCount + " overdue tasks!", 
                      Alert.AlertType.WARNING);
        }
    }
    

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}
