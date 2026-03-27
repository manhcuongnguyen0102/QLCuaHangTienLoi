package API;

import DAO.SanPhamDAO;
import com.google.gson.GsonBuilder;
import model.SanPham;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/API/SanPhamAPI/*")
public class SanPhamAPI extends HttpServlet {
    private SanPhamDAO dao = new SanPhamDAO();
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    // Hàm hỗ trợ thiết lập Response chung (UTF-8 và CORS)
    private void setHeader(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setHeader(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // GET: Lấy danh sách sản phẩm (Đã kèm TenLoai và TenNCC từ DAO)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setHeader(resp);
        PrintWriter out = resp.getWriter();

        try {
            List<SanPham> ds = dao.layTatCa();
            // Gson sẽ tự động biến List thành chuỗi JSON chuẩn
            out.print(gson.toJson(ds));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\", \"message\":\"Lỗi lấy danh sách sản phẩm\"}");
        }
    }

    // POST: Thêm sản phẩm mới
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        setHeader(resp);
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            // Đọc JSON từ body gửi lên và biến thành đối tượng SanPham
            BufferedReader reader = req.getReader();
            SanPham sp = gson.fromJson(reader, SanPham.class);
            sp.setSoLuongTon(0);
            if (sp == null || sp.getTenSanPham() == null||sp.getTenSanPham().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Dữ liệu không hợp lệ!");

            }
            else if (sp.getGiaBan() < sp.getGiaNhap()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Giá bán không được nhỏ hơn giá nhập!");
            }
            else if (sp.getMaLoai() == null || sp.getMaNCC() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Thiếu mã Loại hoặc mã Nhà cung cấp!");
            }
            else {
                // Gọi DAO (Hàm them() của bạn đã có logic tự gọi sinhMaSanPhamMoi() rồi)
                if (dao.them(sp)) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Thêm sản phẩm thành công!");
                    jsonResponse.addProperty("maSanPham", sp.getMaSanPham()); // Trả về mã vừa sinh
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Thêm sản phẩm thất bại!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Lỗi định dạng dữ liệu gửi lên!");
        } finally {
            out.print(jsonResponse.toString());
        }
    }

    // PUT: Cập nhật thông tin sản phẩm
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        setHeader(resp);
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            SanPham sp = gson.fromJson(req.getReader(), SanPham.class);

            if (sp == null || sp.getMaSanPham() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Thiếu mã sản phẩm để cập nhật!");
            } else if (dao.sua(sp)) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Cập nhật sản phẩm thành công!");
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Cập nhật thất bại!");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Lỗi server: " + e.getMessage());
        } finally {
            out.print(jsonResponse.toString());
        }
    }

    // DELETE: Xóa an toàn (Check khóa ngoại)
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setHeader(resp);
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String maSP = req.getParameter("maSanPham");

        if (maSP == null || maSP.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Thiếu mã sản phẩm!");
        } else {
            // Nhận kết quả từ hàm xoa() mới của DAO: 1: Xóa OK, 2: Đã bán - Không cho xóa, 0: Lỗi
            int ketQua = dao.xoa(maSP);

            if (ketQua == 1) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Xóa sản phẩm thành công!");
            } else if (ketQua == 2) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT); // Lỗi 409: Xung đột dữ liệu
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Không thể xóa! Sản phẩm này đã có trong lịch sử hóa đơn.");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Xóa sản phẩm thất bại!");
            }
        }
        out.print(jsonResponse.toString());
    }
}