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
    private final String SELECT_JOIN_SQL =
            "SELECT sp.*, l.tenLoai, n.tenNCC " +
                    "FROM SanPham sp " +
                    "JOIN LoaiSanPham l ON sp.maLoai = l.maLoai " +
                    "JOIN NhaCungCap n ON sp.maNCC = n.maNCC";
    @Override
    public List<SanPham> layTatCa() {
        List<SanPham> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_JOIN_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractSanPham(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public boolean them(SanPham sp) {
        if (sp.getMaSanPham() == null || sp.getMaSanPham().isEmpty()) {
            sp.setMaSanPham(sinhMaSanPhamMoi());
        }

        // 1. Kiểm tra sự tồn tại của maLoai và maNCC trước
        String sqlCheck = "SELECT " +
                "(SELECT COUNT(*) FROM LoaiSanPham WHERE maLoai = ?) AS checkLoai, " +
                "(SELECT COUNT(*) FROM NhaCungCap WHERE maNCC = ?) AS checkNCC";

        String sqlInsert = "INSERT INTO SanPham (maSanPham, tenSanPham, giaNhap, giaBan, soLuongTon, ngayHetHan, maLoai, maNCC) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            // Bước 1: Check sự tồn tại
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, sp.getMaLoai());
                psCheck.setString(2, sp.getMaNCC());
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt("checkLoai") == 0) {
                            System.err.println("Lỗi: Mã loại [" + sp.getMaLoai() + "] không tồn tại!");
                            return false;
                        }
                        if (rs.getInt("checkNCC") == 0) {
                            System.err.println("Lỗi: Mã NCC [" + sp.getMaNCC() + "] không tồn tại!");
                            return false;
                        }
                    }
                }
            }

            // Bước 2: Nếu mọi thứ OK, mới tiến hành Insert
            try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                psInsert.setString(1, sp.getMaSanPham());
                psInsert.setString(2, sp.getTenSanPham());
                psInsert.setDouble(3, sp.getGiaNhap());
                psInsert.setDouble(4, sp.getGiaBan());
                psInsert.setInt(5, sp.getSoLuongTon());
                psInsert.setDate(6, sp.getNgayHetHan());
                psInsert.setString(7, sp.getMaLoai());
                psInsert.setString(8, sp.getMaNCC());
                return psInsert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public String sinhMaSanPhamMoi() {
        String sql = "SELECT MAX(CAST(SUBSTRING(maSanPham, 3, LEN(maSanPham)) AS INT)) FROM SanPham";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next() && rs.getObject(1) != null) {
                int max = rs.getInt(1);
                return String.format("SP%03d", max + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "SP001"; // Nếu kho chưa có gì
    }
    @Override
    public int xoa(String maSP) {
        // BƯỚC 1: Kiểm tra xem sản phẩm đã có trong chi tiết hóa đơn hay chưa
        String sqlCheck = "SELECT " +
                "(SELECT COUNT(*) FROM ChiTietHoaDon WHERE maSanPham = ?) + " +
                "(SELECT COUNT(*) FROM ChiTietPhieuNhap WHERE maSanPham = ?) AS TongGiaoDich";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, maSP);
                psCheck.setString(2, maSP);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // TRƯỜNG HỢP 1: Đã bán -> Không được xóa
                        return 2;
                    }
                }
            }

            // BƯỚC 2: Nếu chưa bán, tiến hành xóa cứng
            String sqlDelete = "DELETE FROM SanPham WHERE maSanPham = ?";
            try (PreparedStatement psDelete = conn.prepareStatement(sqlDelete)) {
                psDelete.setString(1, maSP);
                if (psDelete.executeUpdate() > 0) {
                    return 1; // TRƯỜNG HỢP 2: Xóa thành công
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            String errMsg = e.getMessage().toLowerCase();
            if (errMsg.contains("reference") || errMsg.contains("conflict") || errMsg.contains("foreign key")) {
                return 2;
            }
        }
        return 0; // TRƯỜNG HỢP 3: Lỗi hệ thống
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