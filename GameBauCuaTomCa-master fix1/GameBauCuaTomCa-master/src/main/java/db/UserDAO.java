package db;
import common.NapTienLog;
import common.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static boolean register(User user) {
        String sql = "INSERT INTO users (username, password, name, avatar) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword()); // có thể hash ở đây
            ps.setString(3, user.getName());
            ps.setInt(4, user.getAvatar());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getInt("avatar"),
                        rs.getInt("balance")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateAvatar(int userId, int newAvatar) {
        String sql = "UPDATE users SET avatar = ? WHERE id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newAvatar);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean changePassword(int userId, String newPass) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPass); // Có thể hash nếu muốn
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updateBalance(int userId, int newBalance) {
        System.out.println("[DEBUG] Update balance in DB: userId=" + userId + ", balance=" + newBalance);
        String sql = "UPDATE users SET balance=? WHERE id=?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newBalance);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            System.out.println("[DEBUG] Rows affected: " + rows);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean logNapTien(int userId, double amount, String paymentId, String payerId, Timestamp createdAt) {
        String sql = "INSERT INTO nap_tien_log (user_id, amount, payment_id, payer_id, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDouble(2, amount);
            ps.setString(3, paymentId);
            ps.setString(4, payerId);
            ps.setTimestamp(5, createdAt);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }



    public static List<NapTienLog> getLichSuNapTien(int userId) {
        List<NapTienLog> lichSu = new ArrayList<>();
        String sql = "SELECT * FROM nap_tien_log WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                NapTienLog log = new NapTienLog(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("amount"),
                        rs.getString("payment_id"),
                        rs.getString("payer_id"),
                        rs.getTimestamp("created_at")
                );
                lichSu.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lichSu;
    }







}
