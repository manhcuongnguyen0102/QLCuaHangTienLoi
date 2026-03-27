package DAO;

import DaoInterFace.DBConnection;
import DaoInterFace.IHoaDonDAO;
import model.ChiTietHoaDon;
import model.HoaDon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO implements IHoaDonDAO {


    public String sinhMaHoaDonMoi() {
        String sql = "SELECT MAX(CAST(SUBSTRING(maHoaDon, 3, LEN(maHoaDon)) AS INT)) FROM HoaDon";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next() && rs.getObject(1) != null) {
                int max = rs.getInt(1);
                return String.format("HD%02d", max + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "HD01";
    }


    @Override
    public boolean taoHoaDon(HoaDon hd, List<ChiTietHoaDon> dsChiTiet) {
        Connection conn = null;
        PreparedStatement psCheckKho = null;
        PreparedStatement psHD = null;
        PreparedStatement psCTHD = null;
        PreparedStatement psCapNhatKho = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false); // Bắt đầu Transaction

            //  KIỂM TRA TỒN KHO TRƯỚC KHI LÀM BẤT CỨ ĐIỀU GÌ
            String checkKhoSql = "SELECT soLuongTon FROM SanPham WHERE maSanPham = ?";
            psCheckKho = conn.prepareStatement(checkKhoSql);

            for (ChiTietHoaDon ct : dsChiTiet) {
                psCheckKho.setString(1, ct.getMaSanPham());
                try (ResultSet rsCheck = psCheckKho.executeQuery()) {
                    if (rsCheck.next()) {
                        int tonKho = rsCheck.getInt("soLuongTon");
                        if (tonKho < ct.getSoLuong()) {
                            // CẢNH BÁO: Kho không đủ hàng! Chặn giao dịch ngay lập tức
                            System.err.println("Lỗi: Sản phẩm " + ct.getMaSanPham() + " không đủ tồn kho! (Còn: " + tonKho + ", Khách mua: " + ct.getSoLuong() + ")");
                            conn.rollback();
                            return false;
                        }
                    } else {
                        System.err.println("Lỗi: Không tìm thấy mã sản phẩm " + ct.getMaSanPham());
                        conn.rollback();
                        return false;
                    }
                }
            }

            //  CHUẨN BỊ DỮ LIỆU HEADER
            if (hd.getMaHoaDon() == null || hd.getMaHoaDon().isEmpty()) {
                hd.setMaHoaDon(sinhMaHoaDonMoi());
            }
            if (hd.getNgayLap() == null) {
                hd.setNgayLap(new Timestamp(System.currentTimeMillis()));
            }

            // INSERT HÓA ĐƠN
            String sqlHD = "INSERT INTO HoaDon (maHoaDon, ngayLap, maNhanVien, maKhachHang, tongTien) VALUES (?, ?, ?, ?, ?)";
            psHD = conn.prepareStatement(sqlHD);
            psHD.setString(1, hd.getMaHoaDon());
            psHD.setTimestamp(2, hd.getNgayLap());
            psHD.setString(3, hd.getMaNhanVien());
            psHD.setString(4, hd.getMaKhachHang());
            psHD.setDouble(5, hd.getTongTien());
            psHD.executeUpdate();

            // INSERT CHI TIẾT & TRỪ KHO THEO BATCH
            String sqlCTHD = "INSERT INTO ChiTietHoaDon (maHoaDon, maSanPham, soLuong, giaBan) VALUES (?, ?, ?, ?)";
            String sqlCapNhatKho = "UPDATE SanPham SET soLuongTon = soLuongTon - ? WHERE maSanPham = ?";

            psCTHD = conn.prepareStatement(sqlCTHD);
            psCapNhatKho = conn.prepareStatement(sqlCapNhatKho);

            for (ChiTietHoaDon ct : dsChiTiet) {
                ct.setMaHoaDon(hd.getMaHoaDon()); // Gắn mã hóa đơn cho chi tiết

                // Param cho ChiTietHoaDon
                psCTHD.setString(1, ct.getMaHoaDon());
                psCTHD.setString(2, ct.getMaSanPham());
                psCTHD.setInt(3, ct.getSoLuong());
                psCTHD.setDouble(4, ct.getGiaBan());
                psCTHD.addBatch();

                // Param cho Trừ kho
                psCapNhatKho.setInt(1, ct.getSoLuong());
                psCapNhatKho.setString(2, ct.getMaSanPham());
                psCapNhatKho.addBatch();
            }

            psCTHD.executeBatch();
            psCapNhatKho.executeBatch();

            conn.commit(); // Thành công rực rỡ!
            System.out.println("Tạo hóa đơn thành công! Mã: " + hd.getMaHoaDon());
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Đã Rollback lại giao dịch tạo hóa đơn!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            try {
                if (psCheckKho != null) psCheckKho.close();
                if (psCapNhatKho != null) psCapNhatKho.close();
                if (psCTHD != null) psCTHD.close();
                if (psHD != null) psHD.close();
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


    @Override
    public List<HoaDon> layLichSu(String maKH) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon WHERE maKhachHang = ? ORDER BY ngayLap DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
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

    public List<HoaDon> layTatCa() {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon ORDER BY ngayLap DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMaHoaDon(rs.getString("maHoaDon"));
                hd.setNgayLap(rs.getTimestamp("ngayLap"));
                hd.setMaNhanVien(rs.getString("maNhanVien"));
                hd.setMaKhachHang(rs.getString("maKhachHang"));
                hd.setTongTien(rs.getDouble("tongTien"));
                list.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public HoaDon timTheoMa(String maHD) {
        String sql = "SELECT * FROM HoaDon WHERE maHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HoaDon hd = new HoaDon();
                    hd.setMaHoaDon(rs.getString("maHoaDon"));
                    hd.setNgayLap(rs.getTimestamp("ngayLap"));
                    hd.setMaNhanVien(rs.getString("maNhanVien"));
                    hd.setMaKhachHang(rs.getString("maKhachHang"));
                    hd.setTongTien(rs.getDouble("tongTien"));
                    return hd;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ChiTietHoaDon> layChiTietCuaHoaDon(String maHD) {
        List<ChiTietHoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietHoaDon WHERE maHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietHoaDon ct = new ChiTietHoaDon();
                    ct.setMaChiTiet(rs.getInt("maChiTiet"));
                    ct.setMaHoaDon(rs.getString("maHoaDon"));
                    ct.setMaSanPham(rs.getString("maSanPham"));
                    ct.setSoLuong(rs.getInt("soLuong"));
                    ct.setGiaBan(rs.getDouble("giaBan"));
                    list.add(ct);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}