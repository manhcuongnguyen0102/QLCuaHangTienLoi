package API;
import DAO.TaiKhoanDAO;
import model.TaiKhoan;
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
@WebServlet("/API/ForgotPasswordAPI")
public class ForgotPasswordAPI extends HttpServlet {
    private TaiKhoanDAO dao = new TaiKhoanDAO();
    private Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Cấu hình CORS để cho phép thẻ HTML (Cross-Origin) gọi API được
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
    // Xử lý kiểm tra SĐT (Bước 1)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        String sdt = request.getParameter("sdt");
        boolean exists = dao.kiemTraSDTExist(sdt);

        response.setContentType("application/json");
        JsonObject jsonRes = new JsonObject();
        jsonRes.addProperty("exists", exists);
        response.getWriter().print(jsonRes.toString());
    }

    // Xử lý đổi mật khẩu (Bước 2)
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        BufferedReader reader = request.getReader();
        JsonObject data = gson.fromJson(reader, JsonObject.class);

        String sdt = data.get("sdt").getAsString();
        String newPass = data.get("newPass").getAsString();

        boolean success = dao.doiMatKhauBangSDT(sdt, newPass);

        JsonObject jsonRes = new JsonObject();
        jsonRes.addProperty("message", success ? "Đổi mật khẩu thành công!" : "Lỗi hệ thống!");
        response.getWriter().print(jsonRes.toString());
    }
}

