package API;

import DAO.NhaCungCapDAO;
import model.NhaCungCap;
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

@WebServlet("/API/NhaCungCapAPI/*")
public class NhaCungCapAPI extends HttpServlet {

    private NhaCungCapDAO dao = new NhaCungCapDAO();
    private Gson gson = new Gson();

    // Hàm dùng chung cho sạch code
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

    // GET: Lấy danh sách NCC hoặc 1 NCC
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setHeader(response);
        JsonObject json = new JsonObject();
        try {
            String maNCC = request.getParameter("maNCC");
            String tenNCC = request.getParameter("tenNCC");
            
            if (maNCC != null && !maNCC.trim().isEmpty()) {
                NhaCungCap ncc = dao.timTheoMa(maNCC);
                if (ncc != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    json.addProperty("status", "success");
                    json.add("data", gson.toJsonTree(ncc));
                }
                else if (tenNCC != null && !tenNCC.trim().isEmpty()) {
                    List<NhaCungCap> list = dao.timTheoTen(tenNCC); // Nhớ viết hàm timTheoTen dùng LIKE %?% trong DAO nhé
                    response.setStatus(HttpServletResponse.SC_OK);
                    json.addProperty("status", "success");
                    json.add("data", gson.toJsonTree(list));
                }
                else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    json.addProperty("status", "error");
                    json.addProperty("message", "Không tìm thấy nhà cung cấp!");
                }
            } else {
                List<NhaCungCap> list = dao.layTatCa();
                response.setStatus(HttpServletResponse.SC_OK);
                json.addProperty("status", "success");
                json.add("data", gson.toJsonTree(list));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("status", "error");
            json.addProperty("message", "Lỗi server: " + e.getMessage());
        }
        response.getWriter().print(json.toString());
    }

    // POST: Thêm NCC
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        setHeader(response);
        JsonObject json = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            NhaCungCap ncc = gson.fromJson(reader, NhaCungCap.class);

            if (ncc == null || ncc.getTenNCC() == null || ncc.getTenNCC().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addProperty("status", "error");
                json.addProperty("message", "Tên NCC không được để trống!");
            } else if (ncc.getSoDienThoai()!=null&&!ncc.getSoDienThoai().matches("^[0-9]{10,11}$")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addProperty("status", "error");
                json.addProperty("message", "Số điện thoại không hợp lệ (Phải là 10-11 chữ số)!");
                
            } else {
                // (LUÔN GỌI SINH MÃ BÊN TRONG HÀM dao.them() HOẶC SQL, KHÔNG SINH Ở API)
                boolean success = dao.them(ncc);

                if (success) {
                    response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
                    json.addProperty("status", "success");
                    json.addProperty("message", "Thêm nhà cung cấp thành công!");
                    json.addProperty("maNCC", ncc.getMaNCC()); // Báo cho FE biết mã mới
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addProperty("status", "error");
                    json.addProperty("message", "Thêm thất bại (Có thể trùng mã/tên)!");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.addProperty("status", "error");
            json.addProperty("message", "Định dạng JSON gửi lên không hợp lệ!");
        }
        response.getWriter().print(json.toString());
    }

    // PUT: Sửa NCC
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        setHeader(response);
        JsonObject json = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            NhaCungCap ncc = gson.fromJson(reader, NhaCungCap.class);

            if (ncc == null || ncc.getMaNCC() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addProperty("status", "error");
                json.addProperty("message", "Thiếu mã NCC để cập nhật!");
            } else {
                boolean success = dao.capNhat(ncc);
                if (success) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    json.addProperty("status", "success");
                    json.addProperty("message", "Cập nhật thành công!");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    json.addProperty("status", "error");
                    json.addProperty("message", "Cập nhật thất bại (Không tìm thấy mã)!");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.addProperty("status", "error");
            json.addProperty("message", "Định dạng JSON gửi lên không hợp lệ!");
        }
        response.getWriter().print(json.toString());
    }

    // DELETE: Xóa An Toàn
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setHeader(response);
        JsonObject json = new JsonObject();

        try {
            String maNCC = request.getParameter("maNCC");

            if (maNCC == null || maNCC.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addProperty("status", "error");
                json.addProperty("message", "Thiếu mã NCC để xóa!");
            } else {
                // SỬ DỤNG HÀM XÓA AN TOÀN NHƯ ĐÃ THỎA THUẬN
                int ketQua = dao.xoa(maNCC);

                if (ketQua == 1) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    json.addProperty("status", "success");
                    json.addProperty("message", "Xóa nhà cung cấp thành công!");
                } else if (ketQua == 2) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT); // 409
                    json.addProperty("status", "error");
                    json.addProperty("message", "Không thể xóa! NCC này đang cung cấp sản phẩm trong kho.");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    json.addProperty("status", "error");
                    json.addProperty("message", "Xóa thất bại! Không tìm thấy mã NCC.");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("status", "error");
            json.addProperty("message", "Lỗi hệ thống khi xóa!");
        }
        response.getWriter().print(json.toString());
    }
}