package API;

import DAO.ThongKeDAO;
import model.SanPham;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Thiếu endpoint");
            out.print(jsonResponse.toString());
            return;
        }

        try {
            switch (pathInfo) {
                case "/tongquan":
                    handleTongQuan(jsonResponse);
                    break;
                case "/top-sanpham":
                    handleTopSanPham(jsonResponse);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Endpoint không tồn tại");
                    out.print(jsonResponse.toString());
                    return;
            }
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

    private void handleTongQuan(JsonObject jsonResponse) {
        double dt = tkDAO.doanhThuHomNay();
        int soHD = tkDAO.soHoaDonHomNay();
        int soKH = tkDAO.soKhachHangMoiHomNay();
        int spSapHet = tkDAO.soSanPhamSapHet();

        JsonObject data = new JsonObject();
        data.addProperty("doanhThuHomNay", dt);
        data.addProperty("soHoaDonHomNay", soHD);
        data.addProperty("soKhachHangMoiHomNay", soKH);
        data.addProperty("soSanPhamSapHet", spSapHet);

        jsonResponse.addProperty("status", "success");
        jsonResponse.addProperty("message", "Lấy thống kê tổng quan thành công!");
        jsonResponse.add("data", data);
    }

    private void handleTopSanPham(JsonObject jsonResponse) {
        List<SanPham> top5 = tkDAO.layTop5BanChayTrongThang();
        jsonResponse.addProperty("status", "success");
        jsonResponse.addProperty("message", "Lấy top 5 sản phẩm bán chạy trong tháng thành công!");
        jsonResponse.add("data", gson.toJsonTree(top5));
    }
}