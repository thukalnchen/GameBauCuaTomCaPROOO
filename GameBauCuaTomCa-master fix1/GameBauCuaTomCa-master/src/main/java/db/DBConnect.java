package db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {
    private static final String URL = "jdbc:mysql://localhost:3306/baucua"; // đúng tên db bạn đã tạo
    private static final String USER = "root";
    private static final String PASS = ""; // Nếu XAMPP có pass thì điền vào đây

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Test kết nối
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println(conn != null ? "Kết nối thành công!" : "Kết nối thất bại!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
