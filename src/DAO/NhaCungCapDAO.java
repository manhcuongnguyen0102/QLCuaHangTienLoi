package DAO;

import API.NhaCungCapAPI;
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
    public List<NhaCungCap> timTheoTen(String tenNCC){
        List<NhaCungCap> list= new ArrayList<>();
        String sql="Select * from NhaCungCap where tenNCC LIKE ?";
        try(Connection conn= DBConnection.getConnection();
            PreparedStatement ps= conn.prepareStatement(sql)){
            ps.setString(1,tenNCC);
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhaCungCap ncc = new NhaCungCap();
                    ncc.setMaNCC(rs.getString("maNCC"));
                    ncc.setTenNCC(rs.getString("tenNCC"));
                    ncc.setDiaChi(rs.getString("diaChi"));
                    ncc.setSoDienThoai(rs.getString("soDienThoai"));
                    list.add(ncc);
                }
            }
            }catch (SQLException e){
                e.printStackTrace();
            }

        return  list;

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
    public int xoa(String maNCC) {
        String sqlCheckSP = "SELECT COUNT(*) FROM SanPham WHERE maNCC = ?";

        // (Nếu DB của bạn đã có bảng PhieuNhap, nên check thêm cả PhieuNhap nữa cho chắc ăn)
        String sqlCheckPN = "SELECT COUNT(*) FROM PhieuNhap WHERE maNCC = ?";

        try (Connection conn = DBConnection.getConnection()) {

            // Check Sản Phẩm
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckSP)) {
                psCheck.setString(1, maNCC);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return 2; // Đang có sản phẩm -> Chặn
                }
            }

            // Check Phiếu Nhập (Tùy chọn, bảo vệ dữ liệu tuyệt đối)
            try (PreparedStatement psCheck2 = conn.prepareStatement(sqlCheckPN)) {
                psCheck2.setString(1, maNCC);
                try (ResultSet rs2 = psCheck2.executeQuery()) {
                    if (rs2.next() && rs2.getInt(1) > 0) return 2; // Đã từng nhập hàng -> Chặn
                }
            }

            // BƯỚC 2: Vượt qua các bài test thì Xóa cứng
            String sqlDelete = "DELETE FROM NhaCungCap WHERE maNCC = ?";
            try (PreparedStatement psDelete = conn.prepareStatement(sqlDelete)) {
                psDelete.setString(1, maNCC);
                if (psDelete.executeUpdate() > 0) {
                    return 1; // Xóa thành công
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Lỗi hệ thống hoặc mã sai
    }

    public String sinhMaNCCMoi() {
        // Mã là NCC01 -> Cắt từ vị trí thứ 4 trở đi để lấy phần số
        String sql = "SELECT MAX(CAST(SUBSTRING(maNCC, 4, LEN(maNCC)) AS INT)) FROM NhaCungCap";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next() && rs.getObject(1) != null) {
                int max = rs.getInt(1);
                return String.format("NCC%02d", max + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "NCC01"; // Nếu bảng rỗng, trả về mã đầu tiên
    }
}