package DAO;

import DaoInterFace.DBConnection;
import DaoInterFace.IPhieuNhapDAO;
import model.ChiTietPhieuNhap;
import model.PhieuNhap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
}