package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HomePageFormController {

    @FXML
    void btnCompletedTaskOnAction(ActionEvent event) {
        try {
            Stage stage = new Stage();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/completed_task_form.fxml"))));
            stage.setTitle("Completed Tasks Form");
            stage.centerOnScreen();
            stage.show();

            Stage disposeStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            disposeStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnViewAndAddTasksOnAction(ActionEvent event) {
        try {
            Stage stage = new Stage();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/view_and_add_tasks_form.fxml"))));
            stage.setTitle("View and Add Task Form");
            stage.centerOnScreen();
            stage.show();

            Stage disposeStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            disposeStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
