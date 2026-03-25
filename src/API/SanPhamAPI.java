package API;

import DAO.SanPhamDAO;
import DaoInterFace.DBConnection;
import model.SanPham;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.*;
import java.sql.*;
import java.sql.Date;

@WebServlet("/API/SanPhamAPI")
public class SanPhamAPI extends HttpServlet {

    private SanPhamDAO dao = new SanPhamDAO();

    //  GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        String sql = "SELECT sp.*, l.tenLoai, n.tenNCC " +
                "FROM SanPham sp " +
                "JOIN LoaiSanPham l ON sp.maLoai = l.maLoai " +
                "JOIN NhaCungCap n ON sp.maNCC = n.maNCC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            PrintWriter out = response.getWriter();
            out.print("[");

            boolean first = true;
            while (rs.next()) {
                if (!first) out.print(",");
                first = false;

                out.print("{");
                out.print("\"maSanPham\":\"" + rs.getString("maSanPham") + "\",");
                out.print("\"tenSanPham\":\"" + rs.getString("tenSanPham") + "\",");
                out.print("\"giaNhap\":" + rs.getDouble("giaNhap") + ",");
                out.print("\"giaBan\":" + rs.getDouble("giaBan") + ",");
                out.print("\"soLuongTon\":" + rs.getInt("soLuongTon") + ",");
                out.print("\"ngayHetHan\":\"" + rs.getDate("ngayHetHan") + "\",");
                out.print("\"tenLoai\":\"" + rs.getString("tenLoai") + "\",");
                out.print("\"tenNCC\":\"" + rs.getString("tenNCC") + "\"");
                out.print("}");
            }

            out.print("]");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();

        try {
            String ten = request.getParameter("tenSanPham");
            double giaNhap = Double.parseDouble(request.getParameter("giaNhap"));
            double giaBan = Double.parseDouble(request.getParameter("giaBan"));
            int soLuong = Integer.parseInt(request.getParameter("soLuongTon"));
            String ngay = request.getParameter("ngayHetHan");
            String maLoai = request.getParameter("maLoai");
            String maNCC = request.getParameter("maNCC");

            //  Check giá
            if (giaBan <= giaNhap) {out.print("{\"message\":\"Giá bán phải lớn hơn giá nhập\"}");
                return;
            }

            String maSP = "SP001";

            try (Connection conn = DBConnection.getConnection()) {


                PreparedStatement ps = conn.prepareStatement(
                        "SELECT MAX(CAST(SUBSTRING(maSanPham, 3, LEN(maSanPham)) AS INT)) FROM SanPham");
                ResultSet rs = ps.executeQuery();

                if (rs.next() && rs.getObject(1) != null) {
                    int max = rs.getInt(1);
                    maSP = String.format("SP%03d", max + 1);
                }

                //  Check trùng (chống lỗi khi bấm nhanh)
                boolean exists;
                do {
                    exists = false;

                    PreparedStatement check = conn.prepareStatement(
                            "SELECT COUNT(*) FROM SanPham WHERE maSanPham = ?");
                    check.setString(1, maSP);
                    ResultSet rs2 = check.executeQuery();
                    rs2.next();

                    if (rs2.getInt(1) > 0) {
                        int num = Integer.parseInt(maSP.substring(2)) + 1;
                        maSP = String.format("SP%03d", num);
                        exists = true;
                    }

                } while (exists);
            }

            SanPham sp = new SanPham(
                    maSP, ten, giaNhap, giaBan, soLuong,
                    Date.valueOf(ngay), maLoai, maNCC
            );

            boolean kq = dao.them(sp);

            if (kq) {
                out.print("{\"message\":\"Thêm thành công\",\"maSanPham\":\"" + maSP + "\"}");
            } else {
                out.print("{\"message\":\"Thêm thất bại\"}");
            }

        } catch (Exception e) {
            out.print("{\"message\":\"Lỗi dữ liệu\"}");
            e.printStackTrace();
        }
    }

    //  PUT
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();

        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            String json = sb.toString();

            String maSP = json.split("\"maSanPham\":\"")[1].split("\"")[0];
            String ten = json.split("\"tenSanPham\":\"")[1].split("\"")[0];
            double giaNhap = Double.parseDouble(json.split("\"giaNhap\":")[1].split(",")[0]);
            double giaBan = Double.parseDouble(json.split("\"giaBan\":")[1].split(",")[0]);
            int soLuong = Integer.parseInt(json.split("\"soLuongTon\":")[1].split(",")[0]);String ngay = json.split("\"ngayHetHan\":\"")[1].split("\"")[0];
            String maLoai = json.split("\"maLoai\":\"")[1].split("\"")[0];
            String maNCC = json.split("\"maNCC\":\"")[1].split("\"")[0];

            if (giaBan <= giaNhap) {
                out.print("{\"message\":\"Giá bán phải lớn hơn giá nhập\"}");
                return;
            }

            SanPham sp = new SanPham(
                    maSP, ten, giaNhap, giaBan, soLuong,
                    Date.valueOf(ngay), maLoai, maNCC
            );

            boolean kq = dao.sua(sp);

            if (kq) {
                out.print("{\"message\":\"Cập nhật thành công\"}");
            } else {
                out.print("{\"message\":\"Cập nhật thất bại\"}");
            }

        } catch (Exception e) {
            out.print("{\"message\":\"Lỗi JSON\"}");
            e.printStackTrace();
        }
    }


    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String maSP = request.getParameter("maSanPham");

        //  check null
        if (maSP == null || maSP.trim().isEmpty()) {
            out.print("{\"message\":\"Thiếu mã sản phẩm\"}");
            return;
        }try (Connection conn = DBConnection.getConnection();
              PreparedStatement ps = conn.prepareStatement(
                      "SELECT COUNT(*) FROM ChiTietHoaDon WHERE maSanPham = ?")) {

            ps.setString(1, maSP);
            ResultSet rs = ps.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {

                // ❗ update trạng thái
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE SanPham SET trangThai = N'Ngừng kinh doanh' WHERE maSanPham = ?")) {

                    ps2.setString(1, maSP);
                    ps2.executeUpdate();
                }

                out.print("{\"message\":\"Sản phẩm đã bán, chuyển sang Ngừng kinh doanh\"}");

            } else {

                boolean kq = dao.xoa(maSP);

                if (kq) {
                    out.print("{\"message\":\"Xóa thành công\"}");
                } else {
                    out.print("{\"message\":\"Xóa thất bại\"}");
                }
            }

        } catch (Exception e) {
            out.print("{\"message\":\"Lỗi server\"}");
            e.printStackTrace();
        }
    }
}