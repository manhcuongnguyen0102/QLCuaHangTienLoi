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

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM KhachHang";

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

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

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "INSERT INTO KhachHang VALUES (?,?,?,?,?,?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, kh.getMaKhachHang());
            ps.setString(2, kh.getTenKhachHang());
            ps.setString(3, kh.getSoDienThoai());
            ps.setDate(4, kh.getNgayDangKy());
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
}