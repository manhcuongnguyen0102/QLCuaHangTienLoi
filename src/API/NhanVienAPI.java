package API;

import DAO.NhanVienDAO;
import DAO.TaiKhoanDAO;
import DaoInterFace.DBConnection;
import model.NhanVien;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/API/NhanVienAPI")
public class NhanVienAPI extends HttpServlet {
    private NhanVienDAO nvDAO = new NhanVienDAO();
    private TaiKhoanDAO tkDAO = new TaiKhoanDAO();
    private Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setStatus(HttpServletResponse.SC_OK);
    }


    // GET
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            List<NhanVien> ds = nvDAO.layTatCa();


            com.google.gson.JsonArray jsonArray = new com.google.gson.JsonArray();

            for (NhanVien nv : ds) {

                JsonObject obj = gson.toJsonTree(nv).getAsJsonObject();


                boolean trangThai = true; // Mặc định là đang làm
                String sql = "SELECT trangThai FROM TaiKhoan WHERE tenDangNhap = ?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, nv.getTenDangNhap());
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            trangThai = rs.getBoolean("trangThai");
                        }
                    }
                }


                obj.addProperty("trangThai", trangThai);


                jsonArray.add(obj);
            }

            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Lấy danh sách nhân viên thành công!");


            jsonResponse.add("data", jsonArray);
            resp.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Lỗi server: " + e.getMessage());
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
            out.close();
        }
    }



    // POST: Thêm nhân viên mới
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            BufferedReader reader = req.getReader();
            NhanVienRequest nvReq = gson.fromJson(reader, NhanVienRequest.class);

            // Kiểm tra dữ liệu đầu vào
            if (nvReq == null || nvReq.getTenNhanVien() == null || nvReq.getTenDangNhap() == null || nvReq.getMatKhau() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Thiếu thông tin bắt buộc!");
                out.print(jsonResponse.toString());
                return;
            }

            // Kiểm tra username đã tồn tại
            if (tkDAO.kiemTraTonTai(nvReq.getTenDangNhap())) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Tên đăng nhập đã tồn tại!");
                out.print(jsonResponse.toString());
                return;
            }

            // ========================================================
            // ĐẨY HẾT VIỆC NẶNG CHO DAO XỬ LÝ
            // ========================================================
            NhanVien nvMoi = new NhanVien();
            nvMoi.setTenNhanVien(nvReq.getTenNhanVien());
            nvMoi.setChucVu(nvReq.getChucVu() != null ? nvReq.getChucVu() : "Nhân viên bán hàng");
            nvMoi.setSoDienThoai(nvReq.getSoDienThoai());
            nvMoi.setTenDangNhap(nvReq.getTenDangNhap());

            // Gọi siêu hàm Transaction bên DAO
            boolean isAdded = nvDAO.them(nvMoi, nvReq.getMatKhau());

            if (isAdded) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Thêm nhân viên thành công!");
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Lỗi lưu vào CSDL (Có thể do lỗi hệ thống)!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Dữ liệu gửi lên sai định dạng!");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
            out.close();
        }
    }

    // PUT: Cập nhật thông tin nhân viên
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();
        try {
            String action = req.getParameter("action");
            if ("restore".equals(action)) {
                String maNV = req.getParameter("maNV");
                if (maNV == null || maNV.trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Thiếu mã nhân viên!");
                    return;
                }
                NhanVien nv = nvDAO.timTheoMa(maNV);
                if (nv == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Không tìm thấy nhân viên!");
                    return;
                }

                // Gọi DAO cập nhật trạng thái về 1 (Đang làm việc)
                boolean restored = tkDAO.capNhatTrangThai(nv.getTenDangNhap(), 1);

                if (restored) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Khôi phục tài khoản thành công!");
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Khôi phục thất bại!");
                }
                return; // Xong việc thì dừng lại, không chạy xuống phần update JSON bên dưới nữa
            }
            BufferedReader reader = req.getReader();
            NhanVienUpdateRequest updateReq = gson.fromJson(reader, NhanVienUpdateRequest.class);

            if (updateReq == null || updateReq.getMaNhanVien() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Thiếu mã nhân viên!");
                out.print(jsonResponse.toString());
                return;
            }

            NhanVien nvCu = nvDAO.timTheoMa(updateReq.getMaNhanVien());
            if (nvCu == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Không tìm thấy nhân viên!");
                out.print(jsonResponse.toString());
                return;
            }

            // Gán giá trị mới, nếu không gửi thì giữ nguyên
            NhanVien nv = new NhanVien();
            nv.setMaNhanVien(updateReq.getMaNhanVien());
            nv.setTenNhanVien(updateReq.getTenNhanVien() != null ? updateReq.getTenNhanVien() : nvCu.getTenNhanVien());
            nv.setChucVu(updateReq.getChucVu() != null ? updateReq.getChucVu() : nvCu.getChucVu());
            nv.setSoDienThoai(updateReq.getSoDienThoai() != null ? updateReq.getSoDienThoai() : nvCu.getSoDienThoai());
            nv.setTenDangNhap(updateReq.getTenDangNhap() != null ? updateReq.getTenDangNhap() : nvCu.getTenDangNhap());

            boolean updated = nvDAO.sua(nv);
            if (updated) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Cập nhật nhân viên thành công!");
                jsonResponse.add("data", gson.toJsonTree(nv));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Cập nhật thất bại!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Lỗi server: " + e.getMessage());
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
            out.close();
        }
    }

    // DELETE: Nghỉ việc (vô hiệu hóa tài khoản)
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String maNV = req.getParameter("maNV");
        if (maNV == null || maNV.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Thiếu mã nhân viên!");
            out.print(jsonResponse.toString());
            return;
        }

        NhanVien nv = nvDAO.timTheoMa(maNV);
        if (nv == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Không tìm thấy nhân viên!");
            out.print(jsonResponse.toString());
            return;
        }

        boolean disabled = tkDAO.capNhatTrangThai(nv.getTenDangNhap(), 0);
        if (disabled) {
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Đã nghỉ việc (vô hiệu hóa tài khoản)!");
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Vô hiệu hóa tài khoản thất bại!");
        }
        out.print(jsonResponse.toString());
        out.flush();
        out.close();
    }

    // --- Class nội bộ để nhận dữ liệu từ client ---
    private class NhanVienRequest {
        private String tenNhanVien;
        private String soDienThoai;
        private String tenDangNhap;
        private String matKhau;
        private String chucVu; // optional

        public String getTenNhanVien() { return tenNhanVien; }
        public String getSoDienThoai() { return soDienThoai; }
        public String getTenDangNhap() { return tenDangNhap; }
        public String getMatKhau() { return matKhau; }
        public String getChucVu() { return chucVu; }
    }

    private class NhanVienUpdateRequest {
        private String maNhanVien;
        private String tenNhanVien;
        private String chucVu;
        private String soDienThoai;
        private String tenDangNhap;

        public String getMaNhanVien() { return maNhanVien; }
        public String getTenNhanVien() { return tenNhanVien; }
        public String getChucVu() { return chucVu; }
        public String getSoDienThoai() { return soDienThoai; }
        public String getTenDangNhap() { return tenDangNhap; }
    }
}