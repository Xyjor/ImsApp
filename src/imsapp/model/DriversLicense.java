package imsapp.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.Period;

/**
 * Model class representing a Driver's License record.
 * Includes eligibility-check logic.
 */
public class DriversLicense {

    private final IntegerProperty licenseId = new SimpleIntegerProperty();
    private final IntegerProperty customerId = new SimpleIntegerProperty();
    private final StringProperty licenseNumber = new SimpleStringProperty();
    private final StringProperty licenseType = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> issueDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> expiryDate = new SimpleObjectProperty<>();
    private final StringProperty restrictionCode = new SimpleStringProperty();
    private final StringProperty conditions = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    // Transient reference for convenience
    private Customer customer;

    public DriversLicense() {}

    // --- licenseId ---
    public int getLicenseId() { return licenseId.get(); }
    public void setLicenseId(int value) { licenseId.set(value); }
    public IntegerProperty licenseIdProperty() { return licenseId; }

    // --- customerId ---
    public int getCustomerId() { return customerId.get(); }
    public void setCustomerId(int value) { customerId.set(value); }
    public IntegerProperty customerIdProperty() { return customerId; }

    // --- licenseNumber ---
    public String getLicenseNumber() { return licenseNumber.get(); }
    public void setLicenseNumber(String value) { licenseNumber.set(value); }
    public StringProperty licenseNumberProperty() { return licenseNumber; }

    // --- licenseType ---
    public String getLicenseType() { return licenseType.get(); }
    public void setLicenseType(String value) { licenseType.set(value); }
    public StringProperty licenseTypeProperty() { return licenseType; }

    // --- issueDate ---
    public LocalDate getIssueDate() { return issueDate.get(); }
    public void setIssueDate(LocalDate value) { issueDate.set(value); }
    public ObjectProperty<LocalDate> issueDateProperty() { return issueDate; }

    // --- expiryDate ---
    public LocalDate getExpiryDate() { return expiryDate.get(); }
    public void setExpiryDate(LocalDate value) { expiryDate.set(value); }
    public ObjectProperty<LocalDate> expiryDateProperty() { return expiryDate; }

    // --- restrictionCode ---
    public String getRestrictionCode() { return restrictionCode.get(); }
    public void setRestrictionCode(String value) { restrictionCode.set(value); }
    public StringProperty restrictionCodeProperty() { return restrictionCode; }

    // --- conditions ---
    public String getConditions() { return conditions.get(); }
    public void setConditions(String value) { conditions.set(value); }
    public StringProperty conditionsProperty() { return conditions; }

    // --- status ---
    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    // --- customer (transient) ---
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    // ================================================================
    //  ELIGIBILITY CHECKS
    // ================================================================

    /**
     * Checks if the license holder meets the minimum age requirement.
     * Student Permit: 16 years old.
     * Non-Professional: 17 years old.
     * Professional: 18 years old.
     *
     * @param dateOfBirth the customer's date of birth
     * @return true if the customer meets the age requirement for this license type
     */
    public boolean isAgeEligible(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return false;
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return switch (getLicenseType()) {
            case "Student"          -> age >= 16;
            case "Non-Professional" -> age >= 17;
            case "Professional"     -> age >= 18;
            default                 -> false;
        };
    }

    /**
     * Checks if the license is currently valid (not expired and status is Active).
     *
     * @return true if the license has not expired and its status is Active
     */
    public boolean isValid() {
        return "Active".equals(getStatus())
                && getExpiryDate() != null
                && !LocalDate.now().isAfter(getExpiryDate());
    }

    /**
     * Returns true if the license will expire within the given number of days.
     *
     * @param days number of days to look ahead
     * @return true if expiring within the window
     */
    public boolean isExpiringSoon(int days) {
        if (getExpiryDate() == null) return false;
        LocalDate threshold = LocalDate.now().plusDays(days);
        return getExpiryDate().isBefore(threshold) || getExpiryDate().isEqual(threshold);
    }

    @Override
    public String toString() {
        return licenseNumber.get() + " (" + licenseType.get() + ")";
    }
}
