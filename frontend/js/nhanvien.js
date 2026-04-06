const URL_NHANVIEN = "http://localhost:8080/QuanLyCuaHangTienLoi/API/NhanVienAPI";
let modalNV = null;
let isEditMode = false;
let allData = []; // Chứa toàn bộ dữ liệu gốc kéo từ DB về
let filteredData = []; // Chứa dữ liệu sau khi tìm kiếm
let currentPage = 1;
const itemsPerPage = 8; // Số dòng trên 1 trang (sếp tự chỉnh)
document.addEventListener("DOMContentLoaded", function() {
    // [BẢO MẬT CHUYÊN SÂU] Kểm tra xem có phải ADMIN/QUAN_LY không
    let userStr = localStorage.getItem("user_info");
    if (userStr) {
        let user = JSON.parse(userStr);
        if (user.vaiTro !== "ADMIN" && user.vaiTro !== "QUAN_LY") {
            alert("CẢNH BÁO: Khu vực này chỉ dành cho Ban Quản Lý!");
            window.location.href = "index.html"; // Đuổi về Dashboard ngay
            return;
        }
    }

    modalNV = new bootstrap.Modal(document.getElementById('modalNhanVien'));
    loadDanhSachNhanVien();
    searchNhanVien();
});

// =========================================
// 1. TẢI DANH SÁCH NHÂN VIÊN VÀ HIỂN THỊ (GET)
// =========================================

