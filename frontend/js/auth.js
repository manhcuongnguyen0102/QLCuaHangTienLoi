document.addEventListener("DOMContentLoaded", function() {
    kiemTraDangNhap();
});

/**
 * Kiểm tra trạng thái đăng nhập và điều hướng user
 * - Nếu không có dữ liệu: Đuổi ra trang Login
 * - Nếu là KHACH_HANG: Khóa ở trang mua sắm/giỏ hàng
 * - Nếu là ADMIN/NHAN_VIEN: Hiển thị tên & Phân quyền Menu quản lý
 */
function kiemTraDangNhap() {
    let userStr = localStorage.getItem("user_info");

    // 1. CHƯA ĐĂNG NHẬP -> Mời ra trang Login
    if (!userStr) {
        // Chỉ chuyển về login nếu đang KHÔNG ở trang login
        if (!window.location.pathname.includes("login.html")) {
            window.location.href = "login.html";
        }
        return;
    }

    let user = JSON.parse(userStr);

    // 2. LÀ KHÁCH HÀNG -> Đá sang trang mua sắm ngay lập tức
    if (user.vaiTro === "KHACH_HANG") {
        let currentPath = window.location.pathname;
        if (!currentPath.includes("trang-mua-sam.html") && !currentPath.includes("gio-hang.html") && !currentPath.includes("thong-tin-tai-khoan.html") && !currentPath.endsWith("/")) {
            window.location.href = "trang-mua-sam.html"; 
        }
        return;
    }


    // 3. LÀ QUẢN LÝ / NHÂN VIÊN -> Cho phép ở lại và hiển thị Tên
    let lblTen = document.getElementById("lblTenNhanVien");
    if (lblTen) {
        lblTen.innerText = user.tenDangNhap; 
    }

    // 4. PHÂN QUYỀN MENU (Tùy chọn)
    phanQuyenMenu(user.vaiTro);
}

/**
 * Phân quyền thanh menu ở chế độ Nhân viên/Quản lý
 * - ADMIN / QUAN_LY: Cho phép thấy 100% chức năng
 * - NHAN_VIEN: Bị ẩn mục Bảng Điều Khiển (Thống kê) và bị điều hướng sang trang Đơn Hàng nếu vào trang Index
 */
function phanQuyenMenu(vaiTro) {
    // Nếu là QUẢN LÝ -> Thấy tất cả mọi thứ
    if (vaiTro === "ADMIN"||vaiTro === "QUAN_LY") return;

    // Nếu là NHÂN VIÊN -> Bạn có thể quyết định họ KHÔNG ĐƯỢC XEM cái gì ở đây
    if (vaiTro === "NHAN_VIEN") {
        // Ví dụ: Không cho nhân viên xem Bảng Điều Khiển (Doanh thu)
        let menuThongKe = document.getElementById("menu-thongke");
        if (menuThongKe) {
            menuThongKe.style.display = "none";
        }
        
        // Nếu Nhân viên vào thẳng trang index.html bằng cách gõ URL, đuổi sang trang Đơn hàng
        if (window.location.pathname.includes("index.html")) {
            window.location.href = "quanlydonhang.html";
        }
    }
}


/**
 * Xử lý sự kiện đăng xuất
 * Xóa trắng thẻ localStorage và đẩy user về lại trang đăng nhập (login.html)
 */
function dangXuat() {
    if (confirm("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?")) {
        localStorage.removeItem("user_info"); // Xé thẻ căn cước
        localStorage.removeItem("shopping_cart");
        window.location.href = "login.html";  // Trở về nơi bắt đầu
    }
}

/**
 * Móc API Khách Hàng để tìm xem tên đăng nhập này thuộc về profile nào
 * Nếu tìm thấy, ghim cứng mã khách hàng (maKhachHang) vào lại LocalStorage để tiện việc tạo hóa đơn.
 * @param {string} tenDangNhap - Tên tài khoản đang đăng nhập
 */
async function fetchThongTinKhachHang(tenDangNhap) {
    try {
        const response = await fetch('http://localhost:8080/QuanLyCuaHangTienLoi/API/KhachHangAPI');
        const data = await response.json();
        if (data.status === "success") {
            const khachHang = data.data.find(kh => kh.tenDangNhap === tenDangNhap);
            if (khachHang) {
                let user = JSON.parse(localStorage.getItem("user_info"));
                user.maKhachHang = khachHang.maKhachHang;
                user.tenKhachHang = khachHang.tenKhachHang;
                localStorage.setItem("user_info", JSON.stringify(user));
            }
        }
    } catch (error) {
        console.error("Lỗi lấy thông tin khách hàng:", error);
    }
}