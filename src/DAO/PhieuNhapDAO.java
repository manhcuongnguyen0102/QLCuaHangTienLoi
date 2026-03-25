package DAO;

import DaoInterFace.DBConnection;
import DaoInterFace.IPhieuNhapDAO;
import model.ChiTietPhieuNhap;
import model.PhieuNhap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.sql.ResultSet;

public class PhieuNhapDAO implements IPhieuNhapDAO {
    // CODE THÊM VÀO ĐỂ PHỤC VỤ CHỨC NĂNG GET (LỊCH SỬ NHẬP HÀNG)
    // Class phụ trợ (DTO) chứa dữ liệu trả về cho API
    public static class PhieuNhapDTO {
        public String maPhieuNhap;
        public java.sql.Timestamp ngayNhap;
        public String maNhanVien;
        public String maNCC;
        public double tongTien;
    }
    @Override
    public List<PhieuNhapDTO> layDanhSachPhieuNhap() {
        List<PhieuNhapDTO> list = new java.util.ArrayList<>();
        // Câu lệnh SQL JOIN bảng ChiTietPhieuNhap để tính tổng tiền bằng hàm SUM()
        String sql = "SELECT pn.maPhieuNhap, pn.ngayNhap, pn.maNhanVien, pn.maNCC, SUM(ct.soLuong * ct.giaNhap) AS tongTien " +
                "FROM PhieuNhap pn " +
                "JOIN ChiTietPhieuNhap ct ON pn.maPhieuNhap = ct.maPhieuNhap " +
                "GROUP BY pn.maPhieuNhap, pn.ngayNhap, pn.maNhanVien, pn.maNCC " +
                "ORDER BY pn.ngayNhap DESC"; // Mới nhất lên đầu

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhieuNhapDTO dto = new PhieuNhapDTO();
                dto.maPhieuNhap = rs.getString("maPhieuNhap");
                dto.ngayNhap = rs.getTimestamp("ngayNhap");
                dto.maNhanVien = rs.getString("maNhanVien");
                dto.maNCC = rs.getString("maNCC");
                dto.tongTien = rs.getDouble("tongTien");
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
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
            // 2. Thêm Phiếu Nhập vào bảng PhieuNhap (Bảng này không có cột tongTien)
            String sqlPN = "INSERT INTO PhieuNhap (maPhieuNhap, ngayNhap, maNhanVien, maNCC) VALUES (?, ?, ?, ?)";
            psPN = conn.prepareStatement(sqlPN);
            psPN.setString(1, pn.getMaPhieuNhap());
            psPN.setTimestamp(2, pn.getNgayNhap());
            psPN.setString(3, pn.getMaNhanVien());
            psPN.setString(4, pn.getMaNCC());
            psPN.executeUpdate();

            // 3. Thêm Chi Tiết Phiếu Nhập và CỘNG Tồn Kho sản phẩm
            String sqlCTPN = "INSERT INTO ChiTietPhieuNhap (maPhieuNhap, maSanPham, soLuong, giaNhap) VALUES (?, ?, ?, ?)";
            String sqlCapNhatKho = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";
            psCTPN = conn.prepareStatement(sqlCTPN);
            psCapNhatKho = conn.prepareStatement(sqlCapNhatKho);

            for (ChiTietPhieuNhap ct : dsChiTiet) {

                // 1. insert chi tiết
                psCTPN.setString(1, ct.getMaPhieuNhap());
                psCTPN.setString(2, ct.getMaSanPham());
                psCTPN.setInt(3, ct.getSoLuong());
                psCTPN.setDouble(4, ct.getGiaNhap());
                psCTPN.executeUpdate();

                // 2. update kho
                psCapNhatKho.setInt(1, ct.getSoLuong());
                psCapNhatKho.setString(2, ct.getMaSanPham());
                int affected = psCapNhatKho.executeUpdate();

                // check tồn tại
                if (affected == 0) {
                    throw new SQLException("Sản phẩm không tồn tại: " + ct.getMaSanPham());
                }
            }
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
    public int getMaxMaPN() {
        int max = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT maPhieuNhap FROM PhieuNhap");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("maPhieuNhap").replace("PN", "");
                int num = Integer.parseInt(id);
                if (num > max) max = num;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return max;
    }
}