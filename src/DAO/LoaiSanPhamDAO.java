package DAO;

import DaoInterFace.DBConnection;
import DaoInterFace.ILoaiSanPhamDAO;
import model.LoaiSanPham;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoaiSanPhamDAO implements ILoaiSanPhamDAO {

    @Override
    public List<LoaiSanPham> layTatCa() {
        List<LoaiSanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM LoaiSanPham";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LoaiSanPham loai = new LoaiSanPham();
                loai.setMaLoai(rs.getString("maLoai"));
                loai.setTenLoai(rs.getString("tenLoai"));
                loai.setMoTa(rs.getString("moTa"));
                list.add(loai);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean them(LoaiSanPham loai) {
        String sql = "INSERT INTO LoaiSanPham (maLoai, tenLoai, moTa) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loai.getMaLoai());
            ps.setString(2, loai.getTenLoai());
            ps.setString(3, loai.getMoTa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}