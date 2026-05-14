package imsapp.model;

import javafx.beans.property.*;

/**
 * Model for an IMS system user (ADMIN or STAFF).
 */
public class User {

    private final IntegerProperty userId   = new SimpleIntegerProperty();
    private final StringProperty  username = new SimpleStringProperty();
    private final StringProperty  fullName = new SimpleStringProperty();
    private final StringProperty  passwordHash = new SimpleStringProperty();
    private final StringProperty  role     = new SimpleStringProperty();
    private final BooleanProperty active   = new SimpleBooleanProperty(true);

    public User() {}

    // userId
    public int getUserId() { return userId.get(); }
    public void setUserId(int v) { userId.set(v); }
    public IntegerProperty userIdProperty() { return userId; }

    // username
    public String getUsername() { return username.get(); }
    public void setUsername(String v) { username.set(v); }
    public StringProperty usernameProperty() { return username; }

    // fullName
    public String getFullName() { return fullName.get(); }
    public void setFullName(String v) { fullName.set(v); }
    public StringProperty fullNameProperty() { return fullName; }

    // passwordHash
    public String getPasswordHash() { return passwordHash.get(); }
    public void setPasswordHash(String v) { passwordHash.set(v); }
    public StringProperty passwordHashProperty() { return passwordHash; }

    // role
    public String getRole() { return role.get(); }
    public void setRole(String v) { role.set(v); }
    public StringProperty roleProperty() { return role; }

    // active
    public boolean isActive() { return active.get(); }
    public void setActive(boolean v) { active.set(v); }
    public BooleanProperty activeProperty() { return active; }

    @Override
    public String toString() {
        String fn = fullName.get();
        return (fn != null && !fn.isEmpty()) ? fn + " (" + username.get() + ")" : username.get();
    }
}
