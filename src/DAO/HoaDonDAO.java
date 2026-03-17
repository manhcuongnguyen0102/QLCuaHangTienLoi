package DAO;

import DaoInterFace.DBConnection;
import DaoInterFace.IHoaDonDAO;
import model.ChiTietHoaDon;
import model.HoaDon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO implements IHoaDonDAO {

    @Override
    public boolean taoHoaDon(HoaDon hd, List<ChiTietHoaDon> dsChiTiet) {
        Connection conn = null;
        PreparedStatement psHD = null;
        PreparedStatement psCTHD = null;
        PreparedStatement psCapNhatKho = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false; // Nếu rớt kết nối mạng thì dừng luôn

            // 1. Tắt Auto-commit để bắt đầu Transaction
            conn.setAutoCommit(false);

            // 2. Thêm Hóa Đơn vào bảng HoaDon
            String sqlHD = "INSERT INTO HoaDon (maHoaDon, ngayLap, maNhanVien, maKhachHang, tongTien) VALUES (?, ?, ?, ?, ?)";
            psHD = conn.prepareStatement(sqlHD);
            psHD.setString(1, hd.getMaHoaDon());
            psHD.setTimestamp(2, hd.getNgayLap());
            psHD.setString(3, hd.getMaNhanVien());
            psHD.setString(4, hd.getMaKhachHang());
            psHD.setDouble(5, hd.getTongTien());
            psHD.executeUpdate();

            // 3. Thêm danh sách Chi Tiết Hóa Đơn và trừ Tồn Kho sản phẩm
            // Lưu ý: Cột maChiTiet là IDENTITY (tự tăng) nên không cần truyền vào
            String sqlCTHD = "INSERT INTO ChiTietHoaDon (maHoaDon, maSanPham, soLuong, giaBan) VALUES (?, ?, ?, ?)";
            String sqlCapNhatKho = "UPDATE SanPham SET soLuongTon = soLuongTon - ? WHERE maSanPham = ?";

            psCTHD = conn.prepareStatement(sqlCTHD);
            psCapNhatKho = conn.prepareStatement(sqlCapNhatKho);

            for (ChiTietHoaDon ct : dsChiTiet) {
                // Tham số cho ChiTietHoaDon
                psCTHD.setString(1, ct.getMaHoaDon());
                psCTHD.setString(2, ct.getMaSanPham());
                psCTHD.setInt(3, ct.getSoLuong());
                psCTHD.setDouble(4, ct.getGiaBan());
                psCTHD.addBatch(); // Đưa vào hàng chờ chạy lô

                // Tham số cho Cập nhật số lượng tồn kho
                psCapNhatKho.setInt(1, ct.getSoLuong());
                psCapNhatKho.setString(2, ct.getMaSanPham());
                psCapNhatKho.addBatch();
            }

            // Chạy tất cả các lệnh trong hàng chờ
            psCTHD.executeBatch();
            psCapNhatKho.executeBatch();

            // 4. Mọi thứ thành công -> Lưu vĩnh viễn vào DB
            conn.commit();
            System.out.println("Tạo hóa đơn thành công!");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    // Nếu có bất kỳ lỗi gì (sai khóa ngoại, thiếu dữ liệu...), hoàn tác lại toàn bộ
                    conn.rollback();
                    System.out.println("Đã Rollback lại giao dịch tạo hóa đơn do có lỗi!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // 5. Đóng các tài nguyên để giải phóng bộ nhớ
            try {
                if (psCapNhatKho != null) psCapNhatKho.close();
                if (psCTHD != null) psCTHD.close();
                if (psHD != null) psHD.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Trả lại trạng thái mặc định cho Connection
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public List<HoaDon> layLichSu(String maKH) {
        List<HoaDon> list = new ArrayList<>();
        // Sắp xếp theo ngày lập mới nhất lên đầu
        String sql = "SELECT * FROM HoaDon WHERE maKhachHang = ? ORDER BY ngayLap DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conn != null) {
                ps.setString(1, maKH);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    HoaDon hd = new HoaDon();
                    hd.setMaHoaDon(rs.getString("maHoaDon"));
                    hd.setNgayLap(rs.getTimestamp("ngayLap"));
                    hd.setMaNhanVien(rs.getString("maNhanVien"));
                    hd.setMaKhachHang(rs.getString("maKhachHang"));
                    hd.setTongTien(rs.getDouble("tongTien"));
                    list.add(hd);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}