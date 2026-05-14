package imsapp.dao;

import imsapp.model.DriversLicense;
import imsapp.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DriversLicenseDAO {

    public List<DriversLicense> findAll() throws SQLException {
        List<DriversLicense> list = new ArrayList<>();
        String sql = "SELECT * FROM drivers_licenses ORDER BY license_number";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public DriversLicense findById(int id) throws SQLException {
        String sql = "SELECT * FROM drivers_licenses WHERE license_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public void insert(DriversLicense dl) throws SQLException {
        String sql = "INSERT INTO drivers_licenses (customer_id,license_number,license_type,"
                + "issue_date,expiry_date,restriction_code,conditions,status) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, dl.getCustomerId());
            ps.setString(2, dl.getLicenseNumber());
            ps.setString(3, dl.getLicenseType());
            ps.setDate(4, Date.valueOf(dl.getIssueDate()));
            ps.setDate(5, Date.valueOf(dl.getExpiryDate()));
            ps.setString(6, dl.getRestrictionCode());
            ps.setString(7, dl.getConditions());
            ps.setString(8, dl.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) dl.setLicenseId(keys.getInt(1));
            }
        }
    }

    public void update(DriversLicense dl) throws SQLException {
        String sql = "UPDATE drivers_licenses SET customer_id=?,license_number=?,license_type=?,"
                + "issue_date=?,expiry_date=?,restriction_code=?,conditions=?,status=? WHERE license_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, dl.getCustomerId());
            ps.setString(2, dl.getLicenseNumber());
            ps.setString(3, dl.getLicenseType());
            ps.setDate(4, Date.valueOf(dl.getIssueDate()));
            ps.setDate(5, Date.valueOf(dl.getExpiryDate()));
            ps.setString(6, dl.getRestrictionCode());
            ps.setString(7, dl.getConditions());
            ps.setString(8, dl.getStatus());
            ps.setInt(9, dl.getLicenseId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM drivers_licenses WHERE license_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private DriversLicense mapRow(ResultSet rs) throws SQLException {
        DriversLicense dl = new DriversLicense();
        dl.setLicenseId(rs.getInt("license_id"));
        dl.setCustomerId(rs.getInt("customer_id"));
        dl.setLicenseNumber(rs.getString("license_number"));
        dl.setLicenseType(rs.getString("license_type"));
        Date d1 = rs.getDate("issue_date");
        if (d1 != null) dl.setIssueDate(d1.toLocalDate());
        Date d2 = rs.getDate("expiry_date");
        if (d2 != null) dl.setExpiryDate(d2.toLocalDate());
        dl.setRestrictionCode(rs.getString("restriction_code"));
        dl.setConditions(rs.getString("conditions"));
        dl.setStatus(rs.getString("status"));
        return dl;
    }
}
