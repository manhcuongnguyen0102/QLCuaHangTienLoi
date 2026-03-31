const API_SANPHAM = "http://localhost:8080/QuanLyCuaHangTienLoi/API/SanPhamAPI";
let modalSP = null;
let isEditMode = false; // Biến đánh dấu đang ở chế độ Thêm hay Sửa

document.addEventListener("DOMContentLoaded", function() {
    modalSP = new bootstrap.Modal(document.getElementById('modalSanPham'));
    loadDanhSachSanPham();
    loadDanhSachLoai();
    loadDanhSachNCC();
});

// LẤY DANH SÁCH (GET)
function loadDanhSachSanPham() {
    fetch(API_SANPHAM)
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                let html = "";
                jsonData.data.forEach(sp => {
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
            }
        }).catch(err => console.error(err));
}
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
// BẬT FORM THÊM MỚI
function moFormThem() {
    isEditMode = false;
    document.getElementById("formSanPham").reset();
    document.getElementById("txtMaSP").value = "";
    document.getElementById("modalTitle").innerText = "Thêm Sản Phẩm Mới";
    modalSP.show();
}

// BẬT FORM SỬA (Đổ dữ liệu cũ vào)
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

// LƯU SẢN PHẨM (GỌI POST HOẶC PUT)
function luuSanPham() {
    // 1. Gom dữ liệu từ Form
    let data = {
        tenSanPham: document.getElementById("txtTenSP").value,
        giaNhap: parseFloat(document.getElementById("txtGiaNhap").value),
        giaBan: parseFloat(document.getElementById("txtGiaBan").value),
        maLoai: document.getElementById("txtMaLoai").value,
        maNCC: document.getElementById("txtMaNCC").value,
        ngayHetHan: document.getElementById("txtNgayHetHan").value
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

// XÓA SẢN PHẨM (DELETE)
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