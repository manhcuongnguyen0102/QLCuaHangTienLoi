package DAO;

import API.ThongKeAPI;
import DaoInterFace.DBConnection;
import model.SanPham;
import model.ThongKeBieuDo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThongKeDAO {

    // ========================================================
    // NHÓM 1: THỐNG KÊ TỔNG QUAN HÔM NAY (Dùng cho 4 thẻ thẻ bài trên Dashboard)
    // ========================================================

    public double doanhThuHomNay() {
        double tong = 0;
        String sql = "SELECT SUM(tongTien) AS tong FROM HoaDon WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) tong = rs.getDouble("tong");
        } catch (SQLException e) { e.printStackTrace(); }
        return tong;
    }

    public int soHoaDonHomNay() {
        int tong = 0;
        String sql = "SELECT COUNT(*) AS tong FROM HoaDon WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) tong = rs.getInt("tong");
        } catch (SQLException e) { e.printStackTrace(); }
        return tong;
    }

    public int soKhachHangMoiHomNay() {
        int tong = 0;
        String sql = "SELECT COUNT(*) AS tong FROM KhachHang WHERE CAST(ngayDangKy AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) tong = rs.getInt("tong");
        } catch (SQLException e) { e.printStackTrace(); }
        return tong;
    }

    public int soSanPhamSapHet() {
        int tong = 0;
        String sql = "SELECT COUNT(*) AS tong FROM SanPham WHERE soLuongTon < 10";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) tong = rs.getInt("tong");
        } catch (SQLException e) { e.printStackTrace(); }
        return tong;
    }

    // ========================================================
    // NHÓM 2: THỐNG KÊ KHOẢNG THỜI GIAN & BIỂU ĐỒ
    // ========================================================

    public double doanhThuKhoangThoiGian(java.sql.Date tuNgay, java.sql.Date denNgay) {
        double tong = 0;
        String sql = "SELECT SUM(tongTien) AS tong FROM HoaDon WHERE CAST(ngayLap AS DATE) BETWEEN ? AND ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, tuNgay);
            ps.setDate(2, denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tong = rs.getDouble("tong");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tong;
    }

    // [HÀM MỚI] Tính lợi nhuận thực tế (Tiền Lãi)
    public double loiNhuanTheoKhoang(java.sql.Date tuNgay, java.sql.Date denNgay) {
        double loiNhuan = 0;
        String sql = "SELECT SUM((sp.giaBan - sp.giaNhap) * cthd.soLuong) AS tienLai " +
                "FROM HoaDon hd " +
                "JOIN ChiTietHoaDon cthd ON hd.maHoaDon = cthd.maHoaDon " +
                "JOIN SanPham sp ON cthd.maSanPham = sp.maSanPham " +
                "WHERE CAST(hd.ngayLap AS DATE) >= ? AND CAST(hd.ngayLap AS DATE) <= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // ĐÃ SỬA: Dùng setDate() thay vì setString() để giao tiếp chuẩn với SQL
            ps.setDate(1, tuNgay);
            ps.setDate(2, denNgay);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) loiNhuan = rs.getDouble("tienLai");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loiNhuan;
    }


    // ========================================================
    // NHÓM 3: BẢNG XẾP HẠNG (TOP 5)
    // ========================================================

    public List<SanPham> layTop5BanChayTrongThang() {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT TOP 5 sp.maSanPham, sp.tenSanPham, sp.giaNhap, sp.giaBan, sp.soLuongTon, sp.ngayHetHan, sp.maLoai, sp.maNCC " +
                "FROM SanPham sp " +
                "JOIN ChiTietHoaDon cthd ON sp.maSanPham = cthd.maSanPham " +
                "JOIN HoaDon hd ON cthd.maHoaDon = hd.maHoaDon " +
                "WHERE MONTH(hd.ngayLap) = MONTH(GETDATE()) AND YEAR(hd.ngayLap) = YEAR(GETDATE()) " +
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
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // [HÀM MỚI] Lấy Top 5 khách hàng chi tiêu nhiều nhất trong tháng
    public List<Map<String, Object>> layTop5KhachHangVIP() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT TOP 5 kh.maKhachHang, kh.tenKhachHang, kh.soDienThoai, SUM(hd.tongTien) AS tongChiTieu " +
                "FROM HoaDon hd " +
                "JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang " +
                "WHERE MONTH(hd.ngayLap) = MONTH(GETDATE()) AND YEAR(hd.ngayLap) = YEAR(GETDATE()) " +
                "GROUP BY kh.maKhachHang, kh.tenKhachHang, kh.soDienThoai " +
                "ORDER BY tongChiTieu DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> mapKH = new HashMap<>();
                mapKH.put("maKhachHang", rs.getString("maKhachHang"));
                mapKH.put("tenKhachHang", rs.getString("tenKhachHang"));
                mapKH.put("soDienThoai", rs.getString("soDienThoai"));
                mapKH.put("tongChiTieu", rs.getDouble("tongChiTieu"));
                list.add(mapKH);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    public List<SanPham> layHangSapHet() {
        List<SanPham> list = new ArrayList<>();
        // Ưu tiên hiện những thằng số lượng ít nhất (nguy cấp nhất) lên đầu bảng
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
    // Lấy tọa độ biểu đồ theo khoảng thời gian tùy chọn
    public List<ThongKeBieuDo> layDoanhThuBieuDoTheoKhoang(String tuNgay, String denNgay) {
        List<ThongKeBieuDo> list = new ArrayList<>();
        String sql = "SELECT FORMAT(ngayLap, 'dd/MM') AS ngay, SUM(tongTien) AS tongDoanhThu " +
                "FROM HoaDon " +
                "WHERE CAST(ngayLap AS DATE) >= ? AND CAST(ngayLap AS DATE) <= ? " +
                "GROUP BY FORMAT(ngayLap, 'dd/MM'), CAST(ngayLap AS DATE) " +
                "ORDER BY CAST(ngayLap AS DATE) ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tuNgay);
            ps.setString(2, denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ThongKeBieuDo(rs.getString("ngay"), rs.getDouble("tongDoanhThu")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public List<ThongKeBieuDo> layDoanhThu7NgayQua() {
        List<ThongKeBieuDo> list = new ArrayList<>();
        String sql = "SELECT FORMAT(ngayLap, 'dd/MM') AS ngay, SUM(tongTien) AS tongDoanhThu " +
                "FROM HoaDon " +
                "WHERE CAST(ngayLap AS DATE) >= DATEADD(day, -6, CAST(GETDATE() AS DATE)) " +
                "GROUP BY FORMAT(ngayLap, 'dd/MM'), CAST(ngayLap AS DATE) " +
                "ORDER BY CAST(ngayLap AS DATE) ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ThongKeBieuDo(rs.getString("ngay"), rs.getDouble("tongDoanhThu")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}