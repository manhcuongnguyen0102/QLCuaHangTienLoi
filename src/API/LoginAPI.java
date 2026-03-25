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

// Khai báo đường dẫn (Endpoint) cho API
@WebServlet("/API/LoginAPI")
public class LoginAPI extends HttpServlet {

    private TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private Gson gson = new Gson(); // Khởi tạo thư viện Gson

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Cấu hình CORS để cho phép thẻ HTML (Cross-Origin) gọi API được
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //Cấu hình để Server trả về dữ liệu chuẩn JSON kèm tiếng Việt
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*"); // Cho phép gọi API xuyên miền (CORS)

        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject(); // Dùng để xây dựng cục JSON trả về

        try {
            BufferedReader reader = request.getReader();

            // Dùng Gson "dịch" chuỗi JSON đó thành 1 đối tượng LoginRequest
            LoginRequest loginData = gson.fromJson(reader, LoginRequest.class);

            // Kiểm tra xem Frontend có gửi thiếu dữ liệu không
            if (loginData == null || loginData.getUsername() == null || loginData.getPassword() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Mã lỗi 400
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Thiếu tên đăng nhập hoặc mật khẩu!");
                out.print(jsonResponse.toString());
                return;
            }

            // 3. Chuyền dữ liệu xuống DAO để kiểm tra Database
            TaiKhoan tk = taiKhoanDAO.kiemTraDangNhap(loginData.getUsername(), loginData.getPassword());

            // 4. Xử lý kết quả và báo lại cho Frontend
            if (tk != null) {
                // Đăng nhập thành công
                response.setStatus(HttpServletResponse.SC_OK); // Mã 200 (Thành công)
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Đăng nhập thành công!");

                // Gói toàn bộ thông tin tài khoản (đã bị che mật khẩu) vào JSON để JS lưu lại phân quyền
                jsonResponse.add("data", gson.toJsonTree(tk));
            } else {
                // Đăng nhập thất bại (Sai pass, sai user, hoặc bị khóa)
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Mã 401 (Từ chối truy cập)
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Tên đăng nhập hoặc mật khẩu không chính xác, hoặc tài khoản đã bị khóa!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Bắt lỗi hệ thống (ví dụ: rớt mạng CSDL)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Mã 500
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Lỗi Server: " + e.getMessage());
        } finally {
            // Đẩy dữ liệu về trình duyệt và đóng luồng
            out.print(jsonResponse.toString());
            out.flush();
            out.close();
        }
    }


    // Class phụ trợ: Giúp Gson tự động map dữ liệu JSON từ JavaScript vào Java
    // Lưu ý: Tên biến phải khớp 100% với tên key (key-value) trong JSON gửi lên

    private class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
}