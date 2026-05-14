package imsapp.dao;

import imsapp.model.Payment;
import imsapp.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public List<Payment> findAll() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Payment findById(int id) throws SQLException {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Payment findByTransactionId(String txnId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE transaction_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public void insert(Payment p) throws SQLException {
        String sql = "INSERT INTO payments (transaction_id,customer_id,vehicle_id,"
                + "license_id,payment_type,amount,payment_method,payment_date,remarks) "
                + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTransactionId());
            ps.setInt(2, p.getCustomerId());
            ps.setInt(3, p.getVehicleId());
            ps.setInt(4, p.getLicenseId());
            ps.setString(5, p.getPaymentType());
            ps.setDouble(6, p.getAmount());
            ps.setString(7, p.getPaymentMethod());
            ps.setTimestamp(8, Timestamp.valueOf(p.getPaymentDate()));
            ps.setString(9, p.getRemarks());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setPaymentId(keys.getInt(1));
            }
        }
    }

    public void update(Payment p) throws SQLException {
        String sql = "UPDATE payments SET customer_id=?, vehicle_id=?, license_id=?, "
                + "payment_type=?, amount=?, payment_method=?, remarks=? "
                + "WHERE payment_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, p.getCustomerId());
            ps.setInt(2, p.getVehicleId());
            ps.setInt(3, p.getLicenseId());
            ps.setString(4, p.getPaymentType());
            ps.setDouble(5, p.getAmount());
            ps.setString(6, p.getPaymentMethod());
            ps.setString(7, p.getRemarks());
            ps.setInt(8, p.getPaymentId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Generates a unique transaction ID in the format TXN-YYYYMMDD-XXXX.
     */
    public String generateTransactionId() throws SQLException {
        String dateStr = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "TXN-" + dateStr + "-";
        String sql = "SELECT COUNT(*) FROM payments WHERE transaction_id LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int count = rs.getInt(1) + 1;
                return prefix + String.format("%04d", count);
            }
        }
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setTransactionId(rs.getString("transaction_id"));
        p.setCustomerId(rs.getInt("customer_id"));
        p.setVehicleId(rs.getInt("vehicle_id"));
        p.setLicenseId(rs.getInt("license_id"));
        p.setPaymentType(rs.getString("payment_type"));
        p.setAmount(rs.getDouble("amount"));
        p.setPaymentMethod(rs.getString("payment_method"));
        Timestamp ts = rs.getTimestamp("payment_date");
        if (ts != null) p.setPaymentDate(ts.toLocalDateTime());
        p.setRemarks(rs.getString("remarks"));
        return p;
    }
}
