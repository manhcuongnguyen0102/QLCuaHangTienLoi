const API_BASE_URL = "http://localhost:8080/QuanLyCuaHangTienLoi/API/ThongKeAPI";
let doanhThuChartInstance = null;

document.addEventListener("DOMContentLoaded", function() {
    loadTongQuanHomNay();
    loadBieuDo(); // Mặc định 7 ngày
    loadTopSanPham();
    loadTopKhachHangVIP();
    loadSanPhamSapHet();
});

// 1. GỌI API TỔNG QUAN HÔM NAY (GET /tongquan)
function loadTongQuanHomNay() {
    fetch(`${API_BASE_URL}/tongquan`)
        .then(res => res.json())
        .then(jsonData => {
            if(jsonData.status === "success") {
                let d = jsonData.data;
                document.getElementById("doanhThuNay").innerText = d.doanhThu.toLocaleString('vi-VN') + " ₫";
                document.getElementById("loiNhuanNay").innerText = d.loiNhuan.toLocaleString('vi-VN') + " ₫";
                document.getElementById("soDonNay").innerText = d.soHoaDon;
                document.getElementById("soKhachNay").innerText = d.soKhachHang;
            }
        });
}

// 2. XỬ LÝ NÚT LỌC (Gọi 3 API: Biểu đồ, Tổng doanh thu, Tổng lợi nhuận)
function thucHienLoc() {
    let tuNgay = document.getElementById("tuNgay").value;
    let denNgay = document.getElementById("denNgay").value;
    if(!tuNgay || !denNgay) { alert("Vui lòng chọn Từ ngày và Đến ngày!"); return; }
    
    // Hiện ô kết quả lọc
    document.getElementById("ketQuaLoc").style.display = "flex";
    
    // Vẽ lại biểu đồ
    loadBieuDo(tuNgay, denNgay);

    // Lấy Tổng Doanh Thu (GET /doanhthu-tong)
    fetch(`${API_BASE_URL}/doanhthu-tong?tuNgay=${tuNgay}&denNgay=${denNgay}`)
        .then(res => res.json())
        .then(jsonData => {
            if(jsonData.status === "success") {
                document.getElementById("tongDoanhThuLoc").innerText = jsonData.data.tongDoanhThu.toLocaleString('vi-VN') + " ₫";
            }
        });

    // Lấy Tổng Lợi Nhuận (GET /loinhuan-tong)
    fetch(`${API_BASE_URL}/loinhuan-tong?tuNgay=${tuNgay}&denNgay=${denNgay}`)
        .then(res => res.json())
        .then(jsonData => {
            if(jsonData.status === "success") {
                document.getElementById("tongLoiNhuanLoc").innerText = jsonData.data.tongLoiNhuan.toLocaleString('vi-VN') + " ₫";
            }
        });
}

// 3. XÓA LỌC (Quay về mặc định)
function resetLoc() {
    document.getElementById("tuNgay").value = "";
    document.getElementById("denNgay").value = "";
    document.getElementById("ketQuaLoc").style.display = "none";
    loadBieuDo();
}

// 4. VẼ BIỂU ĐỒ (GET /doanhthu-bieudo)
function loadBieuDo(tuNgay = "", denNgay = "") {
    let url = `${API_BASE_URL}/doanhthu-bieudo`;
    if(tuNgay && denNgay) url += `?tuNgay=${tuNgay}&denNgay=${denNgay}`;

    fetch(url)
        .then(res => res.json())
        .then(jsonData => {
            if(jsonData.status === "success") {
                let dataArray = jsonData.data;
                let labels = dataArray.map(item => item.ngay);
                let revenues = dataArray.map(item => item.doanhThu);

                const ctx = document.getElementById('doanhThuChart').getContext('2d');
                if(doanhThuChartInstance != null) doanhThuChartInstance.destroy();

                doanhThuChartInstance = new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: 'Doanh thu (VNĐ)', data: revenues,
                            borderColor: '#2ecc71', backgroundColor: 'rgba(46, 204, 113, 0.2)',
                            borderWidth: 3, fill: true, tension: 0.3
                        }]
                    },
                    options: { 
                        responsive: true, 
                        maintainAspectRatio: false, 
                        scales: { 
                            y: { beginAtZero: true } 
                        },
                        layout: {
                            padding: 0
                        }
                    }

                });
            }
        });
}

// 5. TOP SẢN PHẨM (GET /top-sanpham)
function loadTopSanPham() {
    fetch(`${API_BASE_URL}/top-sanpham`).then(res => res.json()).then(jsonData => {
        if(jsonData.status === "success") {
            let html = "";
            jsonData.data.forEach(sp => {
                html += `<tr><td><strong>${sp.tenSanPham}</strong></td><td class="text-success">${sp.giaBan.toLocaleString('vi-VN')} ₫</td></tr>`;
            });
            document.getElementById("bangTopSanPham").innerHTML = html;
        }
    });
}

// 6. TOP KHÁCH HÀNG VIP (GET /top-khachhang)
function loadTopKhachHangVIP() {
    fetch(`${API_BASE_URL}/top-khachhang`).then(res => res.json()).then(jsonData => {
        if(jsonData.status === "success") {
            let html = "";
            jsonData.data.forEach(kh => {
                html += `<tr><td><strong>${kh.tenKhachHang}</strong><br><small class="text-muted">${kh.soDienThoai}</small></td>
                             <td class="text-primary fw-bold">${kh.tongChiTieu.toLocaleString('vi-VN')} ₫</td></tr>`;
            });
            document.getElementById("bangTopKhachHang").innerHTML = html;
        }
    });
}

// 7. CẢNH BÁO HẾT HÀNG (GET /sanpham-saphet)
function loadSanPhamSapHet() {
    fetch(`${API_BASE_URL}/sanpham-saphet`).then(res => res.json()).then(jsonData => {
        if(jsonData.status === "success") {
            let html = "";
            jsonData.data.forEach(sp => {
                html += `<tr><td>${sp.tenSanPham}</td><td><span class="badge bg-danger rounded-pill px-3">${sp.soLuongTon}</span></td></tr>`;
            });
            if(html === "") html = "<tr><td colspan='2' class='text-center text-success'>Đã nhập đủ hàng!</td></tr>";
            document.getElementById("bangSapHetHang").innerHTML = html;
        }
    });
}

function dangXuat() { window.location.href = "login.html"; }