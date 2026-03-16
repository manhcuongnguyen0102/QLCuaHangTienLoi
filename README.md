#  Hệ Thống Quản Lý Cửa Hàng Tiện Lợi 

Dự án bài tập lớn môn Java Web - Xây dựng hệ thống quản lý bán hàng toàn diện, áp dụng kiến trúc phần mềm hiện đại.

##  1. Kiến trúc Hệ Thống 
Thay vì sử dụng mô hình Monolithic (nguyên khối) truyền thống với JSP, dự án này được thiết kế theo mô hình **Decoupled (Phân tách Frontend và Backend)** giao tiếp qua **RESTful API**.
* Frontend:** Sử dụng HTML, CSS, và Vanilla JavaScript (Fetch API). Quản lý giao diện và trải nghiệm người dùng, chạy độc lập 
* Backend:** Xây dựng bằng Java Web (Servlet), chạy trên nền tảng Apache Tomcat (cổng 8080). Đóng vai trò là một API Server, xử lý logic nghiệp vụ, tương tác với SQL Server và trả về dữ liệu thuần định dạng `JSON`.

## 🛠 2. Công Nghệ & Thư Viện Nền Tảng (Core Technologies)
Để đảm bảo hệ thống API hoạt động trơn tru và bảo mật, dự án áp dụng các thư viện cốt lõi sau:

1. `javax.servlet-api` (Thư viện Web Core)
  Cung cấp môi trường mạng cho Java. Giúp Java Core (vốn chỉ chạy trên Console) có khả năng lắng nghe các HTTP Request từ Internet và trả lời qua HTTP Response.

2. `Gson` (Google JSON Library)**
   * Vai trò: "Phiên dịch viên" của hệ thống. Vì Frontend chỉ hiểu JSON còn Backend chỉ hiểu Java Object, Gson sẽ tự động map (chuyển đổi) dữ liệu qua lại giữa hai định dạng này một cách siêu tốc.

3. `CorsFilter` (Bộ lọc cấu hình bảo mật)**
   * Vai trò: Giải quyết bài toán Same-Origin Policy của trình duyệt. CorsFilter đóng vai trò như một "trạm kiểm soát biên giới", cấp phép (Allow-Origin) cho phép các luồng gọi dữ liệu chéo từ cổng của Frontend (5500) tiến vào Backend (8080) mà không bị trình duyệt block lỗi CORS.