function loadDanhSachNhanVien() {
    fetch(URL_NHANVIEN)
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
    currentData.forEach(nv => {
        // Xử lý huy hiệu Trạng Thái
        let badgeTrangThai = nv.trangThai 
            ? '<span class="badge bg-success">Đang làm việc</span>' 
            : '<span class="badge bg-danger">Đã nghỉ việc</span>';

        // [ĐÃ SỬA] Xử lý nút Thao tác: Nếu đang làm thì hiện nút Đỏ, nếu nghỉ thì hiện nút Xanh
        let btnNghiViec = nv.trangThai 
            ? `<button class="btn btn-sm btn-danger ms-1" onclick="capNhatTrangThai('${nv.maNhanVien}', '${nv.tenNhanVien}', 0)" title="Cho nghỉ việc"><i class="fas fa-user-slash"></i></button>` 
            : `<button class="btn btn-sm btn-success ms-1" onclick="capNhatTrangThai('${nv.maNhanVien}', '${nv.tenNhanVien}', 1)" title="Khôi phục đi làm lại"><i class="fas fa-undo"></i></button>`;

        html += `
            <tr>
                <td><span class="badge bg-secondary">${nv.maNhanVien}</span></td>
                <td class="fw-bold text-primary">${nv.tenNhanVien}</td>
                <td>${nv.chucVu}</td>
                <td>${nv.soDienThoai}</td>
                <td><span class="badge bg-dark">${nv.tenDangNhap}</span></td>
                <td>${badgeTrangThai}</td> 
                <td>
                    <button class="btn btn-sm btn-warning" onclick='moFormSua(${JSON.stringify(nv)})' title="Sửa thông tin"><i class="fas fa-edit"></i></button>
                    ${btnNghiViec} 
                </td>
            </tr>
        `;
    });
    document.getElementById("bangNhanVien").innerHTML = html;
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
function searchNhanVien() {
    document.getElementById('searchInput').addEventListener('input', function(e) {
    const keyword = e.target.value.toLowerCase().trim();
    currentPage = 1; // Ép về trang 1 khi gõ tìm kiếm

    if (keyword === '') {
        filteredData = allData;
    } else {
        filteredData = allData.filter(item => {
            return item.tenNhanVien.toLowerCase().includes(keyword) || 
                   item.maNhanVien.toLowerCase().includes(keyword);
        });
    }
    
    renderTable(filteredData); // Vẽ lại bảng
});
}

// =========================================
// 2. MỞ FORM THÊM MỚI NHÂN VIÊN
// Chuyển flag isEditMode tắt (bằng false) và dọn Form
// =========================================
function moFormThem() {
    isEditMode = false;
    document.getElementById("formNhanVien").reset();
    document.getElementById("txtMaNV").value = "";
    document.getElementById("modalTitle").innerText = "Thêm Nhân Viên Mới";
    
    document.getElementById("txtUsername").disabled = false;
    document.getElementById("khungMatKhau").style.display = "block"; 
    
    modalNV.show();
}

// =========================================
// 3. MỞ FORM SỬA THÔNG TIN NHÂN VIÊN
// Đẩy (Inject) Object json của Nhân viên đè lên thẻ Input, ẩn ô mật khẩu
// =========================================
function moFormSua(nv) {
    isEditMode = true;
    document.getElementById("modalTitle").innerText = "Chỉnh Sửa Nhân Sự: " + nv.maNhanVien;
    
    document.getElementById("txtMaNV").value = nv.maNhanVien;
    document.getElementById("txtTenNV").value = nv.tenNhanVien;
    document.getElementById("selChucVu").value = nv.chucVu;
    document.getElementById("txtSDT").value = nv.soDienThoai;
    
    document.getElementById("txtUsername").value = nv.tenDangNhap;
    document.getElementById("txtUsername").disabled = true;
    document.getElementById("khungMatKhau").style.display = "none";
    
    modalNV.show();
}

// =========================================
// 4. LƯU DỮ LIỆU NHÂN VIÊN (GỌI ĐIỆN VỀ SERVER CHUNG CHO POST / PUT)
// =========================================
function luuNhanVien() {
    let data = {
        tenNhanVien: document.getElementById("txtTenNV").value.trim(),
        chucVu: document.getElementById("selChucVu").value,
        soDienThoai: document.getElementById("txtSDT").value.trim(),
        tenDangNhap: document.getElementById("txtUsername").value.trim()
    };

    if (!data.tenNhanVien || !data.soDienThoai || !data.tenDangNhap) {
        alert("Vui lòng điền đầy đủ các thông tin bắt buộc!");
        return;
    }

    let method = "POST";
    
    if (isEditMode) {
        method = "PUT";
        data.maNhanVien = document.getElementById("txtMaNV").value;
    } else {
        data.matKhau = document.getElementById("txtPassword").value.trim();
        if (!data.matKhau) {
            alert("Vui lòng cấp mật khẩu khởi tạo cho nhân viên mới!");
            return;
        }
    }

    fetch(URL_NHANVIEN, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(jsonData => {
        if (jsonData.status === "success") {
            alert(jsonData.message);
            modalNV.hide();
            loadDanhSachNhanVien();
        } else {
            alert("Lỗi: " + jsonData.message);
        }
    }).catch(err => alert("Lỗi kết nối máy chủ!"));
}

// =========================================
// 5. CẬP NHẬT TRẠNG THÁI TÀI KHOẢN (CHO NGHỈ VIỆC / KHÔI PHỤC ĐI LÀM)
// Tách hai request tùy thuộc giá trị trạng thái (0 -> Xóa logic [DELETE], 1 -> Khôi phục [PUT])
// =========================================
function capNhatTrangThai(maNV, tenNV, trangThaiMoi) {
    let hanhDong = trangThaiMoi === 1 ? "KHÔI PHỤC nhân viên này đi làm lại" : "cho nhân viên này NGHỈ VIỆC và khóa tài khoản";
    
    if (confirm(`Bạn có chắc chắn muốn ${hanhDong} [${tenNV}] không?`)) {
        
        // Cũ của sếp: Chỉ có DELETE để xóa. 
        // Mới: Nếu nghỉ việc (0) thì gọi DELETE. Nếu khôi phục (1) thì gọi PUT kèm tham số.
        let methodCall = trangThaiMoi === 0 ? "DELETE" : "PUT";
        let urlCall = trangThaiMoi === 0 
            ? `${URL_NHANVIEN}?maNV=${maNV}` 
            : `${URL_NHANVIEN}?maNV=${maNV}&action=restore`; // Gắn cờ restore để Java nhận diện

        fetch(urlCall, { method: methodCall })
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                alert(`Đã ${trangThaiMoi === 1 ? 'khôi phục' : 'khóa'} tài khoản của nhân viên ${tenNV}!`);
                loadDanhSachNhanVien();
            } else {
                alert("Lỗi: " + jsonData.message);
            }
        }).catch(err => alert("Lỗi kết nối máy chủ!"));
    }
}