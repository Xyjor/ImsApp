package imsapp.controller;

import imsapp.dao.CustomerDAO;
import imsapp.dao.DriversLicenseDAO;
import imsapp.model.Customer;
import imsapp.model.DriversLicense;

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

public class LicenseController implements Initializable {

    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private TextField txtLicenseNo, txtRestriction, txtConditions;
    @FXML private ComboBox<String> cmbLicenseType, cmbStatus;
    @FXML private DatePicker dpIssueDate, dpExpiryDate;
    @FXML private TableView<DriversLicense> tblLicenses;
    @FXML private TextField txtSearch;
    @FXML private Label lblMode, lblRecordCount;
    @FXML private Button btnDelete;

    private final DriversLicenseDAO licenseDAO = new DriversLicenseDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ObservableList<DriversLicense> licenseList = FXCollections.observableArrayList();
    private FilteredList<DriversLicense> filteredData;
    private DriversLicense selectedLicense;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbLicenseType.setItems(FXCollections.observableArrayList("Student","Non-Professional","Professional"));
        cmbStatus.setItems(FXCollections.observableArrayList("Active","Expired","Suspended","Revoked"));
        filteredData = new FilteredList<>(licenseList, p -> true);
        tblLicenses.setItems(filteredData);
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
            filteredData.setPredicate(l -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String f = newVal.toLowerCase();
                return safe(l.getLicenseNumber()).toLowerCase().contains(f)
                    || safe(l.getLicenseType()).toLowerCase().contains(f)
                    || safe(l.getStatus()).toLowerCase().contains(f);
            });
            updateRecordCount();
        });
    }

    private void setupDoubleClick() {
        tblLicenses.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                DriversLicense sel = tblLicenses.getSelectionModel().getSelectedItem();
                if (sel != null) { selectedLicense = sel; populateForm(sel); setMode(true, sel); }
            }
        });
    }

    private void setupShortcuts() {
        tblLicenses.sceneProperty().addListener((obs, o, newScene) -> {
            if (newScene != null)
                newScene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                    this::handleSave);
        });
    }

    @SuppressWarnings("unchecked")
    private void setupStatusBadges() {
        tblLicenses.getColumns().stream()
            .filter(c -> "Status".equals(c.getText())).findFirst()
            .ifPresent(col -> ((TableColumn<DriversLicense,String>) col).setCellFactory(tc ->
                new TableCell<DriversLicense,String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setText(null); setStyle(""); return; }
                        setText(item);
                        switch (item) {
                            case "Active":    setStyle("-fx-text-fill:#4ade80;-fx-font-weight:bold;"); break;
                            case "Expired":   setStyle("-fx-text-fill:#f87171;-fx-font-weight:bold;"); break;
                            case "Suspended": setStyle("-fx-text-fill:#fb923c;-fx-font-weight:bold;"); break;
                            case "Revoked":   setStyle("-fx-text-fill:#c084fc;-fx-font-weight:bold;"); break;
                            default:          setStyle("-fx-text-fill:#cbd5e1;");
                        }
                    }
                }));
    }

    private void setMode(boolean editing, DriversLicense l) {
        if (lblMode == null) return;
        if (editing && l != null) {
            lblMode.setText("✏  Editing: " + safe(l.getLicenseNumber()) + " (" + safe(l.getLicenseType()) + ")");
            lblMode.setStyle("-fx-text-fill:#38bdf8;-fx-font-style:italic;");
        } else {
            lblMode.setText("＋  New Record");
            lblMode.setStyle("-fx-text-fill:#64748b;-fx-font-style:italic;");
        }
    }

    private void updateRecordCount() {
        if (lblRecordCount == null) return;
        int shown = filteredData.size(), total = licenseList.size();
        lblRecordCount.setText(shown == total
            ? "Showing " + total + " record" + (total == 1 ? "" : "s")
            : "Showing " + shown + " of " + total + " records");
    }

    private boolean validateInput() {
        if (cmbCustomer.getValue() == null) { showError("Customer is required."); cmbCustomer.requestFocus(); return false; }
        if (txtLicenseNo.getText().trim().isEmpty()) { showError("License Number is required."); txtLicenseNo.requestFocus(); return false; }
        if (cmbLicenseType.getValue() == null) { showError("License Type is required."); cmbLicenseType.requestFocus(); return false; }
        if (dpIssueDate.getValue() != null && dpExpiryDate.getValue() != null
            && dpExpiryDate.getValue().isBefore(dpIssueDate.getValue())) {
            showError("Expiry Date cannot be before Issue Date."); dpExpiryDate.requestFocus(); return false;
        }
        return true;
    }

    @FXML private void handleSave() {
        if (!validateInput()) return;
        try { licenseDAO.insert(buildFromForm()); showInfo("License saved."); handleClear(); loadData(); }
        catch (SQLException e) { showError("Save failed: " + e.getMessage()); }
    }

    @FXML private void handleUpdate() {
        if (selectedLicense == null) { showError("Double-click a row to select a license for editing."); return; }
        if (!validateInput()) return;
        try {
            DriversLicense dl = buildFromForm();
            dl.setLicenseId(selectedLicense.getLicenseId());
            licenseDAO.update(dl); showInfo("License updated."); handleClear(); loadData();
        } catch (SQLException e) { showError("Update failed: " + e.getMessage()); }
    }

    @FXML private void handleDelete() {
        if (selectedLicense == null) { showError("Double-click a row to select a license for deletion."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete license: " + selectedLicense.getLicenseNumber() + "?\nThis cannot be undone.",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try { licenseDAO.delete(selectedLicense.getLicenseId()); showInfo("License deleted."); handleClear(); loadData(); }
                catch (SQLException e) { showError("Delete failed: " + e.getMessage()); }
            }
        });
    }

    @FXML private void handleClear() {
        cmbCustomer.setValue(null);
        txtLicenseNo.clear(); txtRestriction.clear(); txtConditions.clear();
        cmbLicenseType.setValue(null); cmbStatus.setValue(null);
        dpIssueDate.setValue(null); dpExpiryDate.setValue(null);
        selectedLicense = null; tblLicenses.getSelectionModel().clearSelection();
        setMode(false, null);
    }

    private void loadCustomers() {
        try { cmbCustomer.setItems(FXCollections.observableArrayList(customerDAO.findAll())); }
        catch (SQLException e) { showError("Failed to load customers: " + e.getMessage()); }
    }

    private void loadData() {
        try { licenseList.setAll(licenseDAO.findAll()); updateRecordCount(); }
        catch (SQLException e) { showError("Failed to load licenses: " + e.getMessage()); }
    }

    @FXML private void handleExport() {
        try {
            java.io.File file = new java.io.File(System.getProperty("user.home"), "Licenses_Export.csv");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("ID,License No,Type,Issue Date,Expiry Date,Restrictions,Conditions,Status");
                for (DriversLicense l : licenseList)
                    pw.println(l.getLicenseId() + "," + safe(l.getLicenseNumber()) + "," + safe(l.getLicenseType()) + ","
                        + (l.getIssueDate() != null ? l.getIssueDate() : "") + ","
                        + (l.getExpiryDate() != null ? l.getExpiryDate() : "") + ","
                        + safe(l.getRestrictionCode()) + "," + safe(l.getConditions()) + "," + safe(l.getStatus()));
            }
            showInfo("Exported to " + file.getAbsolutePath());
        } catch (Exception e) { showError("Export failed: " + e.getMessage()); }
    }

    private String safe(String s) { return s != null ? s : ""; }

    private DriversLicense buildFromForm() {
        DriversLicense dl = new DriversLicense();
        Customer cust = cmbCustomer.getValue();
        if (cust != null) dl.setCustomerId(cust.getCustomerId());
        dl.setLicenseNumber(txtLicenseNo.getText().trim());
        dl.setLicenseType(cmbLicenseType.getValue());
        dl.setIssueDate(dpIssueDate.getValue());
        dl.setExpiryDate(dpExpiryDate.getValue());
        dl.setRestrictionCode(txtRestriction.getText().trim());
        dl.setConditions(txtConditions.getText().trim());
        dl.setStatus(cmbStatus.getValue());
        return dl;
    }

    private void populateForm(DriversLicense dl) {
        try { cmbCustomer.setValue(customerDAO.findById(dl.getCustomerId())); } catch (SQLException ignored) {}
        txtLicenseNo.setText(safe(dl.getLicenseNumber()));
        cmbLicenseType.setValue(dl.getLicenseType());
        dpIssueDate.setValue(dl.getIssueDate());
        dpExpiryDate.setValue(dl.getExpiryDate());
        txtRestriction.setText(safe(dl.getRestrictionCode()));
        txtConditions.setText(safe(dl.getConditions()));
        cmbStatus.setValue(dl.getStatus());
    }

    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
