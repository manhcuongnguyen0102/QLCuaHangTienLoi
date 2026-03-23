package DAO;

import DaoInterFace.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.SanPham;

public class ThongKeDAO {

    // Tổng doanh thu toàn bộ
    public double tongDoanhThu() {

        double tong = 0;
        String sql = "SELECT SUM(tongTien) AS tong FROM HoaDon";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                tong = rs.getDouble("tong");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tong;
    }

    // Tổng số hóa đơn
    public int tongHoaDon() {

        int tong = 0;
        String sql = "SELECT COUNT(*) AS tong FROM HoaDon";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                tong = rs.getInt("tong");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tong;
    }

    // Doanh thu theo ngày
    public double doanhThuTheoNgay(java.sql.Date ngay) {

        double tong = 0;
        String sql = "SELECT SUM(tongTien) AS tong FROM HoaDon WHERE CAST(ngayLap AS DATE) = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, ngay); // Truyền chuẩn Date vào SQL
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tong = rs.getDouble("tong");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tong;
    }

    // Số lượng sản phẩm bán ra
    public int tongSanPhamBanRa() {

        int tong = 0;
        String sql = "SELECT SUM(soLuong) AS tong FROM ChiTietHoaDon";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                tong = rs.getInt("tong");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tong;
    }
    public double doanhThuKhoangThoiGian(java.sql.Date tuNgay, java.sql.Date denNgay) {
        double tong = 0;

        // SQL Server: Lọc các hóa đơn có ngày lập nằm trong khoảng từ ngày A đến ngày B
        String sql = "SELECT SUM(tongTien) AS tong FROM HoaDon WHERE CAST(ngayLap AS DATE) BETWEEN ? AND ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Truyền 2 mốc thời gian vào câu lệnh SQL
            ps.setDate(1, tuNgay);
            ps.setDate(2, denNgay);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tong = rs.getDouble("tong");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tong;
    }
    // 6. Lấy Top 5 sản phẩm bán chạy nhất
    public List<SanPham> layTop5BanChay() {
        List<SanPham> list = new ArrayList<>();

        // Dùng TOP 5 và GROUP BY để gom nhóm sản phẩm, sau đó sắp xếp giảm dần theo tổng số lượng bán
        String sql = "SELECT TOP 5 sp.maSanPham, sp.tenSanPham, sp.giaNhap, sp.giaBan, sp.soLuongTon, sp.ngayHetHan, sp.maLoai, sp.maNCC " +
                "FROM SanPham sp " +
                "JOIN ChiTietHoaDon cthd ON sp.maSanPham = cthd.maSanPham " +
                "GROUP BY sp.maSanPham, sp.tenSanPham, sp.giaNhap, sp.giaBan, sp.soLuongTon, sp.ngayHetHan, sp.maLoai, sp.maNCC " +
                "ORDER BY SUM(cthd.soLuong) DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setMaSanPham(rs.getString("maSanPham"));
                sp.setTenSanPham(rs.getString("tenSanPham"));
                sp.setGiaNhap(rs.getDouble("giaNhap"));
                sp.setGiaBan(rs.getDouble("giaBan"));
                sp.setSoLuongTon(rs.getInt("soLuongTon"));
                sp.setNgayHetHan(rs.getDate("ngayHetHan"));
                sp.setMaLoai(rs.getString("maLoai"));
                sp.setMaNCC(rs.getString("maNCC"));
                list.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 7. Lấy danh sách sản phẩm sắp hết (tồn kho < 10)
    public List<SanPham> layHangSapHet() {
        List<SanPham> list = new ArrayList<>();

        // Lấy các sản phẩm có số lượng tồn < 10, ưu tiên hiển thị cái nào ít nhất lên đầu (ASC)
        String sql = "SELECT * FROM SanPham WHERE soLuongTon < 10 ORDER BY soLuongTon ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setMaSanPham(rs.getString("maSanPham"));
                sp.setTenSanPham(rs.getString("tenSanPham"));
                sp.setGiaNhap(rs.getDouble("giaNhap"));
                sp.setGiaBan(rs.getDouble("giaBan"));
                sp.setSoLuongTon(rs.getInt("soLuongTon"));
                sp.setNgayHetHan(rs.getDate("ngayHetHan"));
                sp.setMaLoai(rs.getString("maLoai"));
                sp.setMaNCC(rs.getString("maNCC"));
                list.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
