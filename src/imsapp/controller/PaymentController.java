package imsapp.controller;

import imsapp.dao.CustomerDAO;
import imsapp.dao.PaymentDAO;
import imsapp.dao.VehicleDAO;
import imsapp.model.Customer;
import imsapp.model.Payment;
import imsapp.model.Vehicle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

/**
 * Controller for the Payments / Official Receipts module.
 * Contains the "Print Receipt" action that generates a PDF and opens it.
 */
public class PaymentController implements Initializable {

    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private ComboBox<Vehicle> cmbVehicle;
    @FXML private ComboBox<String> cmbPaymentType, cmbPaymentMethod;
    @FXML private TextField txtAmount, txtRemarks;
    @FXML private TableView<Payment> tblPayments;

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final ObservableList<Payment> paymentList = FXCollections.observableArrayList();
    private Payment selectedPayment;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbPaymentType.setItems(FXCollections.observableArrayList(
                "Registration", "Renewal", "License", "Other"));
        cmbPaymentMethod.setItems(FXCollections.observableArrayList(
                "Cash", "Credit Card", "GCash", "Bank Transfer"));

        tblPayments.setItems(paymentList);
        loadCustomers();
        loadData();

        // When a customer is selected, load their vehicles into the vehicle combo
        cmbCustomer.setOnAction(e -> {
            Customer cust = cmbCustomer.getValue();
            if (cust != null) {
                try {
                    cmbVehicle.setItems(FXCollections.observableArrayList(
                            vehicleDAO.findByCustomerId(cust.getCustomerId())));
                } catch (SQLException ex) {
                    showError("Failed to load vehicles: " + ex.getMessage());
                }
            }
        });

        tblPayments.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedPayment = n;
                populateForm(n);
            }
        });
    }

    /**
     * Processes a new payment: validates input, generates a transaction ID,
     * persists to the database, and refreshes the table.
     */
    private boolean validateInput() {
        if (cmbCustomer.getValue() == null) {
            showError("Customer selection is required.");
            cmbCustomer.requestFocus();
            return false;
        }
        if (cmbPaymentType.getValue() == null) {
            showError("Payment Type is required.");
            cmbPaymentType.requestFocus();
            return false;
        }
        if (txtAmount.getText() == null || txtAmount.getText().trim().isEmpty()) {
            showError("Amount is required.");
            txtAmount.requestFocus();
            return false;
        }
        try {
            Double.parseDouble(txtAmount.getText());
        } catch (NumberFormatException e) {
            showError("Amount must be a valid number.");
            txtAmount.requestFocus();
            return false;
        }
        return true;
    }

    @FXML
    private void handleSavePayment() {
        if (!validateInput()) return;
        
        Customer cust = cmbCustomer.getValue();
        Vehicle veh = cmbVehicle.getValue();

        try {
            double amount = Double.parseDouble(txtAmount.getText());

            Payment p = new Payment();
            if (selectedPayment == null) {
                p.setTransactionId(paymentDAO.generateTransactionId());
                p.setPaymentDate(LocalDateTime.now());
            } else {
                p.setPaymentId(selectedPayment.getPaymentId());
                p.setTransactionId(selectedPayment.getTransactionId());
                p.setPaymentDate(selectedPayment.getPaymentDate());
            }

            p.setCustomerId(cust.getCustomerId());
            if (veh != null) p.setVehicleId(veh.getVehicleId());
            p.setPaymentType(cmbPaymentType.getValue());
            p.setAmount(amount);
            p.setPaymentMethod(cmbPaymentMethod.getValue());
            p.setRemarks(txtRemarks.getText());

            if (selectedPayment == null) {
                paymentDAO.insert(p);
                showInfo("Payment saved!\nTransaction ID: " + p.getTransactionId());
            } else {
                paymentDAO.update(p);
                showInfo("Payment updated!");
            }
            
            handleClear();
            loadData();
        } catch (NumberFormatException e) {
            showError("Amount must be a valid number.");
        } catch (SQLException e) {
            showError("Payment operation failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedPayment == null) { showError("Select a payment first."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete payment " + selectedPayment.getTransactionId() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    paymentDAO.delete(selectedPayment.getPaymentId());
                    showInfo("Payment deleted.");
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
        cmbVehicle.setValue(null);
        cmbPaymentType.setValue(null);
        cmbPaymentMethod.setValue(null);
        txtAmount.clear();
        txtRemarks.clear();
        selectedPayment = null;
        tblPayments.getSelectionModel().clearSelection();
    }

    private void loadCustomers() {
        try {
            cmbCustomer.setItems(FXCollections.observableArrayList(customerDAO.findAll()));
        } catch (SQLException e) {
            showError("Failed to load customers: " + e.getMessage());
        }
    }

    @FXML private TextField txtSearch;
    private javafx.collections.transformation.FilteredList<Payment> filteredData;

    private void loadData() {
        try {
            paymentList.setAll(paymentDAO.findAll());
            filteredData = new javafx.collections.transformation.FilteredList<>(paymentList, p -> true);
            tblPayments.setItems(filteredData);
            
            if (txtSearch != null) {
                txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredData.setPredicate(payment -> {
                        if (newValue == null || newValue.isEmpty()) return true;
                        String lowerCaseFilter = newValue.toLowerCase();
                        if (payment.getTransactionId() != null && payment.getTransactionId().toLowerCase().contains(lowerCaseFilter)) return true;
                        if (payment.getPaymentType() != null && payment.getPaymentType().toLowerCase().contains(lowerCaseFilter)) return true;
                        return false;
                    });
                });
            }
        } catch (SQLException e) {
            showError("Failed to load payments: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        try {
            java.io.File file = new java.io.File(System.getProperty("user.home"), "Payments_Export.csv");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("Transaction ID,Type,Method,Amount,Date,Remarks");
                for (Payment p : paymentList) {
                    pw.println(p.getTransactionId() + "," + p.getPaymentType() + "," + p.getPaymentMethod() + "," 
                            + p.getAmount() + "," + p.getPaymentDate() + ",\"" + (p.getRemarks() == null ? "" : p.getRemarks()) + "\"");
                }
            }
            showInfo("Exported to " + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    private void populateForm(Payment p) {
        try {
            Customer c = customerDAO.findById(p.getCustomerId());
            cmbCustomer.setValue(c);
            if (p.getVehicleId() > 0) {
                Vehicle v = vehicleDAO.findById(p.getVehicleId());
                // Reload vehicles for this customer first
                cmbVehicle.setItems(FXCollections.observableArrayList(
                        vehicleDAO.findByCustomerId(p.getCustomerId())));
                cmbVehicle.setValue(v);
            }
        } catch (SQLException ignored) {}
        cmbPaymentType.setValue(p.getPaymentType());
        cmbPaymentMethod.setValue(p.getPaymentMethod());
        txtAmount.setText(String.format("%.2f", p.getAmount()));
        txtRemarks.setText(p.getRemarks());
    }

    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
