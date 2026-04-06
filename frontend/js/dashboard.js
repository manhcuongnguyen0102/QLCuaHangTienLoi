const API_BASE_URL = "http://localhost:8080/QuanLyCuaHangTienLoi/API/ThongKeAPI";
let doanhThuChartInstance = null;

document.addEventListener("DOMContentLoaded", function() {
    loadTongQuanHomNay();
    loadBieuDo(); // Mặc định 7 ngày
    loadTopSanPham();
    loadTopKhachHangVIP();
    loadSanPhamSapHet();
});

// =========================================
// 1. GỌI API TỔNG QUAN HÔM NAY (GET /tongquan)
// Hiển thị các con số tổng Doanh Thu, Lợi Nhuận, Số Đơn, Khách VIP của ngày hôm nay lên các thẻ.
// =========================================
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

// =========================================
// 2. XỬ LÝ NÚT LỌC DỮ LIỆU TÙY CHỈNH THEO KHOẢNG THỜI GIAN
// Kích hoạt khi ấn nút "Lọc", gọi đồng thời 3 API: Biểu đồ, Tổng doanh thu, Tổng lợi nhuận để update thẻ filter.
// =========================================
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

// =========================================
// 3. HỦY KẾT QUẢ VÀ TRỞ BỘ LỌC VỀ BAN ĐẦU
// Đặt lại các input Date, ẩn panel kết quả và vẽ lại biểu đồ của 7 ngày qua
// =========================================
function resetLoc() {
    document.getElementById("tuNgay").value = "";
    document.getElementById("denNgay").value = "";
    document.getElementById("ketQuaLoc").style.display = "none";
    loadBieuDo();
}

// =========================================
// 4. VẼ BIỂU ĐỒ DOANH THU TỪNG NGÀY BẰNG CHART.JS
// Tải mảng dữ liệu JSON về doanh thu và setup một Line Chart mới phủ lên giao diện.
// =========================================
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

// =========================================
// 5. CẬP NHẬT BẢNG TOP SẢN PHẨM KHÁCH MUA NHIỀU NHẤT
// =========================================
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

// =========================================
// 6. CẬP NHẬT BẢNG TOP KHÁCH HÀNG MUA TÍCH CỰC
// =========================================
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

// =========================================
// 7. CẢNH BÁO SẢN PHẨM SẮP HẾT HÀNG TRONG KHO (SL < 10)
// Lấy danh sách hàng sắp hết để chèn vào panel cảnh báo góc phải
// =========================================
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