package imsapp.controller;

import imsapp.dao.CustomerDAO;
import imsapp.model.Customer;

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

/**
 * Controller for Customer Records module.
 * Improvements: fix #3 (single search listener), #6 (edit-mode label),
 * #9 (record count), #10 (status badges), #11 (double-click edit),
 * #13 (Ctrl+S), #14 (CSV null safety).
 */
public class CustomerController implements Initializable {

    @FXML private TextField txtFirstName, txtLastName, txtMiddleName;
    @FXML private TextField txtContact, txtEmail, txtAddress;
    @FXML private TextField txtCity, txtProvince, txtZipCode;
    @FXML private ComboBox<String> cmbGender;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TextField txtSearch;
    @FXML private Label lblMode;
    @FXML private Label lblRecordCount;
    @FXML private Button btnDelete;

    private final CustomerDAO dao = new CustomerDAO();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredData;
    private Customer selectedCustomer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));

        filteredData = new FilteredList<>(customerList, p -> true);
        tblCustomers.setItems(filteredData);

        loadData();
        setupSearch();       // Fix #3: listener added ONCE here, not in loadData()
        setupDoubleClick();  // Fix #11
        setupShortcuts();    // Fix #13
        setMode(false, null);// Fix #6
        // STAFF restriction: disable Delete
        if (btnDelete != null) btnDelete.setDisable(!imsapp.util.SessionManager.isAdmin());
    }

    // Fix #3: Search listener set up once, operates on the persistent FilteredList
    private void setupSearch() {
        if (txtSearch == null) return;
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(c -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String f = newVal.toLowerCase();
                return safe(c.getFirstName()).toLowerCase().contains(f)
                    || safe(c.getLastName()).toLowerCase().contains(f)
                    || safe(c.getContactNumber()).toLowerCase().contains(f)
                    || safe(c.getEmail()).toLowerCase().contains(f);
            });
            updateRecordCount();
        });
    }

    // Fix #11: Double-click row to populate form for editing
    private void setupDoubleClick() {
        tblCustomers.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Customer sel = tblCustomers.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    selectedCustomer = sel;
                    populateForm(sel);
                    setMode(true, sel);
                }
            }
        });
    }

    // Fix #13: Ctrl+S shortcut for Save
    private void setupShortcuts() {
        tblCustomers.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                    this::handleSave);
            }
        });
    }

    // Fix #6: Edit-mode indicator
    private void setMode(boolean editing, Customer c) {
        if (lblMode == null) return;
        if (editing && c != null) {
            lblMode.setText("✏  Editing: " + c.getFullName());
            lblMode.setStyle("-fx-text-fill: #38bdf8; -fx-font-style: italic;");
        } else {
            lblMode.setText("＋  New Record");
            lblMode.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
        }
    }

    // Fix #9: Record count label
    private void updateRecordCount() {
        if (lblRecordCount == null) return;
        int shown = filteredData.size();
        int total = customerList.size();
        lblRecordCount.setText(shown == total
            ? "Showing " + total + " record" + (total == 1 ? "" : "s")
            : "Showing " + shown + " of " + total + " records");
    }

    private boolean validateInput() {
        if (txtFirstName.getText() == null || txtFirstName.getText().trim().isEmpty()) {
            showError("First Name is required."); txtFirstName.requestFocus(); return false;
        }
        if (txtLastName.getText() == null || txtLastName.getText().trim().isEmpty()) {
            showError("Last Name is required."); txtLastName.requestFocus(); return false;
        }
        return true;
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;
        try {
            dao.insert(buildFromForm());
            showInfo("Customer saved successfully.");
            handleClear();
            loadData();
        } catch (SQLException e) {
            showError("Save failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedCustomer == null) {
            showError("Double-click a row in the table to select a customer for editing.");
            return;
        }
        if (!validateInput()) return;
        try {
            Customer c = buildFromForm();
            c.setCustomerId(selectedCustomer.getCustomerId());
            dao.update(c);
            showInfo("Customer updated successfully.");
            handleClear();
            loadData();
        } catch (SQLException e) {
            showError("Update failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedCustomer == null) {
            showError("Double-click a row in the table to select a customer for deletion.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete customer: " + selectedCustomer.getFullName() + "?\nThis cannot be undone.",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    dao.delete(selectedCustomer.getCustomerId());
                    showInfo("Customer deleted.");
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
        txtFirstName.clear(); txtLastName.clear(); txtMiddleName.clear();
        txtContact.clear(); txtEmail.clear(); txtAddress.clear();
        txtCity.clear(); txtProvince.clear(); txtZipCode.clear();
        cmbGender.setValue(null);
        dpDateOfBirth.setValue(null);
        selectedCustomer = null;
        tblCustomers.getSelectionModel().clearSelection();
        setMode(false, null);
    }

    private void loadData() {
        try {
            customerList.setAll(dao.findAll());
            updateRecordCount();
        } catch (SQLException e) {
            showError("Failed to load customers: " + e.getMessage());
        }
    }

    // Fix #14: Null-safe CSV export
    @FXML
    private void handleExport() {
        try {
            java.io.File file = new java.io.File(System.getProperty("user.home"), "Customers_Export.csv");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("ID,First Name,Last Name,Gender,Date of Birth,Contact,Email,Address,City,Province,Zip");
                for (Customer c : customerList) {
                    pw.println(c.getCustomerId() + ","
                        + safe(c.getFirstName()) + ","
                        + safe(c.getLastName()) + ","
                        + safe(c.getGender()) + ","
                        + (c.getDateOfBirth() != null ? c.getDateOfBirth() : "") + ","
                        + safe(c.getContactNumber()) + ","
                        + safe(c.getEmail()) + ",\""
                        + safe(c.getAddress()) + "\","
                        + safe(c.getCity()) + ","
                        + safe(c.getProvince()) + ","
                        + safe(c.getZipCode()));
                }
            }
            showInfo("Exported to " + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    private String safe(String s) { return s != null ? s : ""; }

    private Customer buildFromForm() {
        Customer c = new Customer();
        c.setFirstName(txtFirstName.getText().trim());
        c.setLastName(txtLastName.getText().trim());
        c.setMiddleName(txtMiddleName.getText().trim());
        c.setDateOfBirth(dpDateOfBirth.getValue());
        c.setGender(cmbGender.getValue());
        c.setAddress(txtAddress.getText().trim());
        c.setCity(txtCity.getText().trim());
        c.setProvince(txtProvince.getText().trim());
        c.setZipCode(txtZipCode.getText().trim());
        c.setContactNumber(txtContact.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        return c;
    }

    private void populateForm(Customer c) {
        txtFirstName.setText(safe(c.getFirstName()));
        txtLastName.setText(safe(c.getLastName()));
        txtMiddleName.setText(safe(c.getMiddleName()));
        dpDateOfBirth.setValue(c.getDateOfBirth());
        cmbGender.setValue(c.getGender());
        txtAddress.setText(safe(c.getAddress()));
        txtCity.setText(safe(c.getCity()));
        txtProvince.setText(safe(c.getProvince()));
        txtZipCode.setText(safe(c.getZipCode()));
        txtContact.setText(safe(c.getContactNumber()));
        txtEmail.setText(safe(c.getEmail()));
    }

    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
