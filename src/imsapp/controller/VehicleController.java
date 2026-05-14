package imsapp.controller;

import imsapp.dao.CustomerDAO;
import imsapp.dao.VehicleDAO;
import imsapp.model.Customer;
import imsapp.model.Vehicle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class VehicleController implements Initializable {

    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private TextField txtPlateNo, txtMake, txtModel, txtYear;
    @FXML private TextField txtColor, txtEngineNo, txtChassisNo, txtOrNo, txtCrNo;
    @FXML private ComboBox<String> cmbFuelType, cmbStatus;
    @FXML private DatePicker dpRegistrationDate, dpExpiryDate;
    @FXML private TableView<Vehicle> tblVehicles;
    @FXML private TextField txtSearch;
    @FXML private Label lblMode, lblRecordCount;
    @FXML private Button btnDelete;

    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();
    private FilteredList<Vehicle> filteredData;
    private Vehicle selectedVehicle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbFuelType.setItems(FXCollections.observableArrayList("Gasoline","Diesel","Electric","Hybrid"));
        cmbStatus.setItems(FXCollections.observableArrayList("Registered","Expired","For Renewal"));
        filteredData = new FilteredList<>(vehicleList, p -> true);
        tblVehicles.setItems(filteredData);
        loadCustomers();
        loadData();
        setupSearch();
        setupDoubleClick();
        setupShortcuts();
        setupStatusBadges();
        setMode(false, null);
        if (btnDelete != null) btnDelete.setDisable(!imsapp.util.SessionManager.isAdmin());
    }

    private void setupSearch() {
        if (txtSearch == null) return;
        txtSearch.textProperty().addListener((obs, o, newVal) -> {
            filteredData.setPredicate(v -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String f = newVal.toLowerCase();
                return safe(v.getPlateNumber()).toLowerCase().contains(f)
                    || safe(v.getMake()).toLowerCase().contains(f)
                    || safe(v.getModel()).toLowerCase().contains(f)
                    || safe(v.getStatus()).toLowerCase().contains(f);
            });
            updateRecordCount();
        });
    }

    private void setupDoubleClick() {
        tblVehicles.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                Vehicle sel = tblVehicles.getSelectionModel().getSelectedItem();
                if (sel != null) { selectedVehicle = sel; populateForm(sel); setMode(true, sel); }
            }
        });
    }

    private void setupShortcuts() {
        tblVehicles.sceneProperty().addListener((obs, o, newScene) -> {
            if (newScene != null)
                newScene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                    this::handleSave);
        });
    }

    @SuppressWarnings("unchecked")
    private void setupStatusBadges() {
        tblVehicles.getColumns().stream()
            .filter(c -> "Status".equals(c.getText())).findFirst()
            .ifPresent(col -> ((TableColumn<Vehicle,String>) col).setCellFactory(tc ->
                new TableCell<Vehicle,String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setText(null); setStyle(""); return; }
                        setText(item);
                        switch (item) {
                            case "Registered":  setStyle("-fx-text-fill:#4ade80;-fx-font-weight:bold;"); break;
                            case "Expired":     setStyle("-fx-text-fill:#f87171;-fx-font-weight:bold;"); break;
                            case "For Renewal": setStyle("-fx-text-fill:#fb923c;-fx-font-weight:bold;"); break;
                            default:            setStyle("-fx-text-fill:#cbd5e1;");
                        }
                    }
                }));
    }

    private void setMode(boolean editing, Vehicle v) {
        if (lblMode == null) return;
        if (editing && v != null) {
            lblMode.setText("✏  Editing: " + v.getPlateNumber() + " — " + safe(v.getMake()) + " " + safe(v.getModel()));
            lblMode.setStyle("-fx-text-fill:#38bdf8;-fx-font-style:italic;");
        } else {
            lblMode.setText("＋  New Record");
            lblMode.setStyle("-fx-text-fill:#64748b;-fx-font-style:italic;");
        }
    }

    private void updateRecordCount() {
        if (lblRecordCount == null) return;
        int shown = filteredData.size(), total = vehicleList.size();
        lblRecordCount.setText(shown == total
            ? "Showing " + total + " record" + (total == 1 ? "" : "s")
            : "Showing " + shown + " of " + total + " records");
    }

    private boolean validateInput() {
        if (cmbCustomer.getValue() == null) { showError("Customer is required."); cmbCustomer.requestFocus(); return false; }
        if (txtPlateNo.getText().trim().isEmpty()) { showError("Plate Number is required."); txtPlateNo.requestFocus(); return false; }
        if (txtMake.getText().trim().isEmpty()) { showError("Make is required."); txtMake.requestFocus(); return false; }
        return true;
    }

    @FXML private void handleSave() {
        if (!validateInput()) return;
        try { vehicleDAO.insert(buildFromForm()); showInfo("Vehicle saved."); handleClear(); loadData(); }
        catch (SQLException e) { showError("Save failed: " + e.getMessage()); }
        catch (NumberFormatException e) { showError("Year must be a valid number."); }
    }

    @FXML private void handleUpdate() {
        if (selectedVehicle == null) { showError("Double-click a row to select a vehicle for editing."); return; }
        if (!validateInput()) return;
        try {
            Vehicle v = buildFromForm();
            v.setVehicleId(selectedVehicle.getVehicleId());
            vehicleDAO.update(v);
            showInfo("Vehicle updated."); handleClear(); loadData();
        } catch (SQLException e) { showError("Update failed: " + e.getMessage()); }
    }

    @FXML private void handleDelete() {
        if (selectedVehicle == null) { showError("Double-click a row to select a vehicle for deletion."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete vehicle: " + selectedVehicle.getPlateNumber() + "?\nThis cannot be undone.",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try { vehicleDAO.delete(selectedVehicle.getVehicleId()); showInfo("Vehicle deleted."); handleClear(); loadData(); }
                catch (SQLException e) { showError("Delete failed: " + e.getMessage()); }
            }
        });
    }

    @FXML private void handleClear() {
        cmbCustomer.setValue(null);
        txtPlateNo.clear(); txtMake.clear(); txtModel.clear(); txtYear.clear(); txtColor.clear();
        txtEngineNo.clear(); txtChassisNo.clear(); txtOrNo.clear(); txtCrNo.clear();
        cmbFuelType.setValue(null); cmbStatus.setValue(null);
        if (dpRegistrationDate != null) dpRegistrationDate.setValue(null);
        if (dpExpiryDate != null) dpExpiryDate.setValue(null);
        selectedVehicle = null; tblVehicles.getSelectionModel().clearSelection();
        setMode(false, null);
    }

    private void loadCustomers() {
        try { cmbCustomer.setItems(FXCollections.observableArrayList(customerDAO.findAll())); }
        catch (SQLException e) { showError("Failed to load customers: " + e.getMessage()); }
    }

    private void loadData() {
        try { vehicleList.setAll(vehicleDAO.findAll()); updateRecordCount(); }
        catch (SQLException e) { showError("Failed to load vehicles: " + e.getMessage()); }
    }

    @FXML private void handleExport() {
        try {
            java.io.File file = new java.io.File(System.getProperty("user.home"), "Vehicles_Export.csv");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("ID,Plate No,Make,Model,Year,Color,Engine No,Chassis No,OR No,CR No,Fuel,Reg Date,Expiry,Status");
                for (Vehicle v : vehicleList)
                    pw.println(v.getVehicleId() + "," + safe(v.getPlateNumber()) + "," + safe(v.getMake()) + ","
                        + safe(v.getModel()) + "," + v.getYearModel() + "," + safe(v.getColor()) + ","
                        + safe(v.getEngineNumber()) + "," + safe(v.getChassisNumber()) + ","
                        + safe(v.getOrNumber()) + "," + safe(v.getCrNumber()) + "," + safe(v.getFuelType()) + ","
                        + (v.getRegistrationDate() != null ? v.getRegistrationDate() : "") + ","
                        + (v.getExpiryDate() != null ? v.getExpiryDate() : "") + "," + safe(v.getStatus()));
            }
            showInfo("Exported to " + file.getAbsolutePath());
        } catch (Exception e) { showError("Export failed: " + e.getMessage()); }
    }

    private String safe(String s) { return s != null ? s : ""; }

    private Vehicle buildFromForm() {
        Vehicle v = new Vehicle();
        Customer cust = cmbCustomer.getValue();
        if (cust != null) v.setCustomerId(cust.getCustomerId());
        v.setPlateNumber(txtPlateNo.getText().trim());
        v.setMake(txtMake.getText().trim());
        v.setModel(txtModel.getText().trim());
        String yr = txtYear.getText().trim();
        v.setYearModel(yr.isEmpty() ? 0 : Integer.parseInt(yr));
        v.setColor(txtColor.getText().trim());
        v.setEngineNumber(txtEngineNo.getText().trim());
        v.setChassisNumber(txtChassisNo.getText().trim());
        v.setOrNumber(txtOrNo.getText().trim());
        v.setCrNumber(txtCrNo.getText().trim());
        v.setFuelType(cmbFuelType.getValue());
        v.setStatus(cmbStatus.getValue());
        if (dpRegistrationDate != null) v.setRegistrationDate(dpRegistrationDate.getValue());
        if (dpExpiryDate != null) v.setExpiryDate(dpExpiryDate.getValue());
        return v;
    }

    private void populateForm(Vehicle v) {
        try { cmbCustomer.setValue(customerDAO.findById(v.getCustomerId())); } catch (SQLException ignored) {}
        txtPlateNo.setText(safe(v.getPlateNumber())); txtMake.setText(safe(v.getMake()));
        txtModel.setText(safe(v.getModel()));
        txtYear.setText(v.getYearModel() > 0 ? String.valueOf(v.getYearModel()) : "");
        txtColor.setText(safe(v.getColor())); txtEngineNo.setText(safe(v.getEngineNumber()));
        txtChassisNo.setText(safe(v.getChassisNumber())); txtOrNo.setText(safe(v.getOrNumber()));
        txtCrNo.setText(safe(v.getCrNumber())); cmbFuelType.setValue(v.getFuelType());
        cmbStatus.setValue(v.getStatus());
        if (dpRegistrationDate != null) dpRegistrationDate.setValue(v.getRegistrationDate());
        if (dpExpiryDate != null) dpExpiryDate.setValue(v.getExpiryDate());
    }

    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
