const URL_PHIEUNHAP = "http://localhost:8080/QuanLyCuaHangTienLoi/API/PhieuNhapAPI";
const URL_SANPHAM = "http://localhost:8080/QuanLyCuaHangTienLoi/API/SanPhamAPI";
const URL_NCC = "http://localhost:8080/QuanLyCuaHangTienLoi/API/NhaCungCapAPI";

let danhSachChiTietTam = []; // "Giỏ hàng" chứa các món đang chờ nhập kho
let danhSachSanPhamGoc = []; // Lưu tạm danh sách SP để lấy tên và giá gốc hiển thị

document.addEventListener("DOMContentLoaded", function() {
    loadLichSuPhieuNhap();
});

// ==========================================
// PHẦN 1: HIỂN THỊ LỊCH SỬ & CHI TIẾT
// ==========================================

function loadLichSuPhieuNhap() {
    fetch(URL_PHIEUNHAP)
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                let html = "";
                jsonData.data.forEach(pn => {
                    html += `
                        <tr>
                            <td><span class="badge bg-secondary">${pn.maPhieuNhap}</span></td>
                            <td>${pn.ngayNhap}</td>
                            <td><strong>${pn.maNCC}</strong></td>
                            <td>${pn.maNhanVien}</td>
                            <td>
                                <button class="btn btn-sm btn-outline-info" onclick="xemChiTiet('${pn.maPhieuNhap}')">
                                    <i class="fas fa-eye"></i> Xem Chi Tiết
                                </button>
                            </td>
                        </tr>
                    `;
                });
                document.getElementById("bangPhieuNhap").innerHTML = html;
            }
        });
}

function xemChiTiet(maPhieuNhap) {
    fetch(`${URL_PHIEUNHAP}?maPhieuNhap=${maPhieuNhap}`)
        .then(res => res.json())
        .then(jsonData => {
            if (jsonData.status === "success") {
                let pn = jsonData.data;
                document.getElementById("lblMaPhieu").innerText = pn.maPhieuNhap;
                
                let htmlChiTiet = "";
                let tongTien = 0;
                pn.danhSachChiTiet.forEach(item => {
                    let thanhTien = item.soLuong * item.giaNhap;
                    tongTien += thanhTien;
                    htmlChiTiet += `
                        <tr>
                            <td><strong>${item.maSanPham}</strong></td>
                            <td>${item.soLuong}</td>
                            <td>${item.giaNhap.toLocaleString('vi-VN')} ₫</td>
                            <td class="text-primary fw-bold">${thanhTien.toLocaleString('vi-VN')} ₫</td>
                        </tr>
                    `;
                });
                document.getElementById("bangChiTietPhieu").innerHTML = htmlChiTiet;
                document.getElementById("lblTongTienPhieu").innerText = tongTien.toLocaleString('vi-VN') + " ₫";
                
                new bootstrap.Modal(document.getElementById('modalChiTiet')).show();
            }
        });
}

// ==========================================
// PHẦN 2: LOGIC TẠO PHIẾU NHẬP (TRÙM CUỐI)
// ==========================================

function moFormTaoPhieu() {
    // 1. Reset giỏ hàng và UI
    danhSachChiTietTam = [];
    renderGioHangNhap();
    document.getElementById("txtSoLuong").value = 1;
    document.getElementById("txtGiaNhap").value = "";

    // 2. Load danh sách NCC vào Dropdown
    fetch(URL_NCC).then(res => res.json()).then(json => {
        let html = "<option value=''>-- Chọn Nhà Cung Cấp --</option>";
        if(json.status === "success") {
            json.data.forEach(ncc => { html += `<option value="${ncc.maNCC}">${ncc.tenNCC}</option>`; });
        }
        document.getElementById("selNhaCungCap").innerHTML = html;
    });

    // 3. Load danh sách Sản Phẩm vào Dropdown
    fetch(URL_SANPHAM).then(res => res.json()).then(json => {
        let html = "<option value=''>-- Chọn Sản Phẩm Cần Nhập --</option>";
        if(json.status === "success") {
            danhSachSanPhamGoc = json.data; // Lưu lại để dùng sau
            json.data.forEach(sp => { html += `<option value="${sp.maSanPham}">${sp.tenSanPham} (Tồn: ${sp.soLuongTon})</option>`; });
        }
        document.getElementById("selSanPham").innerHTML = html;
    });

    new bootstrap.Modal(document.getElementById('modalTaoPhieu')).show();
}

