package DAO;

import DaoInterFace.IKhachHangDAO;
import DaoInterFace.DBConnection;
import model.KhachHang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO implements IKhachHangDAO {

    @Override
    public List<KhachHang> layTatCa() {
        List<KhachHang> ds = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang";

        // Đưa Connection, Statement, ResultSet vào trong ngoặc tròn của try
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                KhachHang kh = new KhachHang();
                kh.setMaKhachHang(rs.getString("maKhachHang"));
                kh.setTenKhachHang(rs.getString("tenKhachHang"));
                kh.setSoDienThoai(rs.getString("soDienThoai"));
                kh.setNgayDangKy(rs.getDate("ngayDangKy"));
                kh.setTenDangNhap(rs.getString("tenDangNhap"));
                kh.setDiemTichLuy(rs.getInt("diemTichLuy"));
                ds.add(kh);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }
    @Override
    public String taoMaKhachHangMoi(Connection conn) throws SQLException {
        String sql = "SELECT TOP 1 maKhachHang FROM KhachHang ORDER BY maKhachHang DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maCu = rs.getString("maKhachHang");
                int soTiepTheo = Integer.parseInt(maCu.substring(2)) + 1;
                return String.format("KH%03d", soTiepTheo);
            }
        }
        return "KH001";
    }

    @Override
    public KhachHang timTheoSDT(String sdt) {

        KhachHang kh = null;

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM KhachHang WHERE soDienThoai = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, sdt);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                kh = new KhachHang();

                kh.setMaKhachHang(rs.getString("maKhachHang"));
                kh.setTenKhachHang(rs.getString("tenKhachHang"));
                kh.setSoDienThoai(rs.getString("soDienThoai"));
                kh.setNgayDangKy(rs.getDate("ngayDangKy"));
                kh.setTenDangNhap(rs.getString("tenDangNhap"));
                kh.setDiemTichLuy(rs.getInt("diemTichLuy"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return kh;
    }


    @Override
    public boolean them(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (maKhachHang, tenKhachHang, soDienThoai, ngayDangKy, tenDangNhap, diemTichLuy) VALUES (?,?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // GỌI HÀM TỰ SINH MÃ Ở ĐÂY
            String maMoi = taoMaKhachHangMoi(conn);

            ps.setString(1, maMoi);
            ps.setString(2, kh.getTenKhachHang());
            ps.setString(3, kh.getSoDienThoai());
            // Xử lý ngày đăng ký: Nếu rỗng thì lấy ngày hiện tại của Server
            if (kh.getNgayDangKy() != null) {
                ps.setDate(4, kh.getNgayDangKy());
            } else {
                ps.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            }
            ps.setString(5, kh.getTenDangNhap());
            ps.setInt(6, kh.getDiemTichLuy());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean capNhatDiem(String maKH, int diemMoi) {

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "UPDATE KhachHang SET diemTichLuy=? WHERE maKhachHang=?";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, diemMoi);
            ps.setString(2, maKH);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    @Override
    public boolean capNhatThongTin(String maKH, String tenKHMoi, String sdtMoi) {
        String sql = "UPDATE KhachHang SET tenKhachHang=?, soDienThoai=? WHERE maKhachHang=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenKHMoi);
            ps.setString(2, sdtMoi);
            ps.setString(3, maKH);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}