package DAO;

import DaoInterFace.INhanVienDAO;
import DaoInterFace.DBConnection;
import model.NhanVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO implements INhanVienDAO {

    @Override
    public List<NhanVien> layTatCa() {

        List<NhanVien> ds = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);) {

            while (rs.next()) {

                NhanVien nv = new NhanVien();
                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setTenNhanVien(rs.getString("tenNhanVien"));
                nv.setChucVu(rs.getString("chucVu"));
                nv.setSoDienThoai(rs.getString("soDienThoai"));
                nv.setTenDangNhap(rs.getString("tenDangNhap"));

                ds.add(nv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    @Override
    public NhanVien timTheoMa(String maNV) {
        String sql = "SELECT * FROM NhanVien WHERE maNhanVien = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNhanVien(rs.getString("maNhanVien"));
                    nv.setTenNhanVien(rs.getString("tenNhanVien"));
                    nv.setChucVu(rs.getString("chucVu"));
                    nv.setSoDienThoai(rs.getString("soDienThoai"));
                    nv.setTenDangNhap(rs.getString("tenDangNhap"));
                    return nv;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }
    @Override
    public boolean them(NhanVien nv, String matKhauMacDinh) {
        // Bước 1: SQL linh hoạt hơn
        String sqlTK = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThai) VALUES (?, ?, ?, 1)";
        String sqlNV = "INSERT INTO NhanVien (maNhanVien, tenNhanVien, chucVu, soDienThoai, tenDangNhap) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // [MỚI] Xác định vai trò dựa trên chức vụ
            String vaiTro = "STAFF"; // Mặc định
            if (nv.getChucVu() != null && nv.getChucVu().equalsIgnoreCase("Quản lý")) {
                vaiTro = "QUAN_LY";
            }

            // BƯỚC 1: Tạo tài khoản với vai trò động
            try (PreparedStatement psTK = conn.prepareStatement(sqlTK)) {
                psTK.setString(1, nv.getTenDangNhap());
                psTK.setString(2, matKhauMacDinh);
                psTK.setString(3, vaiTro); // Truyền vai trò đã được xác định ở trên
                psTK.executeUpdate();
            }

            // BƯỚC 2: Sinh mã mới
            String maMoi = sinhMaNhanVien(conn);

            // BƯỚC 3: Lưu thông tin nhân viên
            try (PreparedStatement psNV = conn.prepareStatement(sqlNV)) {
                psNV.setString(1, maMoi);
                psNV.setString(2, nv.getTenNhanVien());
                psNV.setString(3, nv.getChucVu());
                psNV.setString(4, nv.getSoDienThoai());
                psNV.setString(5, nv.getTenDangNhap());
                psNV.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            try { if(conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception e) {}
        }
        return false;
    }


    @Override
    public boolean sua(NhanVien nv) {
        String sql = "UPDATE NhanVien SET tenNhanVien=?, chucVu=?, soDienThoai=?, tenDangNhap=? WHERE maNhanVien=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nv.getTenNhanVien());
            ps.setString(2, nv.getChucVu());
            ps.setString(3, nv.getSoDienThoai());
            ps.setString(4, nv.getTenDangNhap());
            ps.setString(5, nv.getMaNhanVien());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean xoa(String maNV) {
        String sql = "DELETE FROM NhanVien WHERE maNhanVien=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Cảnh báo nếu dính khóa ngoại với Hóa Đơn hoặc Phiếu Nhập
            System.err.println("Lỗi Xóa: Không thể xóa nhân viên " + maNV + " vì họ đã từng tạo Hóa đơn hoặc Phiếu nhập!");
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public String sinhMaNhanVien(Connection conn) {
        // Dùng SQL Server lấy ngay cái mã lớn nhất ra (Ví dụ: NV015)
        // Lấy đúng 1 dòng (TOP 1) và sắp xếp giảm dần (ORDER BY DESC)
        String sql = "SELECT TOP 1 maNhanVien FROM NhanVien ORDER BY maNhanVien DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String maCu = rs.getString("maNhanVien");
                // Cắt chữ NV (từ vị trí số 2) rồi ép sang số
                int max = Integer.parseInt(maCu.substring(2));
                return String.format("NV%03d", max + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Lỗi parse số khi sinh mã nhân viên!");
        }

        // Nếu bảng trống chưa có ai, trả về nhân viên đầu tiên
        return "NV001";
    }

}