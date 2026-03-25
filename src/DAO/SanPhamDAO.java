package DAO;

import DaoInterFace.DBConnection;
import DaoInterFace.ISanPhamDAO;
import model.SanPham;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAO implements ISanPhamDAO {
    private SanPham extractSanPham(ResultSet rs) throws SQLException {
        SanPham sp = new SanPham();
        sp.setMaSanPham(rs.getString("maSanPham"));
        sp.setTenSanPham(rs.getString("tenSanPham"));
        sp.setGiaNhap(rs.getDouble("giaNhap"));
        sp.setGiaBan(rs.getDouble("giaBan"));
        sp.setSoLuongTon(rs.getInt("soLuongTon"));
        sp.setNgayHetHan(rs.getDate("ngayHetHan"));
        sp.setMaLoai(rs.getString("maLoai"));
        sp.setMaNCC(rs.getString("maNCC"));
        return sp;
    }
    @Override
    public List<SanPham> layTatCa() {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SanPham";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractSanPham(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    @Override
    public SanPham timTheoMa(String maSP) {
        String sql = "SELECT * FROM SanPham WHERE maSanPham = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractSanPham(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<SanPham> timTheoTen(String ten) {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE tenSanPham LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + ten + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractSanPham(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<SanPham> layTheoLoai(String maLoai) {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE maLoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLoai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractSanPham(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean them(SanPham sp) {
        String sql = "INSERT INTO SanPham (maSanPham, tenSanPham, giaNhap, giaBan, soLuongTon, ngayHetHan, maLoai, maNCC) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sp.getMaSanPham());
            ps.setString(2, sp.getTenSanPham());
            ps.setDouble(3, sp.getGiaNhap());
            ps.setDouble(4, sp.getGiaBan());
            ps.setInt(5, sp.getSoLuongTon());
            ps.setDate(6, sp.getNgayHetHan());
            ps.setString(7, sp.getMaLoai());
            ps.setString(8, sp.getMaNCC());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean sua(SanPham sp) {
        String sql = "UPDATE SanPham SET tenSanPham = ?, giaNhap = ?, giaBan = ?, soLuongTon = ?, ngayHetHan = ?, maLoai = ?, maNCC = ? WHERE maSanPham = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sp.getTenSanPham());
            ps.setDouble(2, sp.getGiaNhap());
            ps.setDouble(3, sp.getGiaBan());
            ps.setInt(4, sp.getSoLuongTon());
            ps.setDate(5, sp.getNgayHetHan());
            ps.setString(6, sp.getMaLoai());
            ps.setString(7, sp.getMaNCC());
            ps.setString(8, sp.getMaSanPham());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public boolean xoa(String maSP) {
        String sql = "DELETE FROM SanPham WHERE maSanPham = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSP);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi XÓA: Sản phẩm đang tồn tại trong Chi Tiết Hóa Đơn hoặc Chi Tiết Phiếu Nhập!");
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public boolean capNhatSoLuongTon(String maSP, int soLuongThayDoi) {
        String sql = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuongThayDoi);
            ps.setString(2, maSP);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}