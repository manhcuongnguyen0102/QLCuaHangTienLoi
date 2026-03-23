package DAO;

import DaoInterFace.DBConnection;
import DaoInterFace.INhaCungCapDAO;
import model.NhaCungCap;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhaCungCapDAO implements INhaCungCapDAO {

    @Override
    public List<NhaCungCap> layTatCa() {
        List<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT * FROM NhaCungCap";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhaCungCap ncc = new NhaCungCap();
                ncc.setMaNCC(rs.getString("maNCC"));
                ncc.setTenNCC(rs.getString("tenNCC"));
                ncc.setDiaChi(rs.getString("diaChi"));
                ncc.setSoDienThoai(rs.getString("soDienThoai"));
                list.add(ncc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    @Override
    public boolean them(NhaCungCap ncc) {
        String sql = "INSERT INTO NhaCungCap (maNCC, tenNCC, diaChi, soDienThoai) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ncc.getMaNCC());
            ps.setString(2, ncc.getTenNCC());
            ps.setString(3, ncc.getDiaChi());
            ps.setString(4, ncc.getSoDienThoai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public NhaCungCap timTheoMa(String maNCC) {
        String sql = "SELECT * FROM NhaCungCap WHERE maNCC = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhaCungCap ncc = new NhaCungCap();
                    ncc.setMaNCC(rs.getString("maNCC"));
                    ncc.setTenNCC(rs.getString("tenNCC"));
                    ncc.setDiaChi(rs.getString("diaChi"));
                    ncc.setSoDienThoai(rs.getString("soDienThoai"));
                    return ncc;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean capNhat(NhaCungCap ncc) {
        String sql = "UPDATE NhaCungCap SET tenNCC = ?, diaChi = ?, soDienThoai = ? WHERE maNCC = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ncc.getTenNCC());
            ps.setString(2, ncc.getDiaChi());
            ps.setString(3, ncc.getSoDienThoai());
            ps.setString(4, ncc.getMaNCC());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean xoa(String maNCC) {
        String sql = "DELETE FROM NhaCungCap WHERE maNCC = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNCC);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi XÓA: Nhà cung cấp '" + maNCC + "' có thể đang gắn với các Phiếu Nhập trong kho!");
            e.printStackTrace();
        }
        return false;
    }
}