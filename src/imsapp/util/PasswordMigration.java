package imsapp.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * One-time utility — run this main() to update the admin password hash in the DB.
 * Delete or comment out after running once.
 */
public class PasswordMigration {

    public static void main(String[] args) {
        String rawPassword = "admin123";
        String hashed      = sha256(rawPassword);

        System.out.println("SHA-256 of '" + rawPassword + "' = " + hashed);

        try {
            // Check current state
            String checkSql = "SELECT username, password_hash FROM ims_db.users WHERE username = 'admin'";
            try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(checkSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Current DB hash : " + rs.getString("password_hash"));
                } else {
                    System.out.println("No admin user found in DB.");
                    return;
                }
            }

            // Update to SHA-256 hash
            String updateSql = "UPDATE ims_db.users SET password_hash = ? WHERE username = 'admin'";
            try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(updateSql)) {
                ps.setString(1, hashed);
                int rows = ps.executeUpdate();
                System.out.println("Updated " + rows + " row(s). Login with: admin / " + rawPassword);
            }

        } catch (SQLException e) {
            System.err.println("DB Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
