package imsapp.controller;

import imsapp.dao.UserDAO;
import imsapp.model.User;
import imsapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Username and password required.");
            return;
        }

        try {
            User authenticatedUser = userDAO.authenticate(user, pass);
            if (authenticatedUser != null) {
                SessionManager.setCurrentUser(authenticatedUser);
                loadDashboard();
            } else {
                showError("Invalid username or password.");
            }
        } catch (SQLException e) {
            showError("Database connection failed: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/imsapp/view/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 700));
            stage.setTitle("Vehicle & Driver IMS - Dashboard (" + SessionManager.getCurrentUser().getRole() + ")");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }
}
