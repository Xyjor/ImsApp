package imsapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Controller for the main Dashboard layout.
 * Handles navigation between modules by swapping content in the center StackPane.
 */
public class DashboardController {

    @FXML private StackPane contentArea;
    @FXML private javafx.scene.control.Button btnCustomers, btnLicenses, btnVehicles, btnPayments;

    @FXML
    private void initialize() {
        handleNavCustomers();
    }

    private void setActiveButton(javafx.scene.control.Button activeBtn) {
        javafx.scene.control.Button[] btns = {btnCustomers, btnLicenses, btnVehicles, btnPayments};
        for (javafx.scene.control.Button btn : btns) {
            if (btn != null) {
                btn.getStyleClass().remove("nav-button-active");
            }
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("nav-button-active");
        }
    }

    @FXML
    private void handleNavCustomers() {
        loadView("/imsapp/view/CustomerView.fxml");
        setActiveButton(btnCustomers);
    }

    @FXML
    private void handleNavLicenses() {
        loadView("/imsapp/view/LicenseView.fxml");
        setActiveButton(btnLicenses);
    }

    @FXML
    private void handleNavVehicles() {
        loadView("/imsapp/view/VehicleView.fxml");
        setActiveButton(btnVehicles);
    }

    @FXML
    private void handleNavPayments() {
        loadView("/imsapp/view/PaymentView.fxml");
        setActiveButton(btnPayments);
    }

    @FXML
    private void handleLogout() {
        imsapp.util.SessionManager.setCurrentUser(null);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/imsapp/view/LoginView.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 600, 400));
            stage.setTitle("IMS - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads an FXML view into the center content area.
     */
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
