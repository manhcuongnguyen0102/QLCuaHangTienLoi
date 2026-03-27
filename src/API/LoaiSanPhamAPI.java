package API;

import DAO.LoaiSanPhamDAO;
import model.LoaiSanPham;
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
import java.util.List;

// Đừng quên dấu /* ở cuối để bắt mọi đường dẫn
@WebServlet("/API/LoaiSanPhamAPI/*")
public class LoaiSanPhamAPI extends HttpServlet {

    private LoaiSanPhamDAO dao = new LoaiSanPhamDAO();
    private Gson gson = new Gson();

    // Chuẩn hóa Header (UTF-8 và CORS)
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

    // GET: Lấy danh sách Loại
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setHeader(response);
        PrintWriter out = response.getWriter();
        try {
            String maLoai = request.getParameter("maLoai");

            if (maLoai != null && !maLoai.trim().isEmpty()) {
                // Trường hợp 1: Có truyền mã -> Trả về 1 đối tượng
                LoaiSanPham loai = dao.timTheoMa(maLoai); // (Hàm này bạn đã viết trong DAO rồi)
                if (loai != null) {
                    out.print(gson.toJson(loai));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND); // Lỗi 404
                    out.print("{\"status\":\"error\", \"message\":\"Không tìm thấy loại sản phẩm!\"}");
                }
            } else {
                // Trường hợp 2: Không truyền mã -> Trả về danh sách (Như cũ)
                List<LoaiSanPham> list = dao.layTatCa();
                out.print(gson.toJson(list));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\", \"message\":\"Lỗi lấy dữ liệu!\"}");
        }
    }


    // POST: Thêm Loại mới
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        setHeader(response);
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            LoaiSanPham loai = gson.fromJson(reader, LoaiSanPham.class);

            if (loai == null || loai.getTenLoai() == null || loai.getTenLoai().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Tên loại không được để trống!");
            } else {
                // Nếu chưa có mã, có thể gọi dao.sinhMaLoaiMoi() giống bên SanPham
                boolean kq = dao.them(loai);
                if (kq) {
                    response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Thêm loại sản phẩm thành công!");
                    jsonResponse.addProperty("maLoai", loai.getMaLoai());
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Thêm thất bại (Có thể trùng mã/tên)!");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Định dạng JSON không hợp lệ!");
        } finally {
            out.print(jsonResponse.toString());
        }
    }

    // PUT: Cập nhật Loại
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        setHeader(response);
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            LoaiSanPham loai = gson.fromJson(reader, LoaiSanPham.class);

            if (loai == null || loai.getMaLoai() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Thiếu mã loại sản phẩm để cập nhật!");
            } else {
                boolean kq = dao.capNhat(loai);
                if (kq) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Cập nhật thành công!");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Cập nhật thất bại!");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Định dạng JSON không hợp lệ!");
        } finally {
            out.print(jsonResponse.toString());
        }
    }

    // DELETE: Xóa Loại
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setHeader(response);
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String maLoai = request.getParameter("maLoai");

        if (maLoai == null || maLoai.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Thiếu mã loại sản phẩm!");
        } else {
            // Gọi hàm xóa an toàn từ DAO
            int ketQua = dao.xoa(maLoai);

            if (ketQua == 1) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Xóa loại sản phẩm thành công!");
            } else if (ketQua == 2) {
                response.setStatus(HttpServletResponse.SC_CONFLICT); // Lỗi 409: Xung đột dữ liệu
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Không thể xóa! Đang có sản phẩm thuộc loại này trong kho.");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Lỗi 400
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Xóa thất bại! Mã loại không tồn tại hoặc lỗi hệ thống.");
            }
        }
        out.print(jsonResponse.toString());
    }
}