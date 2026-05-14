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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * Login controller with SHA-256 password hashing and Enter-key support.
 */
public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        // Fix #7: Enter key submits the form
        txtPassword.setOnAction(e -> handleLogin());
        txtUsername.setOnAction(e -> txtPassword.requestFocus());
    }

    @FXML
    private void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Username and password required.");
            return;
        }

        try {
            // Fix #16: Hash with SHA-256 before comparing
            String hashedPass = sha256(pass);
            User authenticatedUser = userDAO.authenticate(user, hashedPass);
            if (authenticatedUser != null) {
                SessionManager.setCurrentUser(authenticatedUser);
                loadDashboard(authenticatedUser);
            } else {
                showError("Invalid username or password.");
                txtPassword.clear();
                txtPassword.requestFocus();
            }
        } catch (SQLException e) {
            showError("Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Hashes a password with SHA-256 and returns the hex string.
     */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available in the JDK — this cannot happen
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    private void loadDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/imsapp/view/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 700));
            // Fix #20: Update window title with logged-in user info
            stage.setTitle("Vehicle & Driver IMS — " + user.getUsername() + " (" + user.getRole() + ")");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }
}
