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
                    //tk.setMatKhau(rs.getString("matKhau"));
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

    @Override
    public boolean dangKyTaiKhoan(String tenDangNhap, String matKhau) {
        // ĐỔI 'NhanVien' THÀNH 'KhachHang' ĐỂ BẢO MẬT HỆ THỐNG
        String sql = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThai) VALUES (?, ?, 'CUSTOMER', 1)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhau);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public boolean kiemTraTonTai(String tenDangNhap) {
        String sql = "SELECT 1 FROM TaiKhoan WHERE tenDangNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Trả về true nếu đã tồn tại
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public boolean dangKyKhachHang(String tenKH, String sdt, String tenDN, String matKhau) {
        String sqlTK = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThai) VALUES (?, ?, 'CUSTOMER', 1)";
        String sqlKH = "INSERT INTO KhachHang (maKhachHang, tenKhachHang, soDienThoai, tenDangNhap) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // BẮT ĐẦU GIAO DỊCH (Transaction)

            // Bước 1: Tạo tài khoản
            try (PreparedStatement psTK = conn.prepareStatement(sqlTK)) {
                psTK.setString(1, tenDN);
                psTK.setString(2, matKhau);
                psTK.executeUpdate();
            }

            // Bước 2: Lấy mã khách hàng mới
            KhachHangDAO khDao = new KhachHangDAO();
            String maMoi = khDao.taoMaKhachHangMoi(conn);

            // Bước 3: Lưu thông tin khách hàng
            try (PreparedStatement psKH = conn.prepareStatement(sqlKH)) {
                psKH.setString(1, maMoi);
                psKH.setString(2, tenKH);
                psKH.setString(3, sdt);
                psKH.setString(4, tenDN);
                psKH.executeUpdate();
            }

            conn.commit(); // CHỐT LƯU TẤT CẢ
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // HỦY NẾU LỖI
            }
            e.printStackTrace();
            return false;
        } finally {
            // Đóng kết nối an toàn
            try { if(conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception e) {}
        }
    }

    public boolean kiemTraSDTExist(String sdt) {
        String sql = "SELECT 1 FROM KhachHang WHERE soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Trả về true nếu tìm thấy số điện thoại
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public boolean doiMatKhauBangSDT(String sdt, String matKhauMoi) {
        // Cập nhật mật khẩu của tài khoản nào có số điện thoại tương ứng trong bảng KhachHang
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = " +
                "(SELECT tenDangNhap FROM KhachHang WHERE soDienThoai = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauMoi);
            ps.setString(2, sdt);

            int rowAffected = ps.executeUpdate();
            return rowAffected > 0; // Trả về true nếu tìm thấy SĐT và đổi thành công
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}