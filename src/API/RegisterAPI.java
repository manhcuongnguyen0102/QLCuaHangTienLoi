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
@WebServlet("/API/RegisterAPI")
public class RegisterAPI  extends HttpServlet{
    private TaiKhoanDAO tk= new TaiKhoanDAO();
    private Gson gson = new Gson();

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

        // 1. Thiết lập phản hồi trả về là JSON và hỗ trợ Tiếng Việt
        response.setHeader("Access-Control-Allow-Origin", "*");
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            // 2. Đọc dữ liệu JSON từ JavaScript gửi lên
            BufferedReader reader = request.getReader();

            // Map JSON vào class nội bộ RegisterRequest (ở cuối file)
            RegisterRequest regData = gson.fromJson(reader, RegisterRequest.class);

            // Kiểm tra dữ liệu đầu vào cơ bản
            if (regData == null || regData.getUsername() == null || regData.getTenKH() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Lỗi 400
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Dữ liệu gửi lên không hợp lệ!");
                out.print(jsonResponse.toString());
                return;
            }

            // 3. Xử lý logic nghiệp vụ

            // Bước A: Kiểm tra xem tên đăng nhập đã bị ai lấy chưa
            if (tk.kiemTraTonTai(regData.getUsername())) {
                response.setStatus(HttpServletResponse.SC_CONFLICT); // Lỗi 409
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Tên đăng nhập này đã tồn tại!");
            } else {
                // Bước B: Gọi hàm DAO "Transaction" để lưu vào 2 bảng cùng lúc
                boolean isSuccess = tk.dangKyKhachHang(
                        regData.getTenKH(),
                        regData.getSdt(),
                        regData.getUsername(),
                        regData.getPassword()
                );

                if (isSuccess) {
                    response.setStatus(HttpServletResponse.SC_OK); // Thành công 200
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Đăng ký thành công! Bạn có thể đăng nhập ngay.");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Lỗi 500
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Lỗi hệ thống khi lưu dữ liệu!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Lỗi Server: " + e.getMessage());
        } finally {
            // 4. Trả kết quả về cho trình duyệt
            out.print(jsonResponse.toString());
            out.flush();
            out.close();
        }
    }

    // Class nội bộ để Gson tự động ánh xạ dữ liệu từ JSON
    // Lưu ý: Tên biến phải khớp 100% với các key trong file register.js
    private class RegisterRequest {
        private String tenKH;
        private String sdt;
        private String username;
        private String password;

        public String getTenKH() { return tenKH; }
        public String getSdt() { return sdt; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
}

