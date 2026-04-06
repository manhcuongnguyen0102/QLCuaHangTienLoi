const URL_KHACHHANG = "http://localhost:8080/QuanLyCuaHangTienLoi/API/KhachHangAPI";
// Gọi ké API Hóa đơn của bạn
const URL_HOADON = "http://localhost:8080/QuanLyCuaHangTienLoi/API/HoaDonAPI"; 
let allData = []; // Chứa toàn bộ dữ liệu gốc kéo từ DB về
let filteredData = []; // Chứa dữ liệu sau khi tìm kiếm
let currentPage = 1;
const itemsPerPage = 8; // Số dòng trên 1 trang (sếp tự chỉnh)
document.addEventListener("DOMContentLoaded", function() {
    loadDanhSachKhachHang();
    searchKhachHang();
});

// =========================================
// 1. TẢI DANH SÁCH KHÁCH HÀNG (GET KhachHangAPI)
// =========================================

function loadDanhSachKhachHang() {
    fetch(URL_KHACHHANG)
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

// Hàm render dữ liệu ra bảng (Có phân trang)
function renderTable() {
    const totalPages = Math.ceil(filteredData.length / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const currentData = filteredData.slice(startIndex, endIndex);

    let html = "";
    currentData.forEach(kh => {
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
    renderPagination(totalPages);
}

// Hàm render nút phân trang
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

// Hàm đổi trang
function changePage(page) {
    currentPage = page;
    renderTable();
}

// Hàm tìm kiếm (gọi lại renderTable)
function searchKhachHang() {
    document.getElementById('searchInput').addEventListener('input', function(e) {
    const keyword = e.target.value.toLowerCase().trim();
    currentPage = 1; 

    if (keyword === '') {
        filteredData = allData;
    } else {
        filteredData = allData.filter(item => {
            return item.tenKhachHang.toLowerCase().includes(keyword) || 
                   item.maKhachHang.toLowerCase().includes(keyword);
        });
    }
    
    renderTable(filteredData); // Vẽ lại bảng
});
}
// =========================================
// 2. XEM LỊCH SỬ MUA HÀNG (Sửa dụng GET HoaDonAPI)
// Truyền mã Khách Hàng cụ thể vào API để rỉa mảng dữ liệu hóa đơn của KH đó
// =========================================
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