package com.myapp;

import com.myapp.data.TaskManager;
import com.myapp.model.Category;
import com.myapp.model.PriorityLevel;
import com.myapp.model.Task;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class MainApp extends Application {
    private TaskManager taskManager;

    @Override
    public void start(Stage primaryStage) {
        // ✅ Φορτώνουμε τις εργασίες από το JSON
        taskManager = new TaskManager();
        List<Task> tasks = taskManager.getTasks();
        System.out.println("📂 Tasks loaded: " + tasks.size());

        // 🔹 Αν δεν υπάρχουν εργασίες, προσθέτουμε μία demo task
        if (tasks.isEmpty()) {
            Task demoTask = new Task(1, "Complete JavaFX UI", "Build the main interface",
                    new Category("Development"), new PriorityLevel("High"), LocalDate.of(2024, 3, 1));
            taskManager.addTask(demoTask);
            System.out.println("✅ Added a demo task!");
        }

        // 🔹 Δημιουργούμε ένα Label που δείχνει τον αριθμό των εργασιών
        Label taskCountLabel = new Label("Tasks Loaded: " + tasks.size());
        VBox root = new VBox(20, taskCountLabel);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("Task Management App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
