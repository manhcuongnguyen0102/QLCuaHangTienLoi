package Test;

import DAO.TaiKhoanDAO;
import model.TaiKhoan;

public class mainTK {
    public static void main(String[] args) {
        // 1. Khởi tạo đối tượng DAO
        TaiKhoanDAO dao = new TaiKhoanDAO();

        System.out.println("========== TEST CHỨC NĂNG ĐĂNG NHẬP ==========");

        // 2. Test Đăng nhập thành công (Dựa trên dữ liệu mẫu mình đã Insert ở SQL)
        // Giả sử tài khoản là 'admin' và mật khẩu là '123456'
        String userTest = "admin";
        String passTest = "123456";

        TaiKhoan tk = dao.kiemTraDangNhap(userTest, passTest);

        if (tk != null) {
            System.out.println(" Đăng nhập THÀNH CÔNG!");
            System.out.println("Thông tin tài khoản:");
            System.out.println("- Tên đăng nhập: " + tk.getTenDangNhap());
            System.out.println("- Vai trò: " + tk.getVaiTro());
            System.out.println("- Trạng thái: " + (tk.isTrangThai() ? "Đang hoạt động" : "Bị khóa"));
        } else {
            System.out.println(" Đăng nhập THẤT BẠI! (Sai user/pass hoặc tài khoản bị khóa)");
        }

        System.out.println("\n========== TEST CHỨC NĂNG ĐỔI MẬT KHẨU ==========");

        // 3. Test Đổi mật khẩu
        boolean isChanged = dao.doiMatKhau("admin", "123456", "newpass123");
        if (isChanged) {
            System.out.println("✅ Đổi mật khẩu thành công!");
            // Nhớ đổi lại mật khẩu cũ để lần sau test cho dễ nhé!
            dao.doiMatKhau("admin", "newpass123", "123456");
        } else {
            System.out.println(" Đổi mật khẩu thất bại!");
        }
    }
}