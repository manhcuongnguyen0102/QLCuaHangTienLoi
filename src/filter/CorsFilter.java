package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Dấu /* nghĩa là bộ lọc này sẽ đứng gác ở mọi ngõ ngách của dự án
@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Không cần làm gì khi khởi tạo
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Cấp "giấy thông hành" cho mọi tên miền/cổng (bao gồm cả cổng 5500 của VS Code)
        response.setHeader("Access-Control-Allow-Origin", "*");

        // Cho phép các phương thức gọi API cơ bản
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        // Cấu hình thời gian nhớ thông hành (1 giờ)
        response.setHeader("Access-Control-Max-Age", "3600");

        // Cho phép gửi kèm các dữ liệu định dạng JSON
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");

        // Xử lý một request đặc biệt mang tên "OPTIONS" (Trình duyệt hay gửi cái này để thăm dò trước khi gửi cục JSON thật)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Cho phép đi tiếp vào Servlet
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        // Không cần làm gì khi hủy
    }
}