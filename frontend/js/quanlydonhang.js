const API_HOADON = "http://localhost:8080/QuanLyCuaHangTienLoi/API/HoaDonAPI";
let allData = []; // Chứa toàn bộ dữ liệu gốc kéo từ DB về
let filteredData = []; // Chứa dữ liệu sau khi tìm kiếm
let currentPage = 1;
const itemsPerPage = 8; // Số dòng trên 1 trang (sếp tự chỉnh)
document.addEventListener("DOMContentLoaded", function() {
    loadDanhSachHoaDon();
    searchHoaDon();
});

// =========================================
// 1. GỌI API LẤY TẤT CẢ HÓA ĐƠN
// Trả về UI danh sách tổng lịch sử hóa đơn cửa hàng
// =========================================
function loadDanhSachHoaDon() {
    fetch(API_HOADON)
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                allData = jsonData.data; // 1. Lưu toàn bộ vào mảng gốc
                filteredData = [...allData]; // 2. Ban đầu dữ liệu lọc = dữ liệu gốc
                currentPage = 1; // 3. Reset về trang 1
                renderTable(); // 4. Gọi hàm render mới
            }
        }).catch(err => console.error(err));
}
function renderTable() {
    const totalPages = Math.ceil(filteredData.length / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const currentData = filteredData.slice(startIndex, endIndex);

    let html = "";
    currentData.forEach(hd => {
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
    renderPagination(totalPages);
}
function renderPagination(totalPages) {
    let paginationHtml = "";
    for (let i = 1; i <= totalPages; i++) {
        paginationHtml += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <button class="page-link" onclick="changePage(${i})">${i}</button>
            </li>
        `;
    }
    document.getElementById("pagination-container").innerHTML = paginationHtml;
}
function changePage(page) {
    currentPage = page;
    renderTable();
}
function searchHoaDon() {
   document.getElementById('searchInput').addEventListener('input', function(e) {
    const keyword = e.target.value.toLowerCase().trim();
    currentPage = 1; // Ép về trang 1 khi gõ tìm kiếm

    if (keyword === '') {
        filteredData = allData;
    } else {
        filteredData = allData.filter(item => {
             return item.maHoaDon.toLowerCase().includes(keyword);

           
        });
    }
    
    renderTable(filteredData); // Vẽ lại bảng với dữ liệu đã lọc
});
}

// =========================================
// 2. GỌI API LẤY CHI TIẾT 1 HÓA ĐƠN KHI BẤM NÚT "XEM CHI TIẾT"
// Show Popup chứa danh sách các line item đã mua
// =========================================
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