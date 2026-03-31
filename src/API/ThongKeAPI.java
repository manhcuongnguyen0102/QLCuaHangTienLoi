package API;

import DAO.ThongKeDAO;
import model.SanPham;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.ThongKeBieuDo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet("/API/ThongKeAPI/*")
public class ThongKeAPI extends HttpServlet {
    private ThongKeDAO tkDAO = new ThongKeDAO();
    private Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        JsonObject jsonResponse = new JsonObject();

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Thiếu endpoint (VD: /tongquan, /top-sanpham, v.v.)");
            } else {
                switch (pathInfo) {
                    case "/tongquan":
                        handleTongQuan(jsonResponse);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        break;

                    case "/top-sanpham":
                        handleTopSanPham(jsonResponse);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        break;

                    case "/top-khachhang":
                        handleTopKhachHang(jsonResponse);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        break;

                    case "/doanhthu-tong":
                        handleDoanhThuTong(req, jsonResponse);
                        kiemTraVaSetStatus(jsonResponse, resp);
                        break;

                    case "/loinhuan-tong":
                        handleLoiNhuanTong(req, jsonResponse);
                        kiemTraVaSetStatus(jsonResponse, resp);
                        break;

                    case "/sanpham-saphet":
                        handleSanPhamSapHet(jsonResponse);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        break;

                    case "/doanhthu-bieudo":
                        handleBieuDoDoanhThu(req, jsonResponse);
                        kiemTraVaSetStatus(jsonResponse, resp);
                        break;

                    default:
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Endpoint không tồn tại");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Lỗi server: " + e.getMessage());
        }

        // Đã bỏ khối finally gây lỗi in đúp JSON. Chỉ in 1 lần duy nhất ở đây!
        try (PrintWriter out = resp.getWriter()) {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }

    private void kiemTraVaSetStatus(JsonObject jsonResponse, HttpServletResponse resp) {
        if (jsonResponse.has("status") && jsonResponse.get("status").getAsString().equals("error")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ (HANDLERS)
    // ==========================================

    private void handleTongQuan(JsonObject jsonResponse) {
        double dt = tkDAO.doanhThuHomNay();
        int soHD = tkDAO.soHoaDonHomNay();
        int soKH = tkDAO.soKhachHangMoiHomNay();
        int spSapHet = tkDAO.soSanPhamSapHet();

        // CHUẨN HÓA DATE: Tạo ngày hôm nay bằng java.sql.Date để truyền cho DAO
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        double loiNhuan = tkDAO.loiNhuanTheoKhoang(today, today);

        JsonObject data = new JsonObject();
        data.addProperty("doanhThu", dt);
        data.addProperty("loiNhuan", loiNhuan);
        data.addProperty("soHoaDon", soHD);
        data.addProperty("soKhachHang", soKH);
        data.addProperty("soSanPhamSapHet", spSapHet);

        jsonResponse.addProperty("status", "success");
        jsonResponse.addProperty("message", "Lấy thống kê tổng quan thành công!");
        jsonResponse.add("data", data);
    }

    private void handleTopSanPham(JsonObject jsonResponse) {
        List<SanPham> top5 = tkDAO.layTop5BanChayTrongThang();
        jsonResponse.addProperty("status", "success");
        jsonResponse.addProperty("message", "Lấy top 5 sản phẩm bán chạy thành công!");
        jsonResponse.add("data", gson.toJsonTree(top5));
    }

    private void handleTopKhachHang(JsonObject jsonResponse) {
        List<Map<String, Object>> top5VIP = tkDAO.layTop5KhachHangVIP();
        jsonResponse.addProperty("status", "success");
        jsonResponse.addProperty("message", "Lấy top 5 khách hàng VIP thành công!");
        jsonResponse.add("data", gson.toJsonTree(top5VIP));
    }

    private void handleDoanhThuTong(HttpServletRequest req, JsonObject jsonResponse) {
        String tuNgayStr = req.getParameter("tuNgay");
        String denNgayStr = req.getParameter("denNgay");

        if (tuNgayStr == null || denNgayStr == null || tuNgayStr.trim().isEmpty() || denNgayStr.trim().isEmpty()) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Thiếu tham số tuNgay hoặc denNgay!");
            return;
        }

        try {
            // CHUẨN HÓA DATE: Ép kiểu String (yyyy-MM-dd) sang java.sql.Date
            java.sql.Date tuNgay = java.sql.Date.valueOf(tuNgayStr);
            java.sql.Date denNgay = java.sql.Date.valueOf(denNgayStr);

            double tongDoanhThu = tkDAO.doanhThuKhoangThoiGian(tuNgay, denNgay);
            JsonObject data = new JsonObject();
            data.addProperty("tongDoanhThu", tongDoanhThu);

            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Tính tổng doanh thu thành công!");
            jsonResponse.add("data", data);
        } catch (IllegalArgumentException e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Định dạng ngày không hợp lệ! Vui lòng dùng yyyy-MM-dd.");
        }
    }

    private void handleLoiNhuanTong(HttpServletRequest req, JsonObject jsonResponse) {
        String tuNgayStr = req.getParameter("tuNgay");
        String denNgayStr = req.getParameter("denNgay");

        if (tuNgayStr == null || denNgayStr == null || tuNgayStr.trim().isEmpty() || denNgayStr.trim().isEmpty()) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Thiếu tham số tuNgay hoặc denNgay!");
            return;
        }

        try {

            java.sql.Date tuNgay = java.sql.Date.valueOf(tuNgayStr);
            java.sql.Date denNgay = java.sql.Date.valueOf(denNgayStr);

            double tongLoiNhuan = tkDAO.loiNhuanTheoKhoang(tuNgay, denNgay);
            JsonObject data = new JsonObject();
            data.addProperty("tongLoiNhuan", tongLoiNhuan);

            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Tính tổng lợi nhuận thành công!");
            jsonResponse.add("data", data);
        } catch (IllegalArgumentException e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Định dạng ngày không hợp lệ! Vui lòng dùng yyyy-MM-dd.");
        }
    }

    private void handleBieuDoDoanhThu(HttpServletRequest req, JsonObject jsonResponse) {
        String tuNgay = req.getParameter("tuNgay");
        String denNgay = req.getParameter("denNgay");
        List<ThongKeBieuDo> listBieuDo;

        // Vì hàm layDoanhThuBieuDoTheoKhoang trong DAO của bạn đang nhận vào String,
        // nên chỗ này truyền trực tiếp String xuống mà không cần ép kiểu Date.
        if (tuNgay != null && denNgay != null && !tuNgay.isEmpty() && !denNgay.isEmpty()) {
            listBieuDo = tkDAO.layDoanhThuBieuDoTheoKhoang(tuNgay, denNgay);
        } else {
            listBieuDo = tkDAO.layDoanhThu7NgayQua();
        }

        jsonResponse.addProperty("status", "success");
        jsonResponse.addProperty("message", "Lấy dữ liệu biểu đồ thành công!");
        jsonResponse.add("data", gson.toJsonTree(listBieuDo));
    }

    private void handleSanPhamSapHet(JsonObject jsonResponse) {
        List<SanPham> danhSachSapHet = tkDAO.layHangSapHet();
        jsonResponse.addProperty("status", "success");
        jsonResponse.addProperty("message", "Lấy danh sách sản phẩm sắp hết hàng thành công!");
        jsonResponse.add("data", gson.toJsonTree(danhSachSapHet));
    }
}