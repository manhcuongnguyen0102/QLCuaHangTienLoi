package DaoInterFace;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DBConnection {
    // Thay đổi thông tin này cho phù hợp với máy của bạn
    private static final String SERVER_NAME = "localhost"; // Hoặc tên máy tính của bạn (VD: LAPTOP-6IOOCEDN)
   // private static final String SERVER_NAME = "localhost";
    private static final String DB_NAME = "QLCuaHangTienLoi";
    private static final String PORT = "1433"; // Cổng mặc định của SQL Server
    // Nếu SQL Server của bạn dùng quyền Windows Authentication (không cần user/pass):
    // Cần thêm tham số integratedSecurity=true vào chuỗi kết nối.
    // Dưới đây là chuỗi kết nối sử dụng tài khoản sa (Khuyên dùng)
    private static final String USER = "sa";
    private static final String PASS = "123456"; // Điền mật khẩu SQL Server của bạn vào đây

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // 1. Đăng ký Driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // 2. Tạo chuỗi kết nối (URL)
            // Lưu ý: encrypt=true;trustServerCertificate=true giúp tránh lỗi SSL khi kết nối
            String url = "jdbc:sqlserver://" + SERVER_NAME + ":" + PORT
                    + ";databaseName=" + DB_NAME
                    + ";encrypt=true;trustServerCertificate=true;";
            jdbc:sqlserver://localhost:1433;databaseName=QLCuaHangTienLoi

            // 3. Mở kết nối
            conn = DriverManager.getConnection(url, USER, PASS);
            System.out.println("Kết nối SQL Server thành công!");

        } catch (ClassNotFoundException e) {
            System.out.println("Lỗi: Không tìm thấy thư viện JDBC Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Lỗi: Không thể kết nối tới cơ sở dữ liệu!");
            e.printStackTrace();
        }
        return conn;
    }

    // Hàm main để test thử xem kết nối chạy chưa
    public static void main(String[] args) {
        getConnection();
    }
}

