const URL_NHANVIEN = "http://localhost:8080/QuanLyCuaHangTienLoi/API/NhanVienAPI";
let modalNV = null;
let isEditMode = false;

document.addEventListener("DOMContentLoaded", function() {
    // [BẢO MẬT CHUYÊN SÂU] Kểm tra xem có phải ADMIN/QUAN_LY không
    let userStr = localStorage.getItem("user_info");
    if (userStr) {
        let user = JSON.parse(userStr);
        if (user.vaiTro !== "admin" && user.vaiTro !== "QUAN_LY") {
            alert("CẢNH BÁO: Khu vực này chỉ dành cho Ban Quản Lý!");
            window.location.href = "index.html"; // Đuổi về Dashboard ngay
            return;
        }
    }

    modalNV = new bootstrap.Modal(document.getElementById('modalNhanVien'));
    loadDanhSachNhanVien();
});

// 1. TẢI DANH SÁCH (GET)
function loadDanhSachNhanVien() {
    fetch(URL_NHANVIEN)
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                let html = "";
                jsonData.data.forEach(nv => {
                    // Xử lý huy hiệu Trạng Thái
                    let badgeTrangThai = nv.trangThai 
                        ? '<span class="badge bg-success">Đang làm việc</span>' 
                        : '<span class="badge bg-danger">Đã nghỉ việc</span>';

                    // Ẩn nút "Nghỉ việc" (màu đỏ) nếu người ta đã nghỉ rồi
                    let btnNghiViec = nv.trangThai 
                        ? `<button class="btn btn-sm btn-danger ms-1" onclick="xoaNhanVien('${nv.maNhanVien}', '${nv.tenNhanVien}')" title="Cho nghỉ việc"><i class="fas fa-user-slash"></i></button>` 
                        : '';

                    html += `
                        <tr>
                            <td><span class="badge bg-secondary">${nv.maNhanVien}</span></td>
                            <td class="fw-bold text-primary">${nv.tenNhanVien}</td>
                            <td>${nv.chucVu}</td>
                            <td>${nv.soDienThoai}</td>
                            <td><span class="badge bg-dark">${nv.tenDangNhap}</span></td>
                            <td>${badgeTrangThai}</td> <td>
                                <button class="btn btn-sm btn-warning" onclick='moFormSua(${JSON.stringify(nv)})' title="Sửa thông tin"><i class="fas fa-edit"></i></button>
                                ${btnNghiViec} </td>
                        </tr>
                    `;
                });
                        
                    
                document.getElementById("bangNhanVien").innerHTML = html;
            }
        }).catch(err => console.error(err));
}

// 2. MỞ FORM THÊM MỚI
function moFormThem() {
    isEditMode = false;
    document.getElementById("formNhanVien").reset();
    document.getElementById("txtMaNV").value = "";
    document.getElementById("modalTitle").innerText = "Thêm Nhân Viên Mới";
    
    // Mở khóa ô Username và hiển thị ô Mật khẩu
    document.getElementById("txtUsername").disabled = false;
    document.getElementById("khungMatKhau").style.display = "block"; 
    
    modalNV.show();
}

// 3. MỞ FORM SỬA THÔNG TIN
function moFormSua(nv) {
    isEditMode = true;
    document.getElementById("modalTitle").innerText = "Chỉnh Sửa Nhân Sự: " + nv.maNhanVien;
    
    document.getElementById("txtMaNV").value = nv.maNhanVien;
    document.getElementById("txtTenNV").value = nv.tenNhanVien;
    document.getElementById("selChucVu").value = nv.chucVu;
    document.getElementById("txtSDT").value = nv.soDienThoai;
    
    // Khóa ô Username (Không cho đổi tên đăng nhập) và Ẩn ô Mật khẩu
    document.getElementById("txtUsername").value = nv.tenDangNhap;
    document.getElementById("txtUsername").disabled = true;
    document.getElementById("khungMatKhau").style.display = "none";
    
    modalNV.show();
}

// 4. LƯU DỮ LIỆU (POST / PUT)
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
        // Nếu là Thêm Mới, bắt buộc phải có mật khẩu
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

// 5. XÓA TÀI KHOẢN (ĐÁNH DẤU NGHỈ VIỆC - DELETE)
function xoaNhanVien(maNV, tenNV) {
    if (confirm(`Bạn có chắc chắn muốn cho nhân viên [${tenNV}] nghỉ việc và khóa tài khoản không?`)) {
        
        // API DELETE của bạn dùng Query Parameter (?maNV=...)
        fetch(`${URL_NHANVIEN}?maNV=${maNV}`, { method: "DELETE" })
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                alert(`Đã khóa tài khoản của nhân viên ${tenNV}!`);
                loadDanhSachNhanVien();
            } else {
                alert("Lỗi: " + jsonData.message);
            }
        }).catch(err => alert("Lỗi kết nối máy chủ!"));
    }
}