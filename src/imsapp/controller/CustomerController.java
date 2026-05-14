package imsapp.controller;

import imsapp.dao.CustomerDAO;
import imsapp.model.Customer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for Customer Records module.
 */
public class CustomerController implements Initializable {

    @FXML private TextField txtFirstName, txtLastName, txtMiddleName;
    @FXML private TextField txtContact, txtEmail, txtAddress;
    @FXML private TextField txtCity, txtProvince, txtZipCode;
    @FXML private ComboBox<String> cmbGender;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private TableView<Customer> tblCustomers;

    private final CustomerDAO dao = new CustomerDAO();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private Customer selectedCustomer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        tblCustomers.setItems(customerList);
        loadData();

        // Row selection listener — populate form fields
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedCustomer = newVal;
                populateForm(newVal);
            }
        });
    }

    private boolean validateInput() {
        if (txtFirstName.getText() == null || txtFirstName.getText().trim().isEmpty()) {
            showError("First Name is required.");
            txtFirstName.requestFocus();
            return false;
        }
        if (txtLastName.getText() == null || txtLastName.getText().trim().isEmpty()) {
            showError("Last Name is required.");
            txtLastName.requestFocus();
            return false;
        }
        return true;
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;
        try {
            Customer c = buildFromForm();
            dao.insert(c);
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
            showError("Please select a customer to update.");
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
            showError("Please select a customer to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete customer " + selectedCustomer.getFullName() + "?",
                ButtonType.YES, ButtonType.NO);
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

    @FXML private TextField txtSearch;
    private javafx.collections.transformation.FilteredList<Customer> filteredData;

    @FXML
    private void handleClear() {
        txtFirstName.clear(); txtLastName.clear(); txtMiddleName.clear();
        txtContact.clear(); txtEmail.clear(); txtAddress.clear();
        txtCity.clear(); txtProvince.clear(); txtZipCode.clear();
        cmbGender.setValue(null);
        dpDateOfBirth.setValue(null);
        selectedCustomer = null;
        tblCustomers.getSelectionModel().clearSelection();
        txtFirstName.setStyle("-fx-border-color: none;");
        txtLastName.setStyle("-fx-border-color: none;");
    }

    private void loadData() {
        try {
            customerList.setAll(dao.findAll());
            filteredData = new javafx.collections.transformation.FilteredList<>(customerList, p -> true);
            tblCustomers.setItems(filteredData);
            
            if (txtSearch != null) {
                txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredData.setPredicate(customer -> {
                        if (newValue == null || newValue.isEmpty()) return true;
                        String lowerCaseFilter = newValue.toLowerCase();
                        if (customer.getFirstName().toLowerCase().contains(lowerCaseFilter)) return true;
                        if (customer.getLastName().toLowerCase().contains(lowerCaseFilter)) return true;
                        return false;
                    });
                });
            }
        } catch (SQLException e) {
            showError("Failed to load customers: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        try {
            java.io.File file = new java.io.File(System.getProperty("user.home"), "Customers_Export.csv");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("ID,First Name,Last Name,Gender,Contact,Address");
                for (Customer c : customerList) {
                    pw.println(c.getCustomerId() + "," + c.getFirstName() + "," + c.getLastName() + "," 
                            + c.getGender() + "," + c.getContactNumber() + ",\"" + c.getAddress() + "\"");
                }
            }
            showInfo("Exported to " + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    private Customer buildFromForm() {
        Customer c = new Customer();
        c.setFirstName(txtFirstName.getText());
        c.setLastName(txtLastName.getText());
        c.setMiddleName(txtMiddleName.getText());
        c.setDateOfBirth(dpDateOfBirth.getValue());
        c.setGender(cmbGender.getValue());
        c.setAddress(txtAddress.getText());
        c.setCity(txtCity.getText());
        c.setProvince(txtProvince.getText());
        c.setZipCode(txtZipCode.getText());
        c.setContactNumber(txtContact.getText());
        c.setEmail(txtEmail.getText());
        return c;
    }

    private void populateForm(Customer c) {
        txtFirstName.setText(c.getFirstName());
        txtLastName.setText(c.getLastName());
        txtMiddleName.setText(c.getMiddleName());
        dpDateOfBirth.setValue(c.getDateOfBirth());
        cmbGender.setValue(c.getGender());
        txtAddress.setText(c.getAddress());
        txtCity.setText(c.getCity());
        txtProvince.setText(c.getProvince());
        txtZipCode.setText(c.getZipCode());
        txtContact.setText(c.getContactNumber());
        txtEmail.setText(c.getEmail());
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
