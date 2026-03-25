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
    @Override
    public boolean capNhat(LoaiSanPham loai) {
        String sql = "UPDATE LoaiSanPham SET tenLoai = ?, moTa = ? WHERE maLoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, loai.getTenLoai());
            ps.setString(2, loai.getMoTa());
            ps.setString(3, loai.getMaLoai()); // Mã loại để ở cuối cùng ứng với dấu ? thứ 3

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public LoaiSanPham timTheoMa(String maLoai) {
        String sql = "SELECT * FROM LoaiSanPham WHERE maLoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLoai);

            // Lồng thêm try-with-resources cho ResultSet để tự động đóng
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LoaiSanPham loai = new LoaiSanPham();
                    loai.setMaLoai(rs.getString("maLoai"));
                    loai.setTenLoai(rs.getString("tenLoai"));
                    loai.setMoTa(rs.getString("moTa"));
                    return loai;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy
    }

    @Override
    public boolean xoa(String maLoai) {
        String sql = "DELETE FROM LoaiSanPham WHERE maLoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLoai);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Thêm dòng thông báo để nhóm dễ debug nếu vướng khóa ngoại
            System.err.println("Lỗi XÓA: Có thể loại sản phẩm '" + maLoai + "' đang chứa sản phẩm bên trong!");
            e.printStackTrace();
        }
        return false;
    }
}