package DAO;
import DaoInterFace.INhanVienDAO;
import DaoInterFace.DBConnection;
import model.NhanVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO implements INhanVienDAO {

    @Override
    public List<NhanVien> layTatCa() {

        List<NhanVien> ds = new ArrayList<>();

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM NhanVien";

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                NhanVien nv = new NhanVien();

                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setTenNhanVien(rs.getString("tenNhanVien"));
                nv.setChucVu(rs.getString("chucVu"));
                nv.setSoDienThoai(rs.getString("soDienThoai"));
                nv.setTenDangNhap(rs.getString("tenDangNhap"));

                ds.add(nv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    @Override
    public NhanVien timTheoMa(String maNV) {

        NhanVien nv = null;

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM NhanVien WHERE maNhanVien = ?";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, maNV);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                nv = new NhanVien();

                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setTenNhanVien(rs.getString("tenNhanVien"));
                nv.setChucVu(rs.getString("chucVu"));
                nv.setSoDienThoai(rs.getString("soDienThoai"));
                nv.setTenDangNhap(rs.getString("tenDangNhap"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return nv;
    }

    @Override
    public boolean them(NhanVien nv) {

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "INSERT INTO NhanVien VALUES (?,?,?,?,?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, nv.getMaNhanVien());
            ps.setString(2, nv.getTenNhanVien());
            ps.setString(3, nv.getChucVu());
            ps.setString(4, nv.getSoDienThoai());
            ps.setString(5, nv.getTenDangNhap());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean sua(NhanVien nv) {

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "UPDATE NhanVien SET tenNhanVien=?, chucVu=?, soDienThoai=?, tenDangNhap=? WHERE maNhanVien=?";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, nv.getTenNhanVien());
            ps.setString(2, nv.getChucVu());
            ps.setString(3, nv.getSoDienThoai());
            ps.setString(4, nv.getTenDangNhap());
            ps.setString(5, nv.getMaNhanVien());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean xoa(String maNV) {

        try {

            Connection conn = DBConnection.getConnection();

            String sql = "DELETE FROM NhanVien WHERE maNhanVien=?";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, maNV);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}