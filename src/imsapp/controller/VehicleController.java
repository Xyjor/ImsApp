package imsapp.controller;

import imsapp.dao.CustomerDAO;
import imsapp.dao.VehicleDAO;
import imsapp.model.Customer;
import imsapp.model.Vehicle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for Vehicle Registration (OR/CR) module.
 */
public class VehicleController implements Initializable {

    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private TextField txtPlateNo, txtMake, txtModel, txtYear;
    @FXML private TextField txtColor, txtEngineNo, txtChassisNo;
    @FXML private TextField txtOrNo, txtCrNo;
    @FXML private ComboBox<String> cmbFuelType, cmbStatus;
    @FXML private TableView<Vehicle> tblVehicles;

    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();
    private Vehicle selectedVehicle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbFuelType.setItems(FXCollections.observableArrayList(
                "Gasoline", "Diesel", "Electric", "Hybrid"));
        cmbStatus.setItems(FXCollections.observableArrayList(
                "Registered", "Expired", "For Renewal"));

        tblVehicles.setItems(vehicleList);
        loadCustomers();
        loadData();

        tblVehicles.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedVehicle = n;
                populateForm(n);
            }
        });
    }

    private boolean validateInput() {
        if (cmbCustomer.getValue() == null) {
            showError("Customer selection is required.");
            cmbCustomer.requestFocus();
            return false;
        }
        if (txtPlateNo.getText() == null || txtPlateNo.getText().trim().isEmpty()) {
            showError("Plate Number is required.");
            txtPlateNo.requestFocus();
            return false;
        }
        if (txtMake.getText() == null || txtMake.getText().trim().isEmpty()) {
            showError("Make is required.");
            txtMake.requestFocus();
            return false;
        }
        return true;
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;
        try {
            Vehicle v = buildFromForm();
            vehicleDAO.insert(v);
            showInfo("Vehicle saved successfully.");
            handleClear();
            loadData();
        } catch (SQLException e) {
            showError("Save failed: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Year must be a valid number.");
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedVehicle == null) { showError("Select a vehicle first."); return; }
        if (!validateInput()) return;
        try {
            Vehicle v = buildFromForm();
            v.setVehicleId(selectedVehicle.getVehicleId());
            vehicleDAO.update(v);
            showInfo("Vehicle updated.");
            handleClear();
            loadData();
        } catch (SQLException e) {
            showError("Update failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedVehicle == null) { showError("Select a vehicle first."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete vehicle " + selectedVehicle.getPlateNumber() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    vehicleDAO.delete(selectedVehicle.getVehicleId());
                    showInfo("Vehicle deleted.");
                    handleClear();
                    loadData();
                } catch (SQLException e) {
                    showError("Delete failed: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleClear() {
        cmbCustomer.setValue(null);
        txtPlateNo.clear(); txtMake.clear(); txtModel.clear();
        txtYear.clear(); txtColor.clear();
        txtEngineNo.clear(); txtChassisNo.clear();
        txtOrNo.clear(); txtCrNo.clear();
        cmbFuelType.setValue(null); cmbStatus.setValue(null);
        selectedVehicle = null;
        tblVehicles.getSelectionModel().clearSelection();
    }

    private void loadCustomers() {
        try {
            cmbCustomer.setItems(FXCollections.observableArrayList(customerDAO.findAll()));
        } catch (SQLException e) {
            showError("Failed to load customers: " + e.getMessage());
        }
    }

    @FXML private TextField txtSearch;
    private javafx.collections.transformation.FilteredList<Vehicle> filteredData;

    private void loadData() {
        try {
            vehicleList.setAll(vehicleDAO.findAll());
            filteredData = new javafx.collections.transformation.FilteredList<>(vehicleList, p -> true);
            tblVehicles.setItems(filteredData);
            
            if (txtSearch != null) {
                txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredData.setPredicate(vehicle -> {
                        if (newValue == null || newValue.isEmpty()) return true;
                        String lowerCaseFilter = newValue.toLowerCase();
                        if (vehicle.getPlateNumber().toLowerCase().contains(lowerCaseFilter)) return true;
                        if (vehicle.getMake().toLowerCase().contains(lowerCaseFilter)) return true;
                        if (vehicle.getModel().toLowerCase().contains(lowerCaseFilter)) return true;
                        return false;
                    });
                });
            }
        } catch (SQLException e) {
            showError("Failed to load vehicles: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        try {
            java.io.File file = new java.io.File(System.getProperty("user.home"), "Vehicles_Export.csv");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("ID,Plate No,Make,Model,Year,Color,Status");
                for (Vehicle v : vehicleList) {
                    pw.println(v.getVehicleId() + "," + v.getPlateNumber() + "," + v.getMake() + "," 
                            + v.getModel() + "," + v.getYearModel() + "," + v.getColor() + "," + v.getStatus());
                }
            }
            showInfo("Exported to " + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    private Vehicle buildFromForm() {
        Vehicle v = new Vehicle();
        Customer cust = cmbCustomer.getValue();
        if (cust != null) v.setCustomerId(cust.getCustomerId());
        v.setPlateNumber(txtPlateNo.getText());
        v.setMake(txtMake.getText());
        v.setModel(txtModel.getText());
        v.setYearModel(Integer.parseInt(txtYear.getText()));
        v.setColor(txtColor.getText());
        v.setEngineNumber(txtEngineNo.getText());
        v.setChassisNumber(txtChassisNo.getText());
        v.setOrNumber(txtOrNo.getText());
        v.setCrNumber(txtCrNo.getText());
        v.setFuelType(cmbFuelType.getValue());
        v.setStatus(cmbStatus.getValue());
        return v;
    }

    private void populateForm(Vehicle v) {
        try {
            Customer c = customerDAO.findById(v.getCustomerId());
            cmbCustomer.setValue(c);
        } catch (SQLException ignored) {}
        txtPlateNo.setText(v.getPlateNumber());
        txtMake.setText(v.getMake());
        txtModel.setText(v.getModel());
        txtYear.setText(String.valueOf(v.getYearModel()));
        txtColor.setText(v.getColor());
        txtEngineNo.setText(v.getEngineNumber());
        txtChassisNo.setText(v.getChassisNumber());
        txtOrNo.setText(v.getOrNumber());
        txtCrNo.setText(v.getCrNumber());
        cmbFuelType.setValue(v.getFuelType());
        cmbStatus.setValue(v.getStatus());
    }

    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
