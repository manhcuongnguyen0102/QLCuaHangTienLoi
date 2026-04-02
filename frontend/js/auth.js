document.addEventListener("DOMContentLoaded", function() {
    kiemTraDangNhap();
});

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
        // 2. LÀ KHÁCH HÀNG -> Đá sang trang mua sắm ngay lập tức
    if (user.vaiTro === "CUSTOMER") {
        if (!window.location.pathname.includes("trang-mua-sam.html") && !window.location.pathname.endsWith("/")) {
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

function phanQuyenMenu(vaiTro) {
    // Nếu là QUẢN LÝ -> Thấy tất cả mọi thứ
    if (vaiTro === "admin"||vaiTro === "QUAN_LY") return;

    // Nếu là NHÂN VIÊN -> Bạn có thể quyết định họ KHÔNG ĐƯỢC XEM cái gì ở đây
    if (vaiTro === "STAFF") {
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


function dangXuat() {
    if (confirm("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?")) {
        localStorage.removeItem("user_info"); // Xé thẻ căn cước
        window.location.href = "login.html";  // Trở về nơi bắt đầu
    }
}