package DAO;

import DaoInterFace.DBConnection;
import DaoInterFace.ITaiKhoanDAO;
import model.TaiKhoan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaiKhoanDAO implements ITaiKhoanDAO {

    @Override
    public TaiKhoan kiemTraDangNhap(String user, String pass) {
        // Chỉ cho phép đăng nhập nếu tài khoản đang hoạt động (trangThai = 1)
        String sql = "SELECT * FROM TaiKhoan WHERE tenDangNhap = ? AND matKhau = ? AND trangThai = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user);
            ps.setString(2, pass);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan tk = new TaiKhoan();
                    tk.setTenDangNhap(rs.getString("tenDangNhap"));
                    tk.setMatKhau(rs.getString("matKhau"));
                    tk.setVaiTro(rs.getString("vaiTro"));
                    tk.setTrangThai(rs.getBoolean("trangThai"));
                    return tk;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra đăng nhập!");
            e.printStackTrace();
        }
        return null; // Trả về null nếu sai thông tin hoặc lỗi
    }

    @Override
    public boolean doiMatKhau(String user, String oldPass, String newPass) {
        // Kiểm tra đúng mật khẩu cũ mới cho phép cập nhật mật khẩu mới
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = ? AND matKhau = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPass);
            ps.setString(2, user);
            ps.setString(3, oldPass);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0; // Trả về true nếu cập nhật thành công ít nhất 1 dòng
        } catch (SQLException e) {
            System.err.println("Lỗi khi đổi mật khẩu!");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean capNhatTrangThai(String user, int status) {
        String sql = "UPDATE TaiKhoan SET trangThai = ? WHERE tenDangNhap = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Vì trong SQL Server trangThai là kiểu BIT, nên 1 tương đương true, 0 tương đương false
            ps.setInt(1, status);
            ps.setString(2, user);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái tài khoản!");
            e.printStackTrace();
        }
        return false;
    }
}