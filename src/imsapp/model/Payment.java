package imsapp.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Model class representing a Payment / Official Receipt.
 */
public class Payment {

    private final IntegerProperty paymentId = new SimpleIntegerProperty();
    private final StringProperty transactionId = new SimpleStringProperty();
    private final IntegerProperty customerId = new SimpleIntegerProperty();
    private final IntegerProperty vehicleId = new SimpleIntegerProperty();
    private final IntegerProperty licenseId = new SimpleIntegerProperty();
    private final StringProperty paymentType = new SimpleStringProperty();
    private final DoubleProperty amount = new SimpleDoubleProperty();
    private final StringProperty paymentMethod = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> paymentDate = new SimpleObjectProperty<>();
    private final StringProperty remarks = new SimpleStringProperty();

    // Transient references
    private Customer customer;
    private Vehicle vehicle;

    public Payment() {}

    // --- paymentId ---
    public int getPaymentId() { return paymentId.get(); }
    public void setPaymentId(int value) { paymentId.set(value); }
    public IntegerProperty paymentIdProperty() { return paymentId; }

    // --- transactionId ---
    public String getTransactionId() { return transactionId.get(); }
    public void setTransactionId(String value) { transactionId.set(value); }
    public StringProperty transactionIdProperty() { return transactionId; }

    // --- customerId ---
    public int getCustomerId() { return customerId.get(); }
    public void setCustomerId(int value) { customerId.set(value); }
    public IntegerProperty customerIdProperty() { return customerId; }

    // --- vehicleId ---
    public int getVehicleId() { return vehicleId.get(); }
    public void setVehicleId(int value) { vehicleId.set(value); }
    public IntegerProperty vehicleIdProperty() { return vehicleId; }

    // --- licenseId ---
    public int getLicenseId() { return licenseId.get(); }
    public void setLicenseId(int value) { licenseId.set(value); }
    public IntegerProperty licenseIdProperty() { return licenseId; }

    // --- paymentType ---
    public String getPaymentType() { return paymentType.get(); }
    public void setPaymentType(String value) { paymentType.set(value); }
    public StringProperty paymentTypeProperty() { return paymentType; }

    // --- amount ---
    public double getAmount() { return amount.get(); }
    public void setAmount(double value) { amount.set(value); }
    public DoubleProperty amountProperty() { return amount; }

    // --- paymentMethod ---
    public String getPaymentMethod() { return paymentMethod.get(); }
    public void setPaymentMethod(String value) { paymentMethod.set(value); }
    public StringProperty paymentMethodProperty() { return paymentMethod; }

    // --- paymentDate ---
    public LocalDateTime getPaymentDate() { return paymentDate.get(); }
    public void setPaymentDate(LocalDateTime value) { paymentDate.set(value); }
    public ObjectProperty<LocalDateTime> paymentDateProperty() { return paymentDate; }

    // --- remarks ---
    public String getRemarks() { return remarks.get(); }
    public void setRemarks(String value) { remarks.set(value); }
    public StringProperty remarksProperty() { return remarks; }

    // --- customer (transient) ---
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    // --- vehicle (transient) ---
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    @Override
    public String toString() {
        return transactionId.get() + " - PHP " + String.format("%.2f", amount.get());
    }
}
