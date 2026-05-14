package imsapp.model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Model class representing a Vehicle record (OR/CR).
 */
public class Vehicle {

    private final IntegerProperty vehicleId = new SimpleIntegerProperty();
    private final IntegerProperty customerId = new SimpleIntegerProperty();
    private final StringProperty plateNumber = new SimpleStringProperty();
    private final StringProperty engineNumber = new SimpleStringProperty();
    private final StringProperty chassisNumber = new SimpleStringProperty();
    private final StringProperty make = new SimpleStringProperty();
    private final StringProperty model = new SimpleStringProperty();
    private final IntegerProperty yearModel = new SimpleIntegerProperty();
    private final StringProperty color = new SimpleStringProperty();
    private final StringProperty bodyType = new SimpleStringProperty();
    private final StringProperty fuelType = new SimpleStringProperty();
    private final StringProperty mvFileNumber = new SimpleStringProperty();
    private final StringProperty orNumber = new SimpleStringProperty();
    private final StringProperty crNumber = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> registrationDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> expiryDate = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty();

    // Transient reference
    private Customer customer;

    public Vehicle() {}

    // --- vehicleId ---
    public int getVehicleId() { return vehicleId.get(); }
    public void setVehicleId(int value) { vehicleId.set(value); }
    public IntegerProperty vehicleIdProperty() { return vehicleId; }

    // --- customerId ---
    public int getCustomerId() { return customerId.get(); }
    public void setCustomerId(int value) { customerId.set(value); }
    public IntegerProperty customerIdProperty() { return customerId; }

    // --- plateNumber ---
    public String getPlateNumber() { return plateNumber.get(); }
    public void setPlateNumber(String value) { plateNumber.set(value); }
    public StringProperty plateNumberProperty() { return plateNumber; }

    // --- engineNumber ---
    public String getEngineNumber() { return engineNumber.get(); }
    public void setEngineNumber(String value) { engineNumber.set(value); }
    public StringProperty engineNumberProperty() { return engineNumber; }

    // --- chassisNumber ---
    public String getChassisNumber() { return chassisNumber.get(); }
    public void setChassisNumber(String value) { chassisNumber.set(value); }
    public StringProperty chassisNumberProperty() { return chassisNumber; }

    // --- make ---
    public String getMake() { return make.get(); }
    public void setMake(String value) { make.set(value); }
    public StringProperty makeProperty() { return make; }

    // --- model ---
    public String getModel() { return model.get(); }
    public void setModel(String value) { model.set(value); }
    public StringProperty modelProperty() { return model; }

    // --- yearModel ---
    public int getYearModel() { return yearModel.get(); }
    public void setYearModel(int value) { yearModel.set(value); }
    public IntegerProperty yearModelProperty() { return yearModel; }

    // --- color ---
    public String getColor() { return color.get(); }
    public void setColor(String value) { color.set(value); }
    public StringProperty colorProperty() { return color; }

    // --- bodyType ---
    public String getBodyType() { return bodyType.get(); }
    public void setBodyType(String value) { bodyType.set(value); }
    public StringProperty bodyTypeProperty() { return bodyType; }

    // --- fuelType ---
    public String getFuelType() { return fuelType.get(); }
    public void setFuelType(String value) { fuelType.set(value); }
    public StringProperty fuelTypeProperty() { return fuelType; }

    // --- mvFileNumber ---
    public String getMvFileNumber() { return mvFileNumber.get(); }
    public void setMvFileNumber(String value) { mvFileNumber.set(value); }
    public StringProperty mvFileNumberProperty() { return mvFileNumber; }

    // --- orNumber ---
    public String getOrNumber() { return orNumber.get(); }
    public void setOrNumber(String value) { orNumber.set(value); }
    public StringProperty orNumberProperty() { return orNumber; }

    // --- crNumber ---
    public String getCrNumber() { return crNumber.get(); }
    public void setCrNumber(String value) { crNumber.set(value); }
    public StringProperty crNumberProperty() { return crNumber; }

    // --- registrationDate ---
    public LocalDate getRegistrationDate() { return registrationDate.get(); }
    public void setRegistrationDate(LocalDate value) { registrationDate.set(value); }
    public ObjectProperty<LocalDate> registrationDateProperty() { return registrationDate; }

    // --- expiryDate ---
    public LocalDate getExpiryDate() { return expiryDate.get(); }
    public void setExpiryDate(LocalDate value) { expiryDate.set(value); }
    public ObjectProperty<LocalDate> expiryDateProperty() { return expiryDate; }

    // --- status ---
    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    // --- customer (transient) ---
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    /**
     * Returns a short vehicle description.
     */
    public String getDescription() {
        return yearModel.get() + " " + make.get() + " " + model.get();
    }

    @Override
    public String toString() {
        return plateNumber.get() + " - " + getDescription();
    }
}
