package controller;

import db.DBConnection;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import model.CompletedTask;

public class CompletedTaskFormController {

    @FXML
    private TableColumn<CompletedTask, String> colCompletedTask;

    @FXML
    private TableColumn<CompletedTask, LocalDate> colCompletedTaskDate;

    @FXML
    private TableColumn<CompletedTask, String> colCompletedTaskID;

    @FXML
    private TableView<CompletedTask> tblCompletedTask;

    @FXML
    private TextField txtSearch;

    private ObservableList<CompletedTask> completedTasks = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colCompletedTaskID.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getId()));
        colCompletedTask.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTask()));
        colCompletedTaskDate.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCompletedDate()));

        loadCompletedTasks();
    }

    @FXML
    void btnHomePageOnAction(ActionEvent event) {
        try {
            Stage stage = new Stage();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/home_page_form.fxml"))));
            stage.setTitle("Home Page");
            stage.centerOnScreen();
            stage.show();

            Stage disposeStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            disposeStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnSearchOnAction(ActionEvent event) {
        String searchTerm = txtSearch.getText().trim();
        if (!searchTerm.isEmpty()) {
            searchTasks(searchTerm);
        } else {
            loadCompletedTasks();
        }
    }

    @FXML
    void btnFullListNewOnAction(ActionEvent event) {
        loadCompletedTasks();
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        CompletedTask selectedTask = tblCompletedTask.getSelectionModel().getSelectedItem();

        if (selectedTask != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Delete Task");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("Are you sure you want to delete the selected task?");
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    deleteTask(selectedTask);
                    completedTasks.remove(selectedTask);
                    tblCompletedTask.refresh();
                }
            });
        } else {
            showAlert("No Selection", "Please select a task to delete.");
        }
    }

    private void deleteTask(CompletedTask task) {
        String sql = "DELETE FROM CompletedTaskTable WHERE id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, task.getId());
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Success", "Task deleted successfully.");
            } else {
                showAlert("Error", "Failed to delete the task.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void searchTasks(String searchTerm) {
        String sql = "SELECT * FROM CompletedTaskTable WHERE id LIKE ? OR task LIKE ? OR completedDate = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + searchTerm + "%");
            statement.setString(2, "%" + searchTerm + "%");
            LocalDate date = parseDate(searchTerm);
            if (date != null) {
                statement.setObject(3, date);
            } else {
                statement.setObject(3, null);
            }

            ResultSet resultSet = statement.executeQuery();
            completedTasks.clear();
            while (resultSet.next()) {
                CompletedTask completedTask = new CompletedTask(
                        resultSet.getString("id"),
                        resultSet.getString("task"),
                        resultSet.getDate("completedDate").toLocalDate()
                );
                completedTasks.add(completedTask);
            }

            if (completedTasks.isEmpty()) {
                showAlert("No Results", "No tasks match the search criteria.");
            }

            tblCompletedTask.setItems(completedTasks);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }

    private void loadCompletedTasks() {
        String sql = "SELECT * FROM CompletedTaskTable";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            completedTasks.clear();
            while (resultSet.next()) {
                CompletedTask completedTask = new CompletedTask(
                        resultSet.getString("id"),
                        resultSet.getString("task"),
                        resultSet.getDate("completedDate").toLocalDate()
                );
                completedTasks.add(completedTask);
            }
            tblCompletedTask.setItems(completedTasks);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
