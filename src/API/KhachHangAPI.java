package API;
import DAO.KhachHangDAO;
import model.KhachHang;
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
import java.util.ArrayList;
import java.util.List;
@WebServlet("/API/KhachHangAPI")
public class KhachHangAPI  extends HttpServlet{
    private KhachHangDAO dao = new KhachHangDAO();
    private Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // ==========================================
    // 1. GET: LẤY DANH SÁCH HOẶC TÌM THEO SĐT
    // ==========================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Hỗ trợ tiếng Việt cho dữ liệu trả về
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String sdt = request.getParameter("sdt");
        List<KhachHang> danhSach = new ArrayList<>();

        // Nếu người dùng có gõ SĐT để tìm kiếm
        if (sdt != null && !sdt.trim().isEmpty()) {
            KhachHang kh = dao.timTheoSDT(sdt.trim());
            if (kh != null) {
                danhSach.add(kh); // Thêm 1 người vào danh sách để trả về mảng cho đồng nhất
            }
        } else {
            // Nếu không truyền SĐT thì lấy tất cả
            danhSach = dao.layTatCa();
        }

        // Đóng gói thành JSON chuẩn
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", "success");
        jsonResponse.add("data", gson.toJsonTree(danhSach));

        out.print(jsonResponse.toString());
        out.flush();
    }

    // ==========================================
    // 2. PUT: CẬP NHẬT THÔNG TIN KHÁCH HÀNG
    // ==========================================
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // CHỐNG LỖI FONT: Ép kiểu UTF-8 cho dữ liệu đọc vào
        request.setCharacterEncoding("UTF-8");

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            // Đọc cục JSON từ Frontend gửi lên
            BufferedReader reader = request.getReader();
            JsonObject data = gson.fromJson(reader, JsonObject.class);

            String maKH = data.get("maKH").getAsString();
            String tenKH = data.get("tenKH").getAsString(); // Tên có dấu tiếng Việt sẽ không bị lỗi nữa
            String sdt = data.get("sdt").getAsString();

            // Gọi hàm cập nhật từ DAO
            boolean isUpdated = dao.capNhatThongTin(maKH, tenKH, sdt);

            if (isUpdated) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Cập nhật thông tin khách hàng thành công!");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Không tìm thấy mã khách hàng này!");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Dữ liệu gửi lên không hợp lệ!");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        out.print(jsonResponse.toString());
        out.flush();
    }
}
