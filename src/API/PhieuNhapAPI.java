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


@WebServlet("/API/PhieuNhapAPI/*")
public class PhieuNhapAPI extends HttpServlet {
    private PhieuNhapDAO dao = new PhieuNhapDAO();
    private Gson gson = new Gson();


    private void setHeader(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private String generateMaPN() {
        return dao.sinhMaPhieuNhapMoi();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setHeader(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }


    // GET: LẤY DANH SÁCH HOẶC LẤY CHI TIẾT 1 PHIẾU

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setHeader(response);
        JsonObject json = new JsonObject();

        try {
            String maPhieuNhap = request.getParameter("maPhieuNhap");

            if (maPhieuNhap != null && !maPhieuNhap.trim().isEmpty()) {
                // TÍNH NĂNG MỚI: Lấy thông tin 1 Phiếu + Danh sách chi tiết của nó
                // (Bạn cần viết thêm hàm layChiTietPhieuNhap(maPN) trong DAO)
                PhieuNhap pn = dao.timTheoMa(maPhieuNhap);
                if (pn != null) {
                    List<ChiTietPhieuNhap> chiTiet = dao.layChiTietCuaPhieu(maPhieuNhap);

                    JsonObject dataObj = gson.toJsonTree(pn).getAsJsonObject();
                    dataObj.add("danhSachChiTiet", gson.toJsonTree(chiTiet)); // Nhét mảng chi tiết vào trong Object Phiếu Nhập

                    response.setStatus(HttpServletResponse.SC_OK);
                    json.addProperty("status", "success");
                    json.add("data", dataObj);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    json.addProperty("status", "error");
                    json.addProperty("message", "Không tìm thấy phiếu nhập!");
                }
            } else {
                // Lấy toàn bộ danh sách Phiếu Nhập (Header)
                var list = dao.layTatCa();
                response.setStatus(HttpServletResponse.SC_OK);
                json.addProperty("status", "success");
                json.add("data", gson.toJsonTree(list));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("status", "error");
            json.addProperty("message", "Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }
        response.getWriter().print(json.toString());
    }

    // ==========================================
    // POST: TẠO PHIẾU NHẬP (TRANSACTION)
    // ==========================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        setHeader(response);
        JsonObject json = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            PhieuNhapRequest reqData = gson.fromJson(reader, PhieuNhapRequest.class);

            // Validate DTO
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
                response.setStatus(HttpServletResponse.SC_CREATED); // Đã sửa thành 201 Created
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
            json.addProperty("message", "Lỗi định dạng dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
        response.getWriter().print(json.toString());
    }


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