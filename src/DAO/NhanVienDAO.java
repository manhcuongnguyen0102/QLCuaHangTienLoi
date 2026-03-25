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
    public boolean them(NhanVien nv) {
        String sql = "INSERT INTO NhanVien (maNhanVien, tenNhanVien, chucVu, soDienThoai, tenDangNhap) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nv.getMaNhanVien());
            ps.setString(2, nv.getTenNhanVien());
            ps.setString(3, nv.getChucVu());
            ps.setString(4, nv.getSoDienThoai());
            ps.setString(5, nv.getTenDangNhap());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi Thêm: Mã nhân viên hoặc Tên đăng nhập bị trùng, hoặc tên đăng nhập chưa có trong bảng TaiKhoan!");
            e.printStackTrace();
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

    public String sinhMaNhanVien() {
        int max = 0;
        String sql = "select maNhanVien from NhanVien";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String ma = rs.getString("maNhanVien");
                if (ma != null && ma.startsWith("NV")) {
                    try {
                        int so = Integer.parseInt(ma.substring(2));
                        if (so > max) max = so;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.format("NV%03d", max + 1);
    }

}