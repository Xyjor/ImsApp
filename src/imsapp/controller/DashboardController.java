package imsapp.controller;

import imsapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Dashboard controller.
 * Hides the User Management button from STAFF accounts.
 */
public class DashboardController {

    @FXML private StackPane contentArea;
    @FXML private Button btnCustomers, btnLicenses, btnVehicles, btnPayments, btnUsers;
    @FXML private VBox navPanel;
    @FXML private Label lblUserName, lblUserRole, lblAdminSection;

    @FXML
    private void initialize() {
        // Populate user info card
        imsapp.model.User me = SessionManager.getCurrentUser();
        if (me != null && lblUserName != null) {
            String name = me.getFullName() != null && !me.getFullName().isBlank()
                          ? me.getFullName() : me.getUsername();
            lblUserName.setText(name);
            lblUserRole.setText(me.getRole());
        }
        // Hide admin section for STAFF
        if (!SessionManager.isAdmin()) {
            if (btnUsers != null)        { btnUsers.setVisible(false);        btnUsers.setManaged(false); }
            if (lblAdminSection != null) { lblAdminSection.setVisible(false); lblAdminSection.setManaged(false); }
        }
        handleNavCustomers();
    }

    private void setActiveButton(Button activeBtn) {
        Button[] btns = {btnCustomers, btnLicenses, btnVehicles, btnPayments, btnUsers};
        for (Button btn : btns) {
            if (btn != null) btn.getStyleClass().remove("nav-button-active");
        }
        if (activeBtn != null) activeBtn.getStyleClass().add("nav-button-active");
    }

    @FXML private void handleNavCustomers() { loadView("/imsapp/view/CustomerView.fxml");       setActiveButton(btnCustomers); }
    @FXML private void handleNavLicenses()  { loadView("/imsapp/view/LicenseView.fxml");        setActiveButton(btnLicenses);  }
    @FXML private void handleNavVehicles()  { loadView("/imsapp/view/VehicleView.fxml");         setActiveButton(btnVehicles);  }
    @FXML private void handleNavPayments()  { loadView("/imsapp/view/PaymentView.fxml");         setActiveButton(btnPayments);  }
    @FXML private void handleNavUsers()     { loadView("/imsapp/view/UserManagementView.fxml");  setActiveButton(btnUsers);     }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to log out?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Logout");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                SessionManager.setCurrentUser(null);
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/imsapp/view/LoginView.fxml"));
                    javafx.scene.Parent root = loader.load();
                    javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
                    javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
                    stage.setScene(new javafx.scene.Scene(root, screen.getWidth(), screen.getHeight()));
                    stage.setTitle("IMS - Login");
                    stage.setMaximized(true);
                } catch (IOException e) { e.printStackTrace(); }
            }
        });
    }

    @FXML
    private void handleExit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to exit the application?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Exit Application");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                javafx.application.Platform.exit();
                System.exit(0);
            }
        });
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load view: " + fxmlPath);
        }
    }
}
