package imsapp.dao;

import imsapp.model.Vehicle;
import imsapp.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    public List<Vehicle> findAll() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicles ORDER BY plate_number";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Vehicle findById(int id) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE vehicle_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Vehicle> findByCustomerId(int customerId) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicles WHERE customer_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Vehicle v) throws SQLException {
        String sql = "INSERT INTO vehicles (customer_id,plate_number,engine_number,chassis_number,"
                + "make,model,year_model,color,body_type,fuel_type,mv_file_number,"
                + "or_number,cr_number,registration_date,expiry_date,status) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setVehicleParams(ps, v);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) v.setVehicleId(keys.getInt(1));
            }
        }
    }

    public void update(Vehicle v) throws SQLException {
        String sql = "UPDATE vehicles SET customer_id=?,plate_number=?,engine_number=?,"
                + "chassis_number=?,make=?,model=?,year_model=?,color=?,body_type=?,"
                + "fuel_type=?,mv_file_number=?,or_number=?,cr_number=?,"
                + "registration_date=?,expiry_date=?,status=? WHERE vehicle_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            setVehicleParams(ps, v);
            ps.setInt(17, v.getVehicleId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void setVehicleParams(PreparedStatement ps, Vehicle v) throws SQLException {
        ps.setInt(1, v.getCustomerId());
        ps.setString(2, v.getPlateNumber());
        ps.setString(3, v.getEngineNumber());
        ps.setString(4, v.getChassisNumber());
        ps.setString(5, v.getMake());
        ps.setString(6, v.getModel());
        ps.setInt(7, v.getYearModel());
        ps.setString(8, v.getColor());
        ps.setString(9, v.getBodyType());
        ps.setString(10, v.getFuelType());
        ps.setString(11, v.getMvFileNumber());
        ps.setString(12, v.getOrNumber());
        ps.setString(13, v.getCrNumber());
        ps.setDate(14, v.getRegistrationDate() != null ? Date.valueOf(v.getRegistrationDate()) : null);
        ps.setDate(15, v.getExpiryDate() != null ? Date.valueOf(v.getExpiryDate()) : null);
        ps.setString(16, v.getStatus());
    }

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setVehicleId(rs.getInt("vehicle_id"));
        v.setCustomerId(rs.getInt("customer_id"));
        v.setPlateNumber(rs.getString("plate_number"));
        v.setEngineNumber(rs.getString("engine_number"));
        v.setChassisNumber(rs.getString("chassis_number"));
        v.setMake(rs.getString("make"));
        v.setModel(rs.getString("model"));
        v.setYearModel(rs.getInt("year_model"));
        v.setColor(rs.getString("color"));
        v.setBodyType(rs.getString("body_type"));
        v.setFuelType(rs.getString("fuel_type"));
        v.setMvFileNumber(rs.getString("mv_file_number"));
        v.setOrNumber(rs.getString("or_number"));
        v.setCrNumber(rs.getString("cr_number"));
        Date rd = rs.getDate("registration_date");
        if (rd != null) v.setRegistrationDate(rd.toLocalDate());
        Date ed = rs.getDate("expiry_date");
        if (ed != null) v.setExpiryDate(ed.toLocalDate());
        v.setStatus(rs.getString("status"));
        return v;
    }
}
