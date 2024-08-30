package controller;

import db.DBConnection;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Todo;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ViewAddTaskFormController implements Initializable {

    @FXML
    private TableColumn<Todo, String> colID;

    @FXML
    private TableColumn<Todo, String> colTask;

    @FXML
    private TableColumn<Todo, LocalDate> colDate;

    @FXML
    private TableColumn<Todo, String> colStatus;

    @FXML
    private DatePicker dateSelectedDate;

    @FXML
    private DatePicker dateTaskDate;

    @FXML
    private Pane paneAddTask;

    @FXML
    private TableView<Todo> tblToDoTable;

    @FXML
    private TextField txtSelectedTask;

    @FXML
    private TextField txtTask;

    private final ObservableList<Todo> todoList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        paneAddTask.setVisible(false);
        initializeTable();
        refreshTable();
        selectionListener();
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
    void btnAddNewOnAction(ActionEvent event) {
        paneAddTask.setVisible(true);
    }

    @FXML
    void btnAddToListOnAction(ActionEvent event) {
        String sql = "INSERT INTO ToDoTable (id, task, taskDate, status) VALUES (?, ?, ?, ?)";
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, autoGenerateID());
            preparedStatement.setString(2, txtTask.getText());
            preparedStatement.setDate(3, java.sql.Date.valueOf(dateTaskDate.getValue()));
            preparedStatement.setString(4, "Incomplete");
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Task added successfully.");
                refreshTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add task.");
            }
            txtTask.setText("");
            dateTaskDate.setValue(null);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the task.");
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        Todo selectedTodo = tblToDoTable.getSelectionModel().getSelectedItem();
        if (selectedTodo != null) {
            String sql = "DELETE FROM ToDoTable WHERE id = ?";
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, selectedTodo.getId());
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Task deleted successfully.");
                    refreshTable();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete task.");
                }
                txtSelectedTask.setText("");
                dateSelectedDate.setValue(null);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while deleting the task.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a task to delete.");
        }
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        Todo selectedTodo = tblToDoTable.getSelectionModel().getSelectedItem();
        if (selectedTodo != null) {
            String sql = "UPDATE ToDoTable SET task = ?, taskDate = ?, status = ? WHERE id = ?";
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, txtSelectedTask.getText());
                preparedStatement.setDate(2, java.sql.Date.valueOf(dateSelectedDate.getValue()));
                preparedStatement.setString(3, selectedTodo.getStatus());
                preparedStatement.setString(4, selectedTodo.getId());
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Task updated successfully.");
                    refreshTable();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update task.");
                }
                txtSelectedTask.setText("");
                dateSelectedDate.setValue(null);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating the task.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a task to update.");
        }
    }

    @FXML
    void btnCompletedNewOnAction(ActionEvent event) {
        Todo selectedTodo = tblToDoTable.getSelectionModel().getSelectedItem();
        if (selectedTodo != null) {
            String sqlInsertCompleted = "INSERT INTO CompletedTaskTable (id, task, completedDate) VALUES (?, ?, ?)";
            String sqlUpdateStatus = "UPDATE ToDoTable SET status = ? WHERE id = ?";
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                PreparedStatement insertStatement = connection.prepareStatement(sqlInsertCompleted);
                insertStatement.setString(1, selectedTodo.getId());
                insertStatement.setString(2, selectedTodo.getTask());
                insertStatement.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                insertStatement.executeUpdate();

                PreparedStatement updateStatement = connection.prepareStatement(sqlUpdateStatus);
                updateStatement.setString(1, "Completed");
                updateStatement.setString(2, selectedTodo.getId());
                updateStatement.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Task marked as completed.");
                refreshTable();

                txtSelectedTask.setText("");
                dateSelectedDate.setValue(null);

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while completing the task.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a task to mark as completed.");
        }
    }

    private void selectionListener() {
        tblToDoTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                txtSelectedTask.setText(newValue.getTask());
                dateSelectedDate.setValue(newValue.getTaskDate());
            } else {
                txtSelectedTask.setText("");
                dateSelectedDate.setValue(null);
            }
        });
    }

    public String autoGenerateID() {
        Connection connection = DBConnection.getInstance().getConnection();
        String newTodoId = null;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id FROM ToDoTable ORDER BY id DESC LIMIT 1");
            boolean isExist = resultSet.next();
            if (isExist) {
                String lastId = resultSet.getString("id");
                int noOfTodos = Integer.parseInt(lastId.substring(2));
                noOfTodos++;
                newTodoId = String.format("TD%03d", noOfTodos);
            } else {
                newTodoId = "TD001";
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return newTodoId;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void initializeTable() {
        colID.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getId()));
        colTask.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTask()));
        colDate.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTaskDate()));

        colStatus.setCellFactory(column -> new TableCell<Todo, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("Completed".equals(status)) {
                        setTextFill(javafx.scene.paint.Color.GREEN);
                    } else if ("Incomplete".equals(status)) {
                        setTextFill(javafx.scene.paint.Color.RED);
                    } else {
                        setTextFill(javafx.scene.paint.Color.BLACK);
                    }
                }
            }
        });
        colStatus.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus()));
        tblToDoTable.setItems(todoList);
    }

    private void refreshTable() {
        todoList.clear();
        String sql = "SELECT * FROM ToDoTable";
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String task = resultSet.getString("task");
                LocalDate taskDate = resultSet.getDate("taskDate").toLocalDate();
                String status = resultSet.getString("status");
                Todo todo = new Todo(id, task, taskDate, status);
                todoList.add(todo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while loading tasks.");
        }
    }
}
