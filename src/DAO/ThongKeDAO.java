package DAO;

import DaoInterFace.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ThongKeDAO {

    // Tổng doanh thu toàn bộ
    public double tongDoanhThu() {

        double tong = 0;

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "SELECT SUM(tongTien) AS tong FROM HoaDon";

            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                tong = rs.getDouble("tong");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tong;
    }

    // Tổng số hóa đơn
    public int tongHoaDon() {

        int tong = 0;

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "SELECT COUNT(*) AS tong FROM HoaDon";

            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                tong = rs.getInt("tong");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tong;
    }

    // Doanh thu theo ngày
    public double doanhThuTheoNgay(String ngay) {

        double tong = 0;

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "SELECT SUM(tongTien) AS tong FROM HoaDon WHERE CAST(ngayLap AS DATE) = ?";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, ngay);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                tong = rs.getDouble("tong");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tong;
    }

    // Số lượng sản phẩm bán ra
    public int tongSanPhamBanRa() {

        int tong = 0;

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "SELECT SUM(soLuong) AS tong FROM ChiTietHoaDon";

            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                tong = rs.getInt("tong");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tong;
    }
}