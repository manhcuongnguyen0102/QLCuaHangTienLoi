package DAO;

import DaoInterFace.DBConnection;
import DaoInterFace.IPhieuNhapDAO;
import model.ChiTietPhieuNhap;
import model.NhanVien;
import model.PhieuNhap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhapDAO implements IPhieuNhapDAO {

    @Override
    public boolean taoPhieuNhap(PhieuNhap pn, List<ChiTietPhieuNhap> dsChiTiet) {
        Connection conn = null;
        PreparedStatement psPN = null;
        PreparedStatement psCTPN = null;
        PreparedStatement psCapNhatKho = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;

            // 1. Tắt Auto-commit để bắt đầu Transaction
            conn.setAutoCommit(false);
            if (pn.getMaPhieuNhap() == null || pn.getMaPhieuNhap().trim().isEmpty()) {
                pn.setMaPhieuNhap(sinhMaPhieuNhapMoi());
            }
            if (pn.getNgayNhap() == null) {
                pn.setNgayNhap(new Timestamp(System.currentTimeMillis()));
            }
            // 2. Thêm Phiếu Nhập vào bảng PhieuNhap (Bảng này không có cột tongTien)
            String sqlPN = "INSERT INTO PhieuNhap (maPhieuNhap, ngayNhap, maNhanVien, maNCC) VALUES (?, ?, ?, ?)";
            psPN = conn.prepareStatement(sqlPN);
            psPN.setString(1, pn.getMaPhieuNhap());
            psPN.setTimestamp(2, pn.getNgayNhap());
            psPN.setString(3, pn.getMaNhanVien());
            psPN.setString(4, pn.getMaNCC());
            psPN.executeUpdate();

            // 3. Thêm Chi Tiết Phiếu Nhập và CỘNG Tồn Kho sản phẩm
            // maChiTiet tự tăng nên bỏ qua trong câu lệnh INSERT
            String sqlCTPN = "INSERT INTO ChiTietPhieuNhap (maPhieuNhap, maSanPham, soLuong, giaNhap) VALUES (?, ?, ?, ?)";
            String sqlCapNhatKho = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";

            psCTPN = conn.prepareStatement(sqlCTPN);
            psCapNhatKho = conn.prepareStatement(sqlCapNhatKho);

            for (ChiTietPhieuNhap ct : dsChiTiet) {
                // Tham số cho ChiTietPhieuNhap
                ct.setMaPhieuNhap(pn.getMaPhieuNhap());
                psCTPN.setString(1, ct.getMaPhieuNhap());
                psCTPN.setString(2, ct.getMaSanPham());
                psCTPN.setInt(3, ct.getSoLuong());
                psCTPN.setDouble(4, ct.getGiaNhap());
                psCTPN.addBatch();

                // Tham số cho Cập nhật số lượng tồn kho (Nhập hàng thì CỘNG thêm)
                psCapNhatKho.setInt(1, ct.getSoLuong());
                psCapNhatKho.setString(2, ct.getMaSanPham());
                psCapNhatKho.addBatch();
            }

            // Chạy tất cả các lệnh trong hàng chờ
            psCTPN.executeBatch();
            psCapNhatKho.executeBatch();

            // 4. Mọi thứ thành công -> Lưu vĩnh viễn vào DB
            conn.commit();
            System.out.println("Tạo phiếu nhập thành công!");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Đã Rollback lại giao dịch tạo phiếu nhập do có lỗi!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // 5. Đóng các tài nguyên
            try {
                if (psCapNhatKho != null) psCapNhatKho.close();
                if (psCTPN != null) psCTPN.close();
                if (psPN != null) psPN.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public String sinhMaPhieuNhapMoi() {
        String sql = "SELECT MAX(CAST(SUBSTRING(maPhieuNhap, 3, LEN(maPhieuNhap)) AS INT)) FROM PhieuNhap";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next() && rs.getObject(1) != null) {
                int max = rs.getInt(1);
                return String.format("PN%02d", max + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PN01";
    }
    public List<PhieuNhap> layTatCa() {

        List<PhieuNhap> ds = new ArrayList<>();
        String sql = "SELECT * FROM PhieuNhap";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);) {

            while (rs.next()) {
                PhieuNhap pn = new PhieuNhap();
                pn.setMaPhieuNhap(rs.getString("maPhieuNhap"));
                pn.setNgayNhap(rs.getTimestamp("ngayNhap"));
                pn.setMaNhanVien(rs.getString("maNhanVien"));
                pn.setMaNCC(rs.getString("maNCC"));
                ds.add(pn);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }
    public PhieuNhap timTheoMa(String maPhieuNhap) {
        String sql = "SELECT * FROM PhieuNhap WHERE maPhieuNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhieuNhap);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhieuNhap pn = new PhieuNhap();
                    pn.setMaPhieuNhap(rs.getString("maPhieuNhap"));
                    pn.setNgayNhap(rs.getTimestamp("ngayNhap"));
                    pn.setMaNhanVien(rs.getString("maNhanVien"));
                    pn.setMaNCC(rs.getString("maNCC"));
                    return pn;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<ChiTietPhieuNhap> layChiTietCuaPhieu(String maPhieuNhap) {
        List<ChiTietPhieuNhap> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietPhieuNhap WHERE maPhieuNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhieuNhap);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
                    ct.setMaChiTiet(rs.getInt("maChiTiet")); // maChiTiet tự tăng trong DB
                    ct.setMaPhieuNhap(rs.getString("maPhieuNhap"));
                    ct.setMaSanPham(rs.getString("maSanPham"));
                    ct.setSoLuong(rs.getInt("soLuong"));
                    ct.setGiaNhap(rs.getDouble("giaNhap"));
                    list.add(ct);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}