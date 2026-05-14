package imsapp.controller;

import imsapp.dao.CustomerDAO;
import imsapp.dao.DriversLicenseDAO;
import imsapp.model.Customer;
import imsapp.model.DriversLicense;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for the Driver's License module with eligibility checks.
 */
public class LicenseController implements Initializable {

    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private TextField txtLicenseNo, txtRestriction, txtConditions;
    @FXML private ComboBox<String> cmbLicenseType, cmbStatus;
    @FXML private DatePicker dpIssueDate, dpExpiryDate;
    @FXML private TableView<DriversLicense> tblLicenses;

    private final DriversLicenseDAO licenseDAO = new DriversLicenseDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ObservableList<DriversLicense> licenseList = FXCollections.observableArrayList();
    private DriversLicense selectedLicense;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbLicenseType.setItems(FXCollections.observableArrayList(
                "Student", "Non-Professional", "Professional"));
        cmbStatus.setItems(FXCollections.observableArrayList(
                "Active", "Expired", "Suspended", "Revoked"));

        tblLicenses.setItems(licenseList);
        loadCustomers();
        loadData();

        tblLicenses.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedLicense = n;
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
        if (txtLicenseNo.getText() == null || txtLicenseNo.getText().trim().isEmpty()) {
            showError("License Number is required.");
            txtLicenseNo.requestFocus();
            return false;
        }
        if (cmbLicenseType.getValue() == null) {
            showError("License Type is required.");
            cmbLicenseType.requestFocus();
            return false;
        }
        if (dpIssueDate.getValue() != null && dpExpiryDate.getValue() != null) {
            if (dpExpiryDate.getValue().isBefore(dpIssueDate.getValue())) {
                showError("Expiry Date cannot be before Issue Date.");
                dpExpiryDate.requestFocus();
                return false;
            }
        }
        return true;
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;
        try {
            DriversLicense dl = buildFromForm();
            licenseDAO.insert(dl);
            showInfo("License saved successfully.");
            handleClear();
            loadData();
        } catch (SQLException e) {
            showError("Save failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedLicense == null) { showError("Select a license first."); return; }
        if (!validateInput()) return;

        try {
            DriversLicense dl = buildFromForm();
            dl.setLicenseId(selectedLicense.getLicenseId());
            licenseDAO.update(dl);
            showInfo("License updated.");
            handleClear();
            loadData();
        } catch (SQLException e) {
            showError("Update failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedLicense == null) { showError("Select a license first."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete license " + selectedLicense.getLicenseNumber() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    licenseDAO.delete(selectedLicense.getLicenseId());
                    showInfo("License deleted.");
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
        txtLicenseNo.clear(); txtRestriction.clear(); txtConditions.clear();
        cmbLicenseType.setValue(null); cmbStatus.setValue(null);
        dpIssueDate.setValue(null); dpExpiryDate.setValue(null);
        selectedLicense = null;
        tblLicenses.getSelectionModel().clearSelection();
    }

    private void loadCustomers() {
        try {
            cmbCustomer.setItems(FXCollections.observableArrayList(customerDAO.findAll()));
        } catch (SQLException e) {
            showError("Failed to load customers: " + e.getMessage());
        }
    }

    @FXML private TextField txtSearch;
    private javafx.collections.transformation.FilteredList<DriversLicense> filteredData;

    private void loadData() {
        try {
            licenseList.setAll(licenseDAO.findAll());
            filteredData = new javafx.collections.transformation.FilteredList<>(licenseList, p -> true);
            tblLicenses.setItems(filteredData);
            
            if (txtSearch != null) {
                txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredData.setPredicate(license -> {
                        if (newValue == null || newValue.isEmpty()) return true;
                        String lowerCaseFilter = newValue.toLowerCase();
                        if (license.getLicenseNumber().toLowerCase().contains(lowerCaseFilter)) return true;
                        if (license.getLicenseType().toLowerCase().contains(lowerCaseFilter)) return true;
                        return false;
                    });
                });
            }
        } catch (SQLException e) {
            showError("Failed to load licenses: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        try {
            java.io.File file = new java.io.File(System.getProperty("user.home"), "Licenses_Export.csv");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("ID,License No,Type,Issue Date,Expiry Date,Status");
                for (DriversLicense l : licenseList) {
                    pw.println(l.getLicenseId() + "," + l.getLicenseNumber() + "," + l.getLicenseType() + "," 
                            + l.getIssueDate() + "," + l.getExpiryDate() + "," + l.getStatus());
                }
            }
            showInfo("Exported to " + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    private DriversLicense buildFromForm() {
        DriversLicense dl = new DriversLicense();
        Customer cust = cmbCustomer.getValue();
        if (cust != null) dl.setCustomerId(cust.getCustomerId());
        dl.setLicenseNumber(txtLicenseNo.getText());
        dl.setLicenseType(cmbLicenseType.getValue());
        dl.setIssueDate(dpIssueDate.getValue());
        dl.setExpiryDate(dpExpiryDate.getValue());
        dl.setRestrictionCode(txtRestriction.getText());
        dl.setConditions(txtConditions.getText());
        dl.setStatus(cmbStatus.getValue());
        return dl;
    }

    private void populateForm(DriversLicense dl) {
        try {
            Customer c = customerDAO.findById(dl.getCustomerId());
            cmbCustomer.setValue(c);
        } catch (SQLException ignored) {}
        txtLicenseNo.setText(dl.getLicenseNumber());
        cmbLicenseType.setValue(dl.getLicenseType());
        dpIssueDate.setValue(dl.getIssueDate());
        dpExpiryDate.setValue(dl.getExpiryDate());
        txtRestriction.setText(dl.getRestrictionCode());
        txtConditions.setText(dl.getConditions());
        cmbStatus.setValue(dl.getStatus());
    }

    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
