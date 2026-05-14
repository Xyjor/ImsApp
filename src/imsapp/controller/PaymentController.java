package imsapp.controller;

import imsapp.dao.CustomerDAO;
import imsapp.dao.PaymentDAO;
import imsapp.dao.VehicleDAO;
import imsapp.model.Customer;
import imsapp.model.Payment;
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
import java.time.LocalDateTime;
import java.util.ResourceBundle;

/**
 * Payments controller — purely a data management module.
 * Fixes: #2/#3 dynamic button label, single search listener, #6 mode label,
 * #9 record count, #11 double-click edit, #13 Ctrl+S, #14 null-safe CSV.
 */
public class PaymentController implements Initializable {

    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private ComboBox<Vehicle>  cmbVehicle;
    @FXML private ComboBox<String>   cmbPaymentType, cmbPaymentMethod;
    @FXML private TextField          txtAmount, txtRemarks;
    @FXML private TableView<Payment> tblPayments;
    @FXML private TextField          txtSearch;
    @FXML private Label              lblMode, lblRecordCount;
    @FXML private Button             btnSavePayment;
    @FXML private Button             btnDelete;

    private final PaymentDAO  paymentDAO  = new PaymentDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final VehicleDAO  vehicleDAO  = new VehicleDAO();
    private final ObservableList<Payment> paymentList = FXCollections.observableArrayList();
    private FilteredList<Payment> filteredData;
    private Payment selectedPayment;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbPaymentType.setItems(FXCollections.observableArrayList("Registration","Renewal","License","Other"));
        cmbPaymentMethod.setItems(FXCollections.observableArrayList("Cash","Credit Card","GCash","Bank Transfer"));

        filteredData = new FilteredList<>(paymentList, p -> true);
        tblPayments.setItems(filteredData);

        loadCustomers();
        loadData();
        setupSearch();
        setupDoubleClick();
        setupShortcuts();
        setMode(false, null);
        if (btnDelete != null) btnDelete.setDisable(!imsapp.util.SessionManager.isAdmin());

