const API_HOADON = "http://localhost:8080/QuanLyCuaHangTienLoi/API/HoaDonAPI";

document.addEventListener("DOMContentLoaded", function() {
    loadDanhSachHoaDon();
});

// 1. GỌI API LẤY TẤT CẢ HÓA ĐƠN
function loadDanhSachHoaDon() {
    fetch(API_HOADON)
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                let html = "";
                jsonData.data.forEach(hd => {
                    // Xử lý null nếu khách vãng lai không có mã
                    let maKhach = hd.maKhachHang ? hd.maKhachHang : "Khách Lẻ";
                    let maNV = hd.maNhanVien ? hd.maNhanVien : "Web Order";

                    html += `
                        <tr>
                            <td><span class="badge bg-secondary">${hd.maHoaDon}</span></td>
                            <td>${hd.ngayLap}</td>
                            <td><strong>${maKhach}</strong></td>
                            <td>${maNV}</td>
                            <td class="text-danger fw-bold">${hd.tongTien.toLocaleString('vi-VN')} ₫</td>
                            <td>
                                <button class="btn btn-sm btn-outline-success" onclick="xemChiTiet('${hd.maHoaDon}')">
                                    <i class="fas fa-eye"></i> Xem Chi Tiết
                                </button>
                            </td>
                        </tr>
                    `;
                });
                document.getElementById("bangHoaDon").innerHTML = html;
            } else {
                alert("Lỗi tải danh sách hóa đơn: " + jsonData.message);
            }
        })
        .catch(err => console.error("Lỗi:", err));
}

// 2. GỌI API LẤY CHI TIẾT 1 HÓA ĐƠN KHI BẤM NÚT "XEM CHI TIẾT"
function xemChiTiet(maHoaDon) {
    fetch(`${API_HOADON}?maHoaDon=${maHoaDon}`)
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                let donHang = jsonData.data;
                let dsChiTiet = donHang.danhSachChiTiet;

                // Cập nhật thông tin Header của Modal
                document.getElementById("modalMaHD").innerText = donHang.maHoaDon;
                document.getElementById("modalTongTien").innerText = donHang.tongTien.toLocaleString('vi-VN') + " ₫";

                // Đổ danh sách sản phẩm vào bảng trong Modal
                let htmlChiTiet = "";
                dsChiTiet.forEach(item => {
                    let thanhTien = item.soLuong * item.giaBan;
                    htmlChiTiet += `
                        <tr>
                            <td><strong>${item.maSanPham}</strong></td>
                            <td>${item.soLuong}</td>
                            <td>${item.giaBan.toLocaleString('vi-VN')} ₫</td>
                            <td class="text-primary fw-bold">${thanhTien.toLocaleString('vi-VN')} ₫</td>
                        </tr>
                    `;
                });
                document.getElementById("bangChiTietHD").innerHTML = htmlChiTiet;

                // Hiển thị cái Modal (Popup) lên màn hình
                let chiTietModal = new bootstrap.Modal(document.getElementById('modalChiTiet'));
                chiTietModal.show();
            } else {
                alert("Không lấy được chi tiết: " + jsonData.message);
            }
        })
        .catch(err => console.error("Lỗi:", err));
}

function dangXuat() {
    window.location.href = "login.html";
}