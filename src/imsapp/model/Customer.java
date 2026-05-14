package imsapp.model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Model class representing a Customer record.
 */
public class Customer {

    private final IntegerProperty customerId = new SimpleIntegerProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty middleName = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateOfBirth = new SimpleObjectProperty<>();
    private final StringProperty gender = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty city = new SimpleStringProperty();
    private final StringProperty province = new SimpleStringProperty();
    private final StringProperty zipCode = new SimpleStringProperty();
    private final StringProperty contactNumber = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();

    public Customer() {}

    public Customer(int customerId, String firstName, String lastName, String middleName,
                    LocalDate dateOfBirth, String gender, String address, String city,
                    String province, String zipCode, String contactNumber, String email) {
        this.customerId.set(customerId);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.middleName.set(middleName);
        this.dateOfBirth.set(dateOfBirth);
        this.gender.set(gender);
        this.address.set(address);
        this.city.set(city);
        this.province.set(province);
        this.zipCode.set(zipCode);
        this.contactNumber.set(contactNumber);
        this.email.set(email);
    }

    // --- customerId ---
    public int getCustomerId() { return customerId.get(); }
    public void setCustomerId(int value) { customerId.set(value); }
    public IntegerProperty customerIdProperty() { return customerId; }

    // --- firstName ---
    public String getFirstName() { return firstName.get(); }
    public void setFirstName(String value) { firstName.set(value); }
    public StringProperty firstNameProperty() { return firstName; }

    // --- lastName ---
    public String getLastName() { return lastName.get(); }
    public void setLastName(String value) { lastName.set(value); }
    public StringProperty lastNameProperty() { return lastName; }

    // --- middleName ---
    public String getMiddleName() { return middleName.get(); }
    public void setMiddleName(String value) { middleName.set(value); }
    public StringProperty middleNameProperty() { return middleName; }

    // --- dateOfBirth ---
    public LocalDate getDateOfBirth() { return dateOfBirth.get(); }
    public void setDateOfBirth(LocalDate value) { dateOfBirth.set(value); }
    public ObjectProperty<LocalDate> dateOfBirthProperty() { return dateOfBirth; }

    // --- gender ---
    public String getGender() { return gender.get(); }
    public void setGender(String value) { gender.set(value); }
    public StringProperty genderProperty() { return gender; }

    // --- address ---
    public String getAddress() { return address.get(); }
    public void setAddress(String value) { address.set(value); }
    public StringProperty addressProperty() { return address; }

    // --- city ---
    public String getCity() { return city.get(); }
    public void setCity(String value) { city.set(value); }
    public StringProperty cityProperty() { return city; }

    // --- province ---
    public String getProvince() { return province.get(); }
    public void setProvince(String value) { province.set(value); }
    public StringProperty provinceProperty() { return province; }

    // --- zipCode ---
    public String getZipCode() { return zipCode.get(); }
    public void setZipCode(String value) { zipCode.set(value); }
    public StringProperty zipCodeProperty() { return zipCode; }

    // --- contactNumber ---
    public String getContactNumber() { return contactNumber.get(); }
    public void setContactNumber(String value) { contactNumber.set(value); }
    public StringProperty contactNumberProperty() { return contactNumber; }

    // --- email ---
    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }

    /**
     * Returns full name in "Last, First Middle" format.
     */
    public String getFullName() {
        String mid = (middleName.get() != null && !middleName.get().isEmpty())
                ? " " + middleName.get() : "";
        return lastName.get() + ", " + firstName.get() + mid;
    }

    /**
     * Returns full address including city, province, and zip.
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder(address.get());
        if (city.get() != null && !city.get().isEmpty()) sb.append(", ").append(city.get());
        if (province.get() != null && !province.get().isEmpty()) sb.append(", ").append(province.get());
        if (zipCode.get() != null && !zipCode.get().isEmpty()) sb.append(" ").append(zipCode.get());
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