        // Cascade: when customer changes, reload their vehicles
        cmbCustomer.setOnAction(e -> {
            Customer cust = cmbCustomer.getValue();
            if (cust != null) {
                try {
                    cmbVehicle.setItems(FXCollections.observableArrayList(
                        vehicleDAO.findByCustomerId(cust.getCustomerId())));
                } catch (SQLException ex) { showError("Failed to load vehicles: " + ex.getMessage()); }
            } else {
                cmbVehicle.setItems(FXCollections.observableArrayList());
            }
        });
    }

    // Fix #3: search listener set up once
    private void setupSearch() {
        if (txtSearch == null) return;
        txtSearch.textProperty().addListener((obs, o, newVal) -> {
            filteredData.setPredicate(p -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String f = newVal.toLowerCase();
                return safe(p.getTransactionId()).toLowerCase().contains(f)
                    || safe(p.getPaymentType()).toLowerCase().contains(f)
                    || safe(p.getPaymentMethod()).toLowerCase().contains(f);
            });
            updateRecordCount();
        });
    }

    // Fix #11: double-click to edit
    private void setupDoubleClick() {
        tblPayments.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                Payment sel = tblPayments.getSelectionModel().getSelectedItem();
                if (sel != null) { selectedPayment = sel; populateForm(sel); setMode(true, sel); }
            }
        });
    }

    // Fix #13: Ctrl+S shortcut
    private void setupShortcuts() {
        tblPayments.sceneProperty().addListener((obs, o, newScene) -> {
            if (newScene != null)
                newScene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                    this::handleSavePayment);
        });
    }

    // Fix #2 + #6: dynamic mode label AND button text
    private void setMode(boolean editing, Payment p) {
        if (lblMode != null) {
            if (editing && p != null) {
                lblMode.setText("✏  Editing: " + safe(p.getTransactionId()));
                lblMode.setStyle("-fx-text-fill:#38bdf8;-fx-font-style:italic;");
            } else {
                lblMode.setText("＋  New Payment Record");
                lblMode.setStyle("-fx-text-fill:#64748b;-fx-font-style:italic;");
            }
        }
        // Fix #2: change button label dynamically
        if (btnSavePayment != null) {
            btnSavePayment.setText(editing ? " Update Payment" : " Save Payment");
        }
    }

    // Fix #9: record count
    private void updateRecordCount() {
        if (lblRecordCount == null) return;
        int shown = filteredData.size(), total = paymentList.size();
        lblRecordCount.setText(shown == total
            ? "Showing " + total + " record" + (total == 1 ? "" : "s")
            : "Showing " + shown + " of " + total + " records");
    }

    private boolean validateInput() {
        if (cmbCustomer.getValue() == null) { showError("Customer is required."); cmbCustomer.requestFocus(); return false; }
        if (cmbPaymentType.getValue() == null) { showError("Payment Type is required."); cmbPaymentType.requestFocus(); return false; }
        if (txtAmount.getText().trim().isEmpty()) { showError("Amount is required."); txtAmount.requestFocus(); return false; }
        try { Double.parseDouble(txtAmount.getText()); }
        catch (NumberFormatException e) { showError("Amount must be a valid number."); txtAmount.requestFocus(); return false; }
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
            p.setRemarks(txtRemarks.getText().trim());

            if (selectedPayment == null) {
                paymentDAO.insert(p);
                showInfo("Payment saved!\nTransaction ID: " + p.getTransactionId());
            } else {
                paymentDAO.update(p);
                showInfo("Payment updated!");
            }
            handleClear();
            loadData();
        } catch (NumberFormatException e) { showError("Amount must be a valid number."); }
        catch (SQLException e) { showError("Payment operation failed: " + e.getMessage()); }
    }

    @FXML
    private void handleDelete() {
        if (selectedPayment == null) { showError("Double-click a row to select a payment for deletion."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete payment: " + selectedPayment.getTransactionId() + "?\nThis cannot be undone.",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try { paymentDAO.delete(selectedPayment.getPaymentId()); showInfo("Payment deleted."); handleClear(); loadData(); }
                catch (SQLException e) { showError("Delete failed: " + e.getMessage()); }
            }
        });
    }

    @FXML
    private void handleClear() {
        cmbCustomer.setValue(null); cmbVehicle.setValue(null);
        cmbPaymentType.setValue(null); cmbPaymentMethod.setValue(null);
        txtAmount.clear(); txtRemarks.clear();
        selectedPayment = null;
        tblPayments.getSelectionModel().clearSelection();
        setMode(false, null);
    }

    private void loadCustomers() {
        try { cmbCustomer.setItems(FXCollections.observableArrayList(customerDAO.findAll())); }
        catch (SQLException e) { showError("Failed to load customers: " + e.getMessage()); }
    }

    private void loadData() {
        try { paymentList.setAll(paymentDAO.findAll()); updateRecordCount(); }
        catch (SQLException e) { showError("Failed to load payments: " + e.getMessage()); }
    }

    // Fix #14: null-safe CSV
    @FXML
    private void handleExport() {
        try {
            java.io.File file = new java.io.File(System.getProperty("user.home"), "Payments_Export.csv");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("Transaction ID,Type,Method,Amount,Date,Remarks");
                for (Payment p : paymentList)
                    pw.println(safe(p.getTransactionId()) + "," + safe(p.getPaymentType()) + ","
                        + safe(p.getPaymentMethod()) + "," + p.getAmount() + ","
                        + (p.getPaymentDate() != null ? p.getPaymentDate() : "") + ",\""
                        + safe(p.getRemarks()) + "\"");
            }
            showInfo("Exported to " + file.getAbsolutePath());
        } catch (Exception e) { showError("Export failed: " + e.getMessage()); }
    }

    private String safe(String s) { return s != null ? s : ""; }

    private void populateForm(Payment p) {
        try {
            Customer c = customerDAO.findById(p.getCustomerId());
            cmbCustomer.setValue(c);
            if (p.getVehicleId() > 0) {
                cmbVehicle.setItems(FXCollections.observableArrayList(vehicleDAO.findByCustomerId(p.getCustomerId())));
                cmbVehicle.setValue(vehicleDAO.findById(p.getVehicleId()));
            }
        } catch (SQLException ignored) {}
        cmbPaymentType.setValue(p.getPaymentType());
        cmbPaymentMethod.setValue(p.getPaymentMethod());
        txtAmount.setText(String.format("%.2f", p.getAmount()));
        txtRemarks.setText(safe(p.getRemarks()));
    }

    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