// Hàm hỗ trợ: Tự động điền giá nhập cũ để Thủ kho đỡ phải gõ lại
function tuDongDienGiaNhap() {
    let maSP = document.getElementById("selSanPham").value;
    let sp = danhSachSanPhamGoc.find(x => x.maSanPham === maSP);
    if(sp) {
        document.getElementById("txtGiaNhap").value = sp.giaNhap; // Gợi ý giá nhập lần trước
    }
}

// Thêm sản phẩm vào mảng tạm thời (Giỏ hàng)
function themVaoPhieu() {
    let maSP = document.getElementById("selSanPham").value;
    let soLuong = parseInt(document.getElementById("txtSoLuong").value);
    let giaNhap = parseFloat(document.getElementById("txtGiaNhap").value);

    if(!maSP || !soLuong || isNaN(giaNhap)) {
        alert("Vui lòng chọn sản phẩm và điền đầy đủ số lượng, giá nhập!");
        return;
    }

    // Kiểm tra xem sản phẩm đã có trong giỏ chưa? Nếu có thì cộng dồn số lượng
    let tonTai = danhSachChiTietTam.find(item => item.maSanPham === maSP);
    if(tonTai) {
        tonTai.soLuong += soLuong;
        tonTai.giaNhap = giaNhap; // Lấy giá nhập mới nhất
    } else {
        danhSachChiTietTam.push({
            maSanPham: maSP,
            soLuong: soLuong,
            giaNhap: giaNhap
        });
    }

    renderGioHangNhap();
}

// Vẽ lại bảng Giỏ hàng
function renderGioHangNhap() {
    let html = "";
    if(danhSachChiTietTam.length === 0) {
        html = "<tr><td colspan='5' class='text-muted'>Chưa có sản phẩm nào được chọn</td></tr>";
    } else {
        danhSachChiTietTam.forEach((item, index) => {
            let thanhTien = item.soLuong * item.giaNhap;
            html += `
                <tr>
                    <td class="fw-bold">${item.maSanPham}</td>
                    <td>${item.soLuong}</td>
                    <td>${item.giaNhap.toLocaleString('vi-VN')} ₫</td>
                    <td class="text-primary fw-bold">${thanhTien.toLocaleString('vi-VN')} ₫</td>
                    <td><button class="btn btn-sm btn-danger" onclick="xoaKhoiPhieu(${index})"><i class="fas fa-times"></i></button></td>
                </tr>
            `;
        });
    }
    document.getElementById("bangGioHangNhap").innerHTML = html;
}

// Xóa 1 món khỏi giỏ
function xoaKhoiPhieu(index) {
    danhSachChiTietTam.splice(index, 1);
    renderGioHangNhap();
}

// BẤM NÚT LƯU PHIẾU NHẬP -> GỌI API (POST)
function luuPhieuNhap() {
    let maNCC = document.getElementById("selNhaCungCap").value;
    let maNV = "NV01"; // TODO: Lấy từ Session đăng nhập thực tế của tài khoản

    if(!maNCC) { alert("Vui lòng chọn Nhà Cung Cấp!"); return; }
    if(danhSachChiTietTam.length === 0) { alert("Phiếu nhập đang trống!"); return; }

    // Ráp thành cục JSON chuẩn với class PhieuNhapRequest của Java
    let requestData = {
        maNCC: maNCC,
        maNhanVien: maNV,
        chiTiet: danhSachChiTietTam
    };

    fetch(URL_PHIEUNHAP, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestData)
    })
    .then(res => res.json())
    .then(jsonData => {
        if(jsonData.status === "success") {
            alert("Đã cập nhật hàng hóa vào kho thành công!");
            bootstrap.Modal.getInstance(document.getElementById('modalTaoPhieu')).hide();
            loadLichSuPhieuNhap(); // Load lại bảng lịch sử
        } else {
            alert("Lỗi: " + jsonData.message);
        }
    })
    .catch(err => alert("Lỗi kết nối tới Server!"));
}