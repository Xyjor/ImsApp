package imsapp.controller;

import imsapp.controller.LoginController;
import imsapp.dao.UserDAO;
import imsapp.model.User;
import imsapp.util.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Admin-only controller for creating and managing employee accounts.
 *
 * STAFF restrictions enforced here:
 *   - Only ADMINs can reach this screen (DashboardController hides the nav button)
 *   - Admin cannot delete their own account
 *   - Admin cannot deactivate their own account
 *   - Admin cannot change their own role
 */
public class UserManagementController implements Initializable {

    @FXML private TextField     txtFullName, txtUsername, txtSearch;
    @FXML private PasswordField txtPassword, txtConfirmPassword;
    @FXML private ComboBox<String> cmbRole, cmbStatus;
    @FXML private TableView<User>  tblUsers;
    @FXML private Label lblMode, lblRecordCount;
    @FXML private Button btnDelete;

    private final UserDAO userDAO = new UserDAO();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;
    private User selectedUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbRole.setItems(FXCollections.observableArrayList("ADMIN", "STAFF"));
        cmbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));

        filteredData = new FilteredList<>(userList, p -> true);
        tblUsers.setItems(filteredData);

        loadData();
        setupSearch();
        setupDoubleClick();
        setupRoleBadges();
        setMode(false, null);
    }

    private void setupSearch() {
        if (txtSearch == null) return;
        txtSearch.textProperty().addListener((obs, o, nv) -> {
            filteredData.setPredicate(u -> {
                if (nv == null || nv.isEmpty()) return true;
                String f = nv.toLowerCase();
                return safe(u.getUsername()).toLowerCase().contains(f)
                    || safe(u.getFullName()).toLowerCase().contains(f)
                    || safe(u.getRole()).toLowerCase().contains(f);
            });
            updateCount();
        });
    }

    private void setupDoubleClick() {
        tblUsers.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                User sel = tblUsers.getSelectionModel().getSelectedItem();
                if (sel != null) { selectedUser = sel; populateForm(sel); setMode(true, sel); }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void setupRoleBadges() {
        tblUsers.getColumns().stream()
            .filter(c -> "Role".equals(c.getText())).findFirst()
            .ifPresent(col -> ((TableColumn<User, String>) col).setCellFactory(tc ->
                new TableCell<User, String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setText(null); setStyle(""); return; }
                        setText(item);
                        setStyle("ADMIN".equals(item)
                            ? "-fx-text-fill:#38bdf8;-fx-font-weight:bold;"
                            : "-fx-text-fill:#a8b2d1;");
                    }
                }));

        tblUsers.getColumns().stream()
            .filter(c -> "Active".equals(c.getText())).findFirst()
            .ifPresent(col -> ((TableColumn<User, Boolean>) col).setCellFactory(tc ->
                new TableCell<User, Boolean>() {
                    @Override protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setText(null); setStyle(""); return; }
                        setText(item ? "✔  Active" : "✘  Inactive");
                        setStyle(item
                            ? "-fx-text-fill:#4ade80;-fx-font-weight:bold;"
                            : "-fx-text-fill:#f87171;-fx-font-weight:bold;");
                    }
                }));
    }

    private void setMode(boolean editing, User u) {
        if (lblMode == null) return;
        if (editing && u != null) {
            lblMode.setText("✏  Editing: " + safe(u.getFullName()) + " (@" + safe(u.getUsername()) + ")");
            lblMode.setStyle("-fx-text-fill:#38bdf8;-fx-font-style:italic;");
            // Prevent admin from deleting their own account
            if (btnDelete != null)
                btnDelete.setDisable(u.getUserId() == SessionManager.getCurrentUser().getUserId());
        } else {
            lblMode.setText("＋  Create New Account");
            lblMode.setStyle("-fx-text-fill:#64748b;-fx-font-style:italic;");
            if (btnDelete != null) btnDelete.setDisable(false);
        }
    }

    private void updateCount() {
        if (lblRecordCount == null) return;
        int shown = filteredData.size(), total = userList.size();
        lblRecordCount.setText(shown == total
            ? "Showing " + total + " account" + (total == 1 ? "" : "s")
            : "Showing " + shown + " of " + total + " accounts");
    }

    @FXML
    private void handleCreate() {
        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String password = txtPassword.getText();
        String confirm  = txtConfirmPassword.getText();
        String role     = cmbRole.getValue();

        if (fullName.isEmpty())   { showError("Full Name is required."); txtFullName.requestFocus(); return; }
        if (username.isEmpty())   { showError("Username is required."); txtUsername.requestFocus(); return; }
        if (role == null)         { showError("Role is required."); cmbRole.requestFocus(); return; }
        if (password.isEmpty())   { showError("Password is required for new accounts."); txtPassword.requestFocus(); return; }
        if (!password.equals(confirm)) { showError("Passwords do not match."); txtConfirmPassword.requestFocus(); return; }

        try {
            if (userDAO.usernameExists(username, 0)) {
                showError("Username '" + username + "' is already taken."); return;
            }
            User u = new User();
            u.setFullName(fullName);
            u.setUsername(username);
            u.setPasswordHash(LoginController.sha256(password));
            u.setRole(role);
            u.setActive("Active".equals(cmbStatus.getValue()) || cmbStatus.getValue() == null);
            userDAO.insert(u);
            showInfo("Account created for " + fullName + ".\nThey can now log in with username: " + username);
            handleClear();
            loadData();
        } catch (SQLException e) { showError("Failed to create account: " + e.getMessage()); }
    }

    @FXML
    private void handleUpdate() {
        if (selectedUser == null) { showError("Double-click a row to select a user for editing."); return; }

        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String role     = cmbRole.getValue();

        if (fullName.isEmpty()) { showError("Full Name is required."); return; }
        if (username.isEmpty()) { showError("Username is required."); return; }
        if (role == null)       { showError("Role is required."); return; }

        // Prevent admin from removing their own ADMIN role
        int myId = SessionManager.getCurrentUser().getUserId();
        if (selectedUser.getUserId() == myId && !"ADMIN".equals(role)) {
            showError("You cannot remove your own ADMIN role."); return;
        }

        try {
            if (userDAO.usernameExists(username, selectedUser.getUserId())) {
                showError("Username '" + username + "' is already taken."); return;
            }
            User u = new User();
            u.setUserId(selectedUser.getUserId());
            u.setFullName(fullName);
            u.setUsername(username);
            u.setRole(role);
            u.setActive("Active".equals(cmbStatus.getValue()));
            userDAO.update(u);
            showInfo("Account updated for " + fullName + ".");
            handleClear();
            loadData();
        } catch (SQLException e) { showError("Failed to update account: " + e.getMessage()); }
    }

    @FXML
    private void handleResetPassword() {
        if (selectedUser == null) { showError("Double-click a row to select a user first."); return; }

        String password = txtPassword.getText();
        String confirm  = txtConfirmPassword.getText();

        if (password.isEmpty()) { showError("Enter a new password in the Password field."); return; }
        if (!password.equals(confirm)) { showError("Passwords do not match."); return; }
        if (password.length() < 6) { showError("Password must be at least 6 characters."); return; }

        Alert confirm2 = new Alert(Alert.AlertType.CONFIRMATION,
            "Reset password for: " + selectedUser.getUsername() + "?", ButtonType.YES, ButtonType.NO);
        confirm2.setTitle("Confirm Password Reset");
        confirm2.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    userDAO.updatePassword(selectedUser.getUserId(), LoginController.sha256(password));
                    showInfo("Password reset for " + selectedUser.getUsername() + ".");
                    txtPassword.clear(); txtConfirmPassword.clear();
                } catch (SQLException e) { showError("Failed to reset password: " + e.getMessage()); }
            }
        });
    }

    @FXML
    private void handleDelete() {
        if (selectedUser == null) { showError("Double-click a row to select a user for deletion."); return; }
        int myId = SessionManager.getCurrentUser().getUserId();
        if (selectedUser.getUserId() == myId) { showError("You cannot delete your own account."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Permanently delete account: " + selectedUser.getUsername() + "?\nThis cannot be undone.",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    userDAO.delete(selectedUser.getUserId());
                    showInfo("Account deleted.");
                    handleClear(); loadData();
                } catch (SQLException e) { showError("Delete failed: " + e.getMessage()); }
            }
        });
    }

    @FXML
    private void handleClear() {
        txtFullName.clear(); txtUsername.clear();
        txtPassword.clear(); txtConfirmPassword.clear();
        cmbRole.setValue(null); cmbStatus.setValue(null);
        selectedUser = null;
        tblUsers.getSelectionModel().clearSelection();
        setMode(false, null);
    }

    private void loadData() {
        try { userList.setAll(userDAO.findAll()); updateCount(); }
        catch (SQLException e) { showError("Failed to load users: " + e.getMessage()); }
    }

    private void populateForm(User u) {
        txtFullName.setText(safe(u.getFullName()));
        txtUsername.setText(safe(u.getUsername()));
        cmbRole.setValue(u.getRole());
        cmbStatus.setValue(u.isActive() ? "Active" : "Inactive");
        txtPassword.clear(); txtConfirmPassword.clear();
    }

    private String safe(String s) { return s != null ? s : ""; }
    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
