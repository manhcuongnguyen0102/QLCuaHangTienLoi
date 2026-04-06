const API_SANPHAM = "http://localhost:8080/QuanLyCuaHangTienLoi/API/SanPhamAPI";
let modalSP = null;
let isEditMode = false; // Biến đánh dấu đang ở chế độ Thêm hay Sửa
let allData = []; // Chứa toàn bộ dữ liệu gốc kéo từ DB về
let filteredData = []; // Chứa dữ liệu sau khi tìm kiếm
let currentPage = 1;
const itemsPerPage = 8; // Số dòng trên 1 trang (sếp tự chỉnh)
document.addEventListener("DOMContentLoaded", function() {
    modalSP = new bootstrap.Modal(document.getElementById('modalSanPham'));
    loadDanhSachSanPham();
    loadDanhSachLoai();
    loadDanhSachNCC();
    searchSanPham();
});

// =========================================
// 1. LẤY MẢNG SẢN PHẨM TỪ BE VÀ TRẢ RA GIAO DIỆN BẢNG
// Sử dụng Fetch để query API Get và render chuỗi HTML đắp vào thẻ TBody
// =========================================

function loadDanhSachSanPham() {
    fetch(API_SANPHAM)
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
    currentData.forEach(sp => {
        html += `
            <tr>
                <td><span class="badge bg-secondary">${sp.maSanPham}</span></td>
                <td><strong>${sp.tenSanPham}</strong></td>
                <td><small class="text-muted">${sp.maLoai} / ${sp.maNCC}</small></td>
                <td>${sp.giaNhap.toLocaleString('vi-VN')} ₫</td>
                <td class="text-success fw-bold">${sp.giaBan.toLocaleString('vi-VN')} ₫</td>
                <td><span class="badge ${sp.soLuongTon < 10 ? 'bg-danger' : 'bg-info'}">${sp.soLuongTon}</span></td>
                <td>
                    <button class="btn btn-sm btn-warning" onclick='moFormSua(${JSON.stringify(sp)})'><i class="fas fa-edit"></i></button>
                    <button class="btn btn-sm btn-danger" onclick="xoaSanPham('${sp.maSanPham}')"><i class="fas fa-trash"></i></button>
                </td>
            </tr>
        `;
    });
    document.getElementById("bangSanPham").innerHTML = html;
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
function searchSanPham() {
document.getElementById('searchInput').addEventListener('input', function(e) {
    const keyword = e.target.value.toLowerCase().trim();
    currentPage = 1; // Ép về trang 1 khi gõ tìm kiếm

    if (keyword === '') {
        filteredData = allData;
    } else {
        filteredData = allData.filter(item => {
            return item.tenSanPham.toLowerCase().includes(keyword) || 
                   item.maSanPham.toLowerCase().includes(keyword);
        });
    }
    
    renderTable(filteredData); // Vẽ lại bảng với dữ liệu đã lọc
});
}
/**
 * Chọc API lấy riêng bảng Loại KH và đổ thành mảng Select Option vào giao diện Modal Sửa/Thêm Phẩm
 */
function loadDanhSachLoai() {
    fetch("http://localhost:8080/QuanLyCuaHangTienLoi/API/LoaiSanPhamAPI")
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                let options = "<option value=''>-- Chọn Loại --</option>";
                jsonData.data.forEach(loai => {
                    options += `<option value="${loai.maLoai}">${loai.tenLoai}</option>`;
                });
                document.getElementById("txtMaLoai").innerHTML = options;
            }
        });
}

/**
 * Chọc API lấy Danh Sách Nhà Cung Cấp, đổ dứ liệu làm các option <select> trong Modal SP
 */
