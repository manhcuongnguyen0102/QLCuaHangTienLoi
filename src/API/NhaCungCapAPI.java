package API;

import DAO.NhaCungCapDAO;
import model.NhaCungCap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.List;

@WebServlet("/API/NhaCungCapAPI")
public class NhaCungCapAPI extends HttpServlet {

    private NhaCungCapDAO dao = new NhaCungCapDAO();
    private Gson gson = new Gson();

    // CORS (giống LoginAPI)
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
    //  GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        JsonObject json = new JsonObject();
        try {
            List<NhaCungCap> list = dao.layTatCa();

            response.setStatus(HttpServletResponse.SC_OK);
            json.addProperty("status", "success");
            json.add("data", gson.toJsonTree(list));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("status", "error");
            json.addProperty("message", "Lỗi server: " + e.getMessage());
        }

        response.getWriter().print(json.toString());
    }

    //  POST (THÊM)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        JsonObject json = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            NhaCungCap ncc = gson.fromJson(reader, NhaCungCap.class);

            // validate
            if (ncc.getTenNCC() == null || ncc.getTenNCC().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addProperty("status", "error");
                json.addProperty("message", "Tên NCC không được để trống!");
                response.getWriter().print(json.toString());
                return;
            }

            // sinh mã
            ncc.setMaNCC(generateId());

            boolean success = dao.them(ncc);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                json.addProperty("status", "success");
                json.addProperty("message", "Thêm nhà cung cấp thành công!");
                json.add("data", gson.toJsonTree(ncc));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                json.addProperty("status", "error");
                json.addProperty("message", "Thêm thất bại!");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("status", "error");
            json.addProperty("message", "Lỗi server: " + e.getMessage());
        }

        response.getWriter().print(json.toString());
    }

    //  PUT (SỬA)
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        JsonObject json = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            NhaCungCap ncc = gson.fromJson(reader, NhaCungCap.class);

            if (ncc.getMaNCC() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addProperty("status", "error");
                json.addProperty("message", "Thiếu mã NCC!");
                response.getWriter().print(json.toString());
                return;
            }

            boolean success = dao.capNhat(ncc);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                json.addProperty("status", "success");
                json.addProperty("message", "Cập nhật thành công!");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                json.addProperty("status", "error");
                json.addProperty("message", "Không tìm thấy NCC!");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("status", "error");
            json.addProperty("message", "Lỗi server: " + e.getMessage());
        }

        response.getWriter().print(json.toString());
    }

    //  DELETE
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        JsonObject json = new JsonObject();
        try {
            String maNCC = request.getParameter("maNCC");

            if (maNCC == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addProperty("status", "error");
                json.addProperty("message", "Thiếu mã NCC!");
                response.getWriter().print(json.toString());
                return;
            }
            boolean success = dao.xoa(maNCC);
            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                json.addProperty("status", "success");
                json.addProperty("message", "Xóa thành công!");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                json.addProperty("status", "error");
                json.addProperty("message", "Không tìm thấy NCC!");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("status", "error");
            json.addProperty("message", "Lỗi server: " + e.getMessage());
        }

        response.getWriter().print(json.toString());
    }
    //  SINH MÃ
    private String generateId() {
        List<NhaCungCap> list = dao.layTatCa();
        int max = 0;
        for (NhaCungCap ncc : list) {
            String id = ncc.getMaNCC().replace("NCC", "");
            int num = Integer.parseInt(id);
            if (num > max) max = num;
        }
        return String.format("NCC%02d", max + 1);
    }
}