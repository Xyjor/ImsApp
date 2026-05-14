package imsapp.dao;

import imsapp.model.Customer;
import imsapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer CRUD operations.
 */
public class CustomerDAO {

    /**
     * Retrieves all customers from the database.
     */
    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY last_name, first_name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Finds a customer by primary key.
     */
    public Customer findById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Inserts a new customer. Sets the generated ID back on the object.
     */
    public void insert(Customer c) throws SQLException {
        String sql = """
            INSERT INTO customers (first_name, last_name, middle_name, date_of_birth,
                gender, address, city, province, zip_code, contact_number, email)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,  c.getFirstName());
            ps.setString(2,  c.getLastName());
            ps.setString(3,  c.getMiddleName());
            ps.setDate(4,    Date.valueOf(c.getDateOfBirth()));
            ps.setString(5,  c.getGender());
            ps.setString(6,  c.getAddress());
            ps.setString(7,  c.getCity());
            ps.setString(8,  c.getProvince());
            ps.setString(9,  c.getZipCode());
            ps.setString(10, c.getContactNumber());
            ps.setString(11, c.getEmail());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setCustomerId(keys.getInt(1));
            }
        }
    }

    /**
     * Updates an existing customer record.
     */
    public void update(Customer c) throws SQLException {
        String sql = """
            UPDATE customers SET first_name=?, last_name=?, middle_name=?,
                date_of_birth=?, gender=?, address=?, city=?, province=?,
                zip_code=?, contact_number=?, email=?
            WHERE customer_id=?
            """;
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1,  c.getFirstName());
            ps.setString(2,  c.getLastName());
            ps.setString(3,  c.getMiddleName());
            ps.setDate(4,    Date.valueOf(c.getDateOfBirth()));
            ps.setString(5,  c.getGender());
            ps.setString(6,  c.getAddress());
            ps.setString(7,  c.getCity());
            ps.setString(8,  c.getProvince());
            ps.setString(9,  c.getZipCode());
            ps.setString(10, c.getContactNumber());
            ps.setString(11, c.getEmail());
            ps.setInt(12,    c.getCustomerId());
            ps.executeUpdate();
        }
    }

    /**
     * Deletes a customer by ID.
     */
    public void delete(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }

    // --- Row mapper ---
    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setFirstName(rs.getString("first_name"));
        c.setLastName(rs.getString("last_name"));
        c.setMiddleName(rs.getString("middle_name"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) c.setDateOfBirth(dob.toLocalDate());
        c.setGender(rs.getString("gender"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        c.setProvince(rs.getString("province"));
        c.setZipCode(rs.getString("zip_code"));
        c.setContactNumber(rs.getString("contact_number"));
        c.setEmail(rs.getString("email"));
        return c;
    }
}
