const API_KHACHHANG = 'http://localhost:8080/QuanLyCuaHangTienLoi/API/KhachHangAPI';
const API_HOADON = 'http://localhost:8080/QuanLyCuaHangTienLoi/API/HoaDonAPI';

document.addEventListener('DOMContentLoaded', async () => {
    const userStr = localStorage.getItem('user_info');
    if (!userStr) { window.location.href = "login.html"; return; }
    
    const user = JSON.parse(userStr);

    // 1. Lấy và hiển thị thông tin cá nhân
    await loadProfileData(user.tenDangNhap);

    // 2. Lấy và hiển thị lịch sử mua hàng
    if (user.maKhachHang) {
        await loadOrderHistory(user.maKhachHang);
    }
});

/**
 * Quét thông qua KhachHangAPI lấy full Data của tài khoản đang đăng nhập
 * và thay thế giá trị chèn vào các block HTML Profiling
 */
async function loadProfileData(username) {
    try {
        const response = await fetch(API_KHACHHANG);
        const result = await response.json();
        
        if (result.status === "success") {
            // Tìm đúng khách hàng đang đăng nhập trong danh sách
            const kh = result.data.find(k => k.tenDangNhap === username);
            if (kh) {
                document.getElementById('display-tenKhachHang').innerText = kh.tenKhachHang;
                document.getElementById('display-tenDangNhap').innerText = kh.tenDangNhap;
                document.getElementById('display-soDienThoai').innerText = kh.soDienThoai || 'Chưa cập nhật';
                document.getElementById('display-diemTichLuy').innerText = kh.diemTichLuy.toLocaleString();
            }
        }
    } catch (error) {
        console.error("Lỗi tải profile:", error);
    }
}

/**
 * Tái sử dụng HoaDonAPI (bản chất truyền maKhachHang vào url)
 * Lấy về cục Json của Hóa Đơn Khách này đã thanh toán. (Dùng map để lặp Array) 
 */
async function loadOrderHistory(maKH) {
    const listContainer = document.getElementById('order-history-list');
    try {
        const response = await fetch(`${API_HOADON}?maKhachHang=${maKH}`);
        const result = await response.json();

        if (result.status === "success" && result.data.length > 0) {
            const html = result.data.reverse().map(order => `
                <div class="order-card">
                    <div class="order-header">
                        <span><strong>Mã đơn: ${order.maHoaDon}</strong></span>
                        
                        <span style="color: #888;"><i class="far fa-clock"></i> ${order.ngayLap || 'Vừa xong'}</span>
                    </div>
                    <div class="order-body">
                        ${(order.danhSachChiTiet || []).map(item => `
                            <div class="order-item" style="display: flex; justify-content: space-between; padding: 5px 0;">
                                <span>${item.tenSanPham} (x${item.soLuong})</span>
                                
                                <span>
                                    <span style="color: #999; font-size: 0.85em; margin-right: 10px;">
                                        (${item.giaBan.toLocaleString()}đ/sp)
                                    </span>
                                    <strong>${(item.giaBan * item.soLuong).toLocaleString()} đ</strong>
                                </span>
                            </div>
                        `).join('')}
                    </div>
                    <div class="order-footer">
                        Tổng cộng: ${order.tongTien.toLocaleString()} đ
                    </div>
                </div>
            `).join('');
            listContainer.innerHTML = html;
        } else {
            listContainer.innerHTML = `<p style="text-align:center; color:#999;">Bạn chưa có đơn hàng nào.</p>`;
        }
    } catch (error) {
        listContainer.innerHTML = `<p style="color:red;">Không thể tải lịch sử đơn hàng.</p>`;
    }
}

function logout() {
    if(confirm("Xác nhận đăng xuất?")) {
        localStorage.clear();
        window.location.href = "login.html";
    }
}