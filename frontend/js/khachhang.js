const URL_KHACHHANG = "http://localhost:8080/QuanLyCuaHangTienLoi/API/KhachHangAPI";
// Gọi ké API Hóa đơn của bạn
const URL_HOADON = "http://localhost:8080/QuanLyCuaHangTienLoi/API/HoaDonAPI"; 

document.addEventListener("DOMContentLoaded", function() {
    loadDanhSachKhachHang();
});

// 1. TẢI DANH SÁCH KHÁCH HÀNG (Dùng KhachHangAPI)
function loadDanhSachKhachHang() {
    fetch(URL_KHACHHANG)
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                let html = "";
                jsonData.data.forEach(kh => {
                    html += `
                        <tr>
                            <td><span class="badge bg-secondary">${kh.maKhachHang}</span></td>
                            <td><strong>${kh.tenKhachHang}</strong></td>
                            <td>${kh.soDienThoai}</td>
                            <td>${kh.ngayDangKy}</td>
                            <td><span class="badge bg-warning text-dark px-3 rounded-pill">${kh.diemTichLuy} điểm</span></td>
                            <td>
                                <button class="btn btn-sm btn-outline-primary" onclick="xemLichSu('${kh.maKhachHang}', '${kh.tenKhachHang}')">
                                    <i class="fas fa-receipt"></i> Xem Lịch Sử Mua
                                </button>
                            </td>
                        </tr>
                    `;
                });
                document.getElementById("bangKhachHang").innerHTML = html;
            }
        }).catch(err => console.error(err));
}

// 2. XEM LỊCH SỬ MUA (Tái sử dụng HoaDonAPI)
function xemLichSu(maKH, tenKH) {
    // Gắn tên khách hàng lên tiêu đề Popup
    document.getElementById("lblTenKH").innerText = tenKH;

    // Gọi API hóa đơn, truyền tham số maKhachHang vào URL (Khớp với nhánh else-if của HoaDonAPI)
    fetch(`${URL_HOADON}?maKhachHang=${maKH}`)
        .then(res => res.json())
        .then(jsonData => {
            let html = "";
            if (jsonData.status === "success" && jsonData.data.length > 0) {
                jsonData.data.forEach(hd => {
                    html += `
                        <tr>
                            <td class="fw-bold">${hd.maHoaDon}</td>
                            <td>${hd.ngayLap}</td>
                            <td>${hd.maNhanVien ? hd.maNhanVien : 'Đặt Online'}</td>
                            <td class="text-danger fw-bold">${hd.tongTien.toLocaleString('vi-VN')} ₫</td>
                        </tr>
                    `;
                });
            } else {
                html = "<tr><td colspan='4' class='text-muted py-3'>Khách hàng này chưa mua đơn hàng nào.</td></tr>";
            }
            document.getElementById("bangLichSuMua").innerHTML = html;
            
            // Hiển thị modal
            new bootstrap.Modal(document.getElementById('modalLichSu')).show();
        })
        .catch(err => alert("Lỗi tải lịch sử!"));
}