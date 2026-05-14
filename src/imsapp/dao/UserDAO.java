package imsapp.dao;

import imsapp.model.User;
import imsapp.util.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password); // Note: Simple auth for demonstration. Use bcrypt in production.
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setUsername(rs.getString("username"));
                    u.setRole(rs.getString("role"));
                    return u;
                }
            }
        }
        return null;
    }
}
