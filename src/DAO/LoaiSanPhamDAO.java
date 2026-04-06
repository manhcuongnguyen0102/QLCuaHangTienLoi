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
        if (loai.getMaLoai() == null || loai.getMaLoai().trim().isEmpty()) {
            loai.setMaLoai(sinhMaLoaiSanPhamMoi());
        }

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
            ps.setString(3, loai.getMaLoai());

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
        return null;
    }
    @Override
    public String sinhMaLoaiSanPhamMoi(){
        String sql = "SELECT MAX(CAST(SUBSTRING(maLoai, 2, LEN(maLoai)) AS INT)) FROM LoaiSanPham";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next() && rs.getObject(1) != null) {
                int max = rs.getInt(1);
                // Tạo format L kèm 2 chữ số (VD: L01, L02, L10)
                return String.format("L%02d", max + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return "L01";
    }
    @Override
    public int xoa(String maLoai) {
        String sqlCheck = "SELECT COUNT(*) FROM SanPham WHERE maLoai = ?";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, maLoai);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return 2; // TRƯỜNG HỢP 2: Đang có sản phẩm -> Không được xóa
                    }
                }
            }

            // 2. Nếu không vướng sản phẩm nào, tiến hành Xóa cứng
            String sqlDelete = "DELETE FROM LoaiSanPham WHERE maLoai = ?";
            try (PreparedStatement psDelete = conn.prepareStatement(sqlDelete)) {
                psDelete.setString(1, maLoai);
                if (psDelete.executeUpdate() > 0) {
                    return 1; // TRƯỜNG HỢP 1: Xóa thành công
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}