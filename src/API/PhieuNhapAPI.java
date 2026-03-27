package API;

import DAO.PhieuNhapDAO;
import model.ChiTietPhieuNhap;
import model.PhieuNhap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/API/PhieuNhapAPI")
public class PhieuNhapAPI extends HttpServlet {
    private PhieuNhapDAO dao = new PhieuNhapDAO();
    private Gson gson = new Gson();
    // ================= SỬA generateMaPN =================
    private String generateMaPN() {
        int max = dao.getMaxMaPN(); // gọi DAO
        return String.format("PN%03d", max + 1);
    }
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
    @Override
    // GET (Lịch sử phiếu nhập)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        JsonObject json = new JsonObject();
        try {
            var list = dao.layDanhSachPhieuNhap();
            response.setStatus(HttpServletResponse.SC_OK);
            json.addProperty("status", "success");
            json.add("data", gson.toJsonTree(list));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("status", "error");
            json.addProperty("message", "Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }

        response.getWriter().print(json.toString());
    }
    // POST (Transaction)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. Nhận tiếng Việt từ Postman
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        JsonObject json = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            // Đọc JSON vào class DTO nội bộ ở cuối file
            PhieuNhapRequest reqData = gson.fromJson(reader, PhieuNhapRequest.class);
            if (reqData == null || reqData.getChiTiet() == null || reqData.getChiTiet().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addProperty("status", "error");
                json.addProperty("message", "Dữ liệu trống hoặc thiếu chi tiết phiếu nhập!");
                response.getWriter().print(json.toString());
                return;
            }
            if (reqData.getMaNCC() == null || reqData.getMaNhanVien() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addProperty("status", "error");
                json.addProperty("message", "Thiếu mã NCC hoặc nhân viên!");
                response.getWriter().print(json.toString());
                return;
            }
            for (ChiTietRequest item : reqData.getChiTiet()) {
                if (item.getMaSanPham() == null || item.getMaSanPham().trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addProperty("status", "error");
                    json.addProperty("message", "Thiếu mã sản phẩm!");
                    response.getWriter().print(json.toString());
                    return;
                }
                if (item.getSoLuong() <= 0 || item.getGiaNhap() <= 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addProperty("status", "error");
                    json.addProperty("message", "Số lượng hoặc giá nhập không hợp lệ!");
                    response.getWriter().print(json.toString());
                    return;
                }
            }
            PhieuNhap pn = new PhieuNhap();
            pn.setMaPhieuNhap(generateMaPN());
            pn.setNgayNhap(new Timestamp(System.currentTimeMillis()));
            pn.setMaNhanVien(reqData.getMaNhanVien());
            pn.setMaNCC(reqData.getMaNCC());

            List<ChiTietPhieuNhap> dsChiTiet = new ArrayList<>();
            for (ChiTietRequest item : reqData.getChiTiet()) {
                ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
                ct.setMaPhieuNhap(pn.getMaPhieuNhap());
                ct.setMaSanPham(item.getMaSanPham());
                ct.setSoLuong(item.getSoLuong());
                ct.setGiaNhap(item.getGiaNhap());
                dsChiTiet.add(ct);
            }

            boolean success = dao.taoPhieuNhap(pn, dsChiTiet);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                json.addProperty("status", "success");
                json.addProperty("message", "Tạo phiếu nhập và cập nhật kho thành công!");
                json.addProperty("maPN", pn.getMaPhieuNhap());
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                json.addProperty("status", "error");
                json.addProperty("message", "Tạo thất bại do lỗi Transaction!");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("status", "error");
            json.addProperty("message", "Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }
        response.getWriter().print(json.toString());
    }

    //CÁC CLASS NỘI BỘ DÙNG ĐỂ GSON ĐỌC JSON (DTO)
    private class PhieuNhapRequest {
        private String maNCC;
        private String maNhanVien;
        private List<ChiTietRequest> chiTiet;

        public String getMaNCC() { return maNCC; }
        public String getMaNhanVien() { return maNhanVien; }
        public List<ChiTietRequest> getChiTiet() { return chiTiet; }
    }

    private class ChiTietRequest {
        private String maSanPham;
        private int soLuong;
        private double giaNhap;

        public String getMaSanPham() { return maSanPham; }
        public int getSoLuong() { return soLuong; }
        public double getGiaNhap() { return giaNhap; }
    }
}