function loadDanhSachNCC() {
    fetch("http://localhost:8080/QuanLyCuaHangTienLoi/API/NhaCungCapAPI")
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                let options = "<option value=''>-- Chọn NCC --</option>";
                jsonData.data.forEach(ncc => {
                    options += `<option value="${ncc.maNCC}">${ncc.tenNCC}</option>`;
                });
                document.getElementById("txtMaNCC").innerHTML = options;
            }
        });
}
// =========================================
// 2. MỞ KHUNG POPUP ĐỂ THÊM MỚI SẢN PHẨM
// Chuyển flag isEditMode sang False, dọn sạch (reset) form cũ trước khi mở màn hình.
// =========================================
function moFormThem() {
    isEditMode = false;
    document.getElementById("formSanPham").reset();
    document.getElementById("txtMaSP").value = "";
    document.getElementById("modalTitle").innerText = "Thêm Sản Phẩm Mới";
    modalSP.show();
}

// =========================================
// 3. MỞ KHUNG POPUP ĐỂ UPDATE (CHỈNH SỬA) LIỆU SẢN PHẨM ĐÃ CÓ
// Chuyển flag isEditMode sang True, bắn ngược từng data Object cũ vào các thẻ input hiện ra.
// =========================================
function moFormSua(sp) {
    isEditMode = true;
    document.getElementById("modalTitle").innerText = "Chỉnh Sửa Sản Phẩm: " + sp.maSanPham;
    document.getElementById("txtMaSP").value = sp.maSanPham;
    document.getElementById("txtTenSP").value = sp.tenSanPham;
    document.getElementById("txtGiaNhap").value = sp.giaNhap;
    document.getElementById("txtGiaBan").value = sp.giaBan;
    document.getElementById("txtMaLoai").value = sp.maLoai;
    document.getElementById("txtMaNCC").value = sp.maNCC;
    document.getElementById("txtNgayHetHan").value = sp.ngayHetHan ? sp.ngayHetHan : "";
    modalSP.show();
}

// =========================================
// 4. LƯU (SAVE) THAO TÁC CỦA NGƯỜI DÙNG: CÓ THỂ LÀ THÊM MỚI HOẶC SỬA.
// Hàm này quét đọc dữ liệu hiện hành trên HTML DOM, gán Method (POST/PUT) tùy thuộc vào flag isEditMode.
// =========================================
function luuSanPham() {
    // 1. Gom dữ liệu từ Form
    let giaTriNgay = document.getElementById("txtNgayHetHan").value;
    let data = {
        tenSanPham: document.getElementById("txtTenSP").value,
        giaNhap: parseFloat(document.getElementById("txtGiaNhap").value),
        giaBan: parseFloat(document.getElementById("txtGiaBan").value),
        maLoai: document.getElementById("txtMaLoai").value,
        maNCC: document.getElementById("txtMaNCC").value,
        ngayHetHan: giaTriNgay ? giaTriNgay : null
    };

    // 2. Tùy chế độ mà cấu hình Fetch
    let method = "POST";
    if (isEditMode) {
        method = "PUT";
        data.maSanPham = document.getElementById("txtMaSP").value; // Nếu sửa thì phải ném cả mã SP lên
    }

    // 3. Gửi Request
    fetch(API_SANPHAM, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(jsonData => {
        if (jsonData.status === "success") {
            alert(jsonData.message);
            modalSP.hide();
            loadDanhSachSanPham(); // Load lại bảng
        } else {
            alert("Lỗi: " + jsonData.message);
        }
    }).catch(err => alert("Lỗi kết nối Server!"));
}

// =========================================
// 5. CÓ CHẮC CHẮN MUỐN XÓA SẢN PHẨM NÀNH KHÔNG (Hủy Sản Phẩm)
// Yêu cầu DELETE bằng AJAX thông qua maSanPham, load lại danh sách sau khi hoàn tất.
// =========================================
function xoaSanPham(maSP) {
    if(confirm("Bạn có chắc chắn muốn xóa sản phẩm " + maSP + " không?")) {
        fetch(`${API_SANPHAM}?maSanPham=${maSP}`, { method: "DELETE" })
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                loadDanhSachSanPham(); // Xóa xong tự load lại bảng
            } else {
                alert(jsonData.message); // Thông báo lỗi khóa ngoại
            }
        });
    }
}