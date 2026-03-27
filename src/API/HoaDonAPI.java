package API;

import DAO.HoaDonDAO;
import model.ChiTietHoaDon;
import model.HoaDon;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/API/HoaDonAPI/*")
public class HoaDonAPI extends HttpServlet {
    private HoaDonDAO dao = new HoaDonDAO();
    private Gson gson = new Gson();


    private void setHeader(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setHeader(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setHeader(response);
        JsonObject json = new JsonObject();

        try {
            String maHoaDon = request.getParameter("maHoaDon");
            String maKhachHang = request.getParameter("maKhachHang");

            if (maHoaDon != null && !maHoaDon.trim().isEmpty()) {
                // TRƯỜNG HỢP 1: Lấy thông tin 1 Hóa Đơn kèm Chi Tiết
                HoaDon hd = dao.timTheoMa(maHoaDon);
                if (hd != null) {
                    List<ChiTietHoaDon> chiTiet = dao.layChiTietCuaHoaDon(maHoaDon);

                    // Nhét mảng chi tiết vào trong Object Hóa Đơn
                    JsonObject dataObj = gson.toJsonTree(hd).getAsJsonObject();
                    dataObj.add("danhSachChiTiet", gson.toJsonTree(chiTiet));

                    response.setStatus(HttpServletResponse.SC_OK);
                    json.addProperty("status", "success");
                    json.add("data", dataObj);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    json.addProperty("status", "error");
                    json.addProperty("message", "Không tìm thấy hóa đơn!");
                }
            } else if (maKhachHang != null && !maKhachHang.trim().isEmpty()) {
                // TRƯỜNG HỢP 2: Lấy lịch sử mua hàng của 1 Khách Hàng
                List<HoaDon> list = dao.layLichSu(maKhachHang);
                response.setStatus(HttpServletResponse.SC_OK);
                json.addProperty("status", "success");
                json.add("data", gson.toJsonTree(list));
            } else {
                // TRƯỜNG HỢP 3: Lấy toàn bộ danh sách Hóa Đơn (Cho Quản lý xem)
                List<HoaDon> list = dao.layTatCa();
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


    // POST: TẠO HÓA ĐƠN BÁN HÀNG (TRANSACTION)

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        setHeader(response);
        JsonObject json = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            // Đọc JSON từ Frontend ném lên vào Class DTO
            HoaDonRequest reqData = gson.fromJson(reader, HoaDonRequest.class);

            // 1. Validate Dữ liệu cơ bản
            if (reqData == null || reqData.getDanhSachChiTiet() == null || reqData.getDanhSachChiTiet().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addProperty("status", "error");
                json.addProperty("message", "Giỏ hàng trống! Vui lòng chọn sản phẩm.");
                response.getWriter().print(json.toString());
                return;
            }
            if (reqData.getMaNhanVien() == null || reqData.getMaNhanVien().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addProperty("status", "error");
                json.addProperty("message", "Thiếu mã nhân viên thu ngân!");
                response.getWriter().print(json.toString());
                return;
            }

            // 2. Kiểm tra từng món hàng trong giỏ
            for (ChiTietHDRequest item : reqData.getDanhSachChiTiet()) {
                if (item.getMaSanPham() == null || item.getMaSanPham().trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addProperty("status", "error");
                    json.addProperty("message", "Có sản phẩm bị thiếu mã!");
                    response.getWriter().print(json.toString());
                    return;
                }
                if (item.getSoLuong() <= 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addProperty("status", "error");
                    json.addProperty("message", "Số lượng bán phải lớn hơn 0!");
                    response.getWriter().print(json.toString());
                    return;
                }
            }

            // 3. Lắp ráp dữ liệu cho Header (Hóa Đơn)
            HoaDon hd = new HoaDon();
            // Mã Hóa Đơn và Ngày Lập sẽ do DAO tự sinh -> Không cần set ở đây
            hd.setMaNhanVien(reqData.getMaNhanVien());
            // Nếu khách vãng lai không có mã KH, DB có thể cho phép null
            hd.setMaKhachHang(reqData.getMaKhachHang());
            hd.setTongTien(reqData.getTongTien());

            // 4. Lắp ráp dữ liệu cho Details (Chi tiết hóa đơn)
            List<ChiTietHoaDon> dsChiTiet = new ArrayList<>();
            for (ChiTietHDRequest item : reqData.getDanhSachChiTiet()) {
                ChiTietHoaDon ct = new ChiTietHoaDon();
                ct.setMaSanPham(item.getMaSanPham());
                ct.setSoLuong(item.getSoLuong());
                ct.setGiaBan(item.getGiaBan());
                dsChiTiet.add(ct);
            }

            // 5. GỌI DAO THỰC THI TRANSACTION MUA HÀNG
            boolean success = dao.taoHoaDon(hd, dsChiTiet);

            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
                json.addProperty("status", "success");
                json.addProperty("message", "Thanh toán thành công!");
                json.addProperty("maHoaDon", hd.getMaHoaDon()); // Trả về mã HĐ vừa sinh
            } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT); // Lỗi 409 (Thường do hết hàng trong kho)
                json.addProperty("status", "error");
                json.addProperty("message", "Thanh toán thất bại! Có thể do sản phẩm trong kho không đủ số lượng.");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.addProperty("status", "error");
            json.addProperty("message", "Lỗi định dạng dữ liệu gửi lên: " + e.getMessage());
            e.printStackTrace();
        }
        response.getWriter().print(json.toString());
    }


    private class HoaDonRequest {
        private String maNhanVien;
        private String maKhachHang;
        private double tongTien;
        private List<ChiTietHDRequest> danhSachChiTiet;

        public String getMaNhanVien() { return maNhanVien; }
        public String getMaKhachHang() { return maKhachHang; }
        public double getTongTien() { return tongTien; }
        public List<ChiTietHDRequest> getDanhSachChiTiet() { return danhSachChiTiet; }
    }

    private class ChiTietHDRequest {
        private String maSanPham;
        private int soLuong;
        private double giaBan;

        public String getMaSanPham() { return maSanPham; }
        public int getSoLuong() { return soLuong; }
        public double getGiaBan() { return giaBan; }
    }
}