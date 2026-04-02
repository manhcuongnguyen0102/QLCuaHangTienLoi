let cart = [];
let currentUser = null;
let khachHangInfo = null;
let usePoints = false;
let discountAmount = 0;
let pointsUsed = 0;

const API_SANPHAM = 'http://localhost:8080/API/SanPhamAPI';
const API_KHACHHANG = 'http://localhost:8080/API/KhachHangAPI';
const API_HOADON = 'http://localhost:8080/API/HoaDonAPI';

const POINT_EARN_RATE = 10000;   // 10.000đ = 1 điểm tích lũy
const POINT_REDEEM_RATE = 100;   // 1 điểm = 100đ giảm giá

document.addEventListener('DOMContentLoaded', async () => {
    if (!await checkLoginAndPermission()) return;
    await fetchKhachHangInfo();
    await loadCartFromLocalStorage();  // load và cập nhật tồn kho từ DB
    renderCart();
    updateCartCountOnHeader();
    setupUserMenu();
});

async function checkLoginAndPermission() {
    let userStr = localStorage.getItem("user_info");
    if (!userStr) {
        alert("Vui lòng đăng nhập để xem giỏ hàng!");
        window.location.href = "login.html";
        return false;
    }
    currentUser = JSON.parse(userStr);
    if (currentUser.vaiTro !== "CUSTOMER") {
        alert("Tài khoản không có quyền mua hàng!");
        window.location.href = "index.html";
        return false;
    }
    let userNameSpan = document.getElementById("user-name-display");
    if (userNameSpan) userNameSpan.innerText = currentUser.tenDangNhap;
    return true;
}

async function fetchKhachHangInfo() {
    try {
        const response = await fetch(API_KHACHHANG);
        const data = await response.json();
        if (data.status === "success") {
            const kh = data.data.find(k => k.tenDangNhap === currentUser.tenDangNhap);
            if (kh) {
                khachHangInfo = kh;
                if (!currentUser.maKhachHang) {
                    currentUser.maKhachHang = kh.maKhachHang;
                    currentUser.tenKhachHang = kh.tenKhachHang;
                    localStorage.setItem("user_info", JSON.stringify(currentUser));
                }
            }
        }
    } catch (error) {
        console.error("Lỗi lấy thông tin khách hàng:", error);
    }
}

// Cập nhật tồn kho thực tế từ database cho tất cả sản phẩm trong giỏ (có log chi tiết)
async function updateAllStockFromAPI() {
    for (let i = 0; i < cart.length; i++) {
        const item = cart[i];
        try {
            const response = await fetch(`${API_SANPHAM}?maSanPham=${item.maSanPham}`);
            const result = await response.json();
            console.log(`📦 Kiểm tra tồn kho ${item.maSanPham}:`, result);
            if (result.status === "success" && Array.isArray(result.data)) {
                // Tìm sản phẩm có mã khớp trong mảng
                const found = result.data.find(p => p.maSanPham === item.maSanPham);
                if (found && typeof found.soLuongTon === 'number') {
                    const newStock = found.soLuongTon;
                    if (item.soLuongTon !== newStock) {
                        console.log(`✅ Cập nhật ${item.maSanPham}: ${item.soLuongTon} → ${newStock}`);
                        item.soLuongTon = newStock;
                        if (item.soLuong > newStock) {
                            console.log(`⚠️ Giảm số lượng ${item.maSanPham} từ ${item.soLuong} xuống ${newStock}`);
                            item.soLuong = newStock;
                        }
                    }
                } else {
                    console.warn(`⚠️ Không tìm thấy sản phẩm ${item.maSanPham} trong mảng trả về`);
                }
            } else {
                console.warn(`⚠️ Dữ liệu tồn kho không hợp lệ cho ${item.maSanPham}:`, result);
            }
        } catch (error) {
            console.error(`❌ Lỗi fetch tồn kho ${item.maSanPham}:`, error);
        }
    }
    saveCartToLocalStorage();
}

async function loadCartFromLocalStorage() {
    let stored = localStorage.getItem("shopping_cart");
    if (stored) {
        cart = JSON.parse(stored);
        // Chuẩn hóa dữ liệu, đảm bảo soLuongTon là số
        cart = cart.map(item => ({
            maSanPham: item.maSanPham,
            tenSanPham: item.tenSanPham,
            giaBan: item.giaBan,
            soLuong: item.soLuong || 1,
            soLuongTon: (item.soLuongTon !== undefined && item.soLuongTon !== null && !isNaN(item.soLuongTon)) ? item.soLuongTon : 999,
            selected: item.selected !== false
        }));
        console.log("🛒 Giỏ hàng trước khi cập nhật tồn kho:", JSON.parse(JSON.stringify(cart)));
        await updateAllStockFromAPI(); // Cập nhật tồn kho mới nhất từ DB
        console.log("🛒 Giỏ hàng sau khi cập nhật tồn kho:", JSON.parse(JSON.stringify(cart)));
    } else {
        cart = [];
    }
}

function saveCartToLocalStorage() {
    localStorage.setItem("shopping_cart", JSON.stringify(cart));
    updateCartCountOnHeader();
}

function updateCartCountOnHeader() {
    const distinctCount = cart.length;
    document.querySelectorAll('.cart-count').forEach(span => span.innerText = distinctCount);
    let countSpan = document.getElementById('cart-item-count');
    if (countSpan) countSpan.innerText = `(${distinctCount} sản phẩm)`;
}

function getSelectedTotal() {
    return cart.reduce((sum, item) => {
        if (item.selected !== false) return sum + (item.giaBan * item.soLuong);
        return sum;
    }, 0);
}

/**
 * Kiểm tra tồn kho của các sản phẩm được chọn
 * Trả về: { isValid: boolean, message: string, updatedCart: array }
 */
async function validateAndFixStockBeforeCheckout() {
    const selectedIndices = [];
    cart.forEach((item, idx) => {
        if (item.selected === true) selectedIndices.push(idx);
    });
    if (selectedIndices.length === 0) return { isValid: false, message: "Chưa chọn sản phẩm nào!", updatedCart: cart };

    let hasError = false;
    let errorMessage = "";

    for (let idx of selectedIndices) {
        const item = cart[idx];
        try {
            const response = await fetch(`${API_SANPHAM}?maSanPham=${item.maSanPham}`);
            const data = await response.json();
            if (data.status === "success" && Array.isArray(data.data)) {
                const found = data.data.find(p => p.maSanPham === item.maSanPham);
                if (found && typeof found.soLuongTon === 'number') {
                    const currentStock = found.soLuongTon;
                    if (item.soLuong > currentStock) {
                        hasError = true;
                        errorMessage += `\n- ${item.tenSanPham}: yêu cầu ${item.soLuong}, chỉ còn ${currentStock}. Số lượng đã được điều chỉnh.`;
                        cart[idx].soLuong = currentStock;
                        cart[idx].soLuongTon = currentStock;
                    } else {
                        cart[idx].soLuongTon = currentStock;
                    }
                } else {
                    hasError = true;
                    errorMessage += `\n- Không tìm thấy thông tin tồn kho của ${item.tenSanPham}.`;
                }
            } else {
                hasError = true;
                errorMessage += `\n- Không thể kiểm tra tồn kho của ${item.tenSanPham}.`;
            }
        } catch (err) {
            console.error(`Lỗi kiểm tra tồn kho cho ${item.maSanPham}:`, err);
            hasError = true;
            errorMessage += `\n- Lỗi kết nối khi kiểm tra ${item.tenSanPham}.`;
        }
    }

    if (hasError) {
        saveCartToLocalStorage();
        return { isValid: false, message: errorMessage, updatedCart: cart };
    }
    return { isValid: true, message: "", updatedCart: cart };
}

function calculateDiscount(selectedTotal, availablePoints) {
    const maxDiscountPercent = 0.5;
    let maxDiscountAmount = selectedTotal * maxDiscountPercent;
    let maxDiscountFromPoints = availablePoints * POINT_REDEEM_RATE;
    let discount = Math.min(maxDiscountFromPoints, maxDiscountAmount, selectedTotal);
    let pointsUsed = Math.floor(discount / POINT_REDEEM_RATE);
    return { discount, pointsUsed, finalTotal: selectedTotal - discount };
}

function deleteSelected() {
    const selectedCount = cart.filter(item => item.selected === true).length;
    if (selectedCount === 0) {
        alert("Vui lòng chọn sản phẩm cần xóa!");
        return;
    }
    if (confirm(`Bạn có chắc muốn xóa ${selectedCount} sản phẩm đã chọn?`)) {
        cart = cart.filter(item => item.selected !== true);
        saveCartToLocalStorage();
        renderCart();
    }
}

function renderCart() {
    const container = document.getElementById('cart-content');
    if (!container) return;

    if (cart.length === 0) {
        container.innerHTML = `<div class="empty-cart">
            <i class="fas fa-shopping-basket"></i>
            <p>Giỏ hàng trống. Hãy <a href="mua-sap.html">mua sắm ngay</a>!</p>
        </div>`;
        return;
    }

    const selectedTotal = getSelectedTotal();
    let discount = 0, finalTotal = selectedTotal;
    let availablePoints = khachHangInfo ? khachHangInfo.diemTichLuy : 0;

    if (usePoints && availablePoints > 0) {
        let calc = calculateDiscount(selectedTotal, availablePoints);
        discount = calc.discount;
        finalTotal = calc.finalTotal;
        pointsUsed = calc.pointsUsed;
        discountAmount = discount;
    } else {
        discountAmount = 0;
        pointsUsed = 0;
    }

    let tableHtml = `<div class="cart-layout"><div class="cart-table-wrapper"><table class="cart-table"><thead><tr>
        <th style="width:40px"><input type="checkbox" id="select-all-checkbox" ${cart.every(item => item.selected !== false) ? 'checked' : ''}></th>
        <th>Sản phẩm</th><th>Đơn giá</th><th>Số lượng</th><th>Thành tiền</th><th>Thao tác</th>
    </tr></thead><tbody>`;
    cart.forEach((item, idx) => {
        let thanhTien = item.giaBan * item.soLuong;
        let stockDisplay = (item.soLuongTon !== undefined && item.soLuongTon !== null && !isNaN(item.soLuongTon)) ? item.soLuongTon : '?';
        tableHtml += `<tr>
            <td><input type="checkbox" class="item-checkbox" data-index="${idx}" ${item.selected !== false ? 'checked' : ''}></td>
            <td class="product-info"><div class="product-name">${escapeHtml(item.tenSanPham)}</div><div class="product-code">Mã: ${escapeHtml(item.maSanPham)}</div><div class="product-stock">Còn: ${stockDisplay}</div></td>
            <td class="don-gia">${formatCurrency(item.giaBan)}</td>
            <td><div class="quantity-control"><button class="qty-decr" data-index="${idx}">-</button><input type="number" class="quantity-input" data-index="${idx}" value="${item.soLuong}" min="1" max="${item.soLuongTon || 999}" step="1"><button class="qty-incr" data-index="${idx}">+</button></div></td>
            <td class="thanh-tien">${formatCurrency(thanhTien)}</td>
            <td><button class="btn-remove" data-index="${idx}"><i class="fas fa-trash-alt"></i> Xóa</button></td>
        </tr>`;
    });
    tableHtml += `</tbody></table><div class="cart-toolbar"><button class="btn-sm btn-danger" onclick="deleteSelected()"><i class="fas fa-trash-alt"></i> Xóa các mục đã chọn</button></div></div>
    <div class="order-summary-wrapper"><div class="order-summary">
        <div class="summary-row"><span>Tạm tính (sản phẩm chọn):</span><span>${formatCurrency(selectedTotal)}</span></div>
        <div id="point-section" style="display: ${khachHangInfo && khachHangInfo.diemTichLuy > 0 ? 'block' : 'none'};">
            <hr>
            <div class="summary-row"><span>Điểm tích lũy:</span><span>${availablePoints} điểm</span></div>
            <div class="summary-row"><span>1 điểm = 100đ (giảm giá, tối đa 50% đơn hàng)</span><span></span></div>
            <label style="display:flex; align-items:center; gap:8px; margin:10px 0;">
                <input type="checkbox" id="use-points-check" ${usePoints ? 'checked' : ''}>
                Sử dụng toàn bộ điểm
            </label>
        </div>
        <div class="summary-row"><span>Giảm giá từ điểm:</span><span id="discount-amount">${formatCurrency(discountAmount)}</span></div>
        <div class="summary-row total"><span>Tổng cộng (đã trừ điểm):</span><span id="final-total">${formatCurrency(finalTotal)}</span></div>
        <button class="checkout-btn" id="checkoutBtn" ${selectedTotal === 0 ? 'disabled' : ''}><i class="fas fa-credit-card"></i> ĐẶT HÀNG NGAY</button>
    </div></div></div>`;
    container.innerHTML = tableHtml;

    // Gắn sự kiện
    document.querySelectorAll('.item-checkbox').forEach(cb => {
        cb.addEventListener('change', (e) => {
            let idx = cb.getAttribute('data-index');
            cart[idx].selected = cb.checked;
            saveCartToLocalStorage();
            renderCart();
        });
    });
    document.getElementById('select-all-checkbox')?.addEventListener('change', (e) => {
        let checked = e.target.checked;
        cart.forEach(item => item.selected = checked);
        saveCartToLocalStorage();
        renderCart();
    });
    document.querySelectorAll('.qty-incr').forEach(btn => {
        btn.addEventListener('click', (e) => {
            let idx = btn.getAttribute('data-index');
            let current = cart[idx].soLuong;
            let maxStock = cart[idx].soLuongTon || 999;
            if (current < maxStock) updateQuantity(idx, current + 1);
            else alert(`Chỉ còn ${maxStock} sản phẩm!`);
        });
    });
    document.querySelectorAll('.qty-decr').forEach(btn => {
        btn.addEventListener('click', (e) => {
            let idx = btn.getAttribute('data-index');
            let current = cart[idx].soLuong;
            if (current > 1) updateQuantity(idx, current - 1);
            else if (confirm("Xóa sản phẩm này?")) removeItem(idx);
        });
    });
    document.querySelectorAll('.btn-remove').forEach(btn => {
        btn.addEventListener('click', (e) => {
            let idx = btn.getAttribute('data-index');
            removeItem(idx);
        });
    });
    document.querySelectorAll('.quantity-input').forEach(inp => {
        inp.addEventListener('change', (e) => {
            let idx = inp.getAttribute('data-index');
            let newQty = parseInt(inp.value);
            let maxStock = cart[idx].soLuongTon || 999;
            if (isNaN(newQty) || newQty < 1) {
                alert("Số lượng >= 1");
                inp.value = cart[idx].soLuong;
                return;
            }
            if (newQty > maxStock) {
                alert(`Tối đa ${maxStock}`);
                inp.value = cart[idx].soLuong;
                return;
            }
            updateQuantity(idx, newQty);
        });
    });
    document.getElementById('use-points-check')?.addEventListener('change', (e) => {
        usePoints = e.target.checked;
        renderCart();
    });
    document.getElementById('checkoutBtn')?.addEventListener('click', checkout);
}

function updateQuantity(index, newQty) {
    const productId = cart[index].maSanPham;
    fetch(`${API_SANPHAM}?maSanPham=${productId}`)
        .then(res => res.json())
        .then(json => {
            let stock = cart[index].soLuongTon; // mặc định là giá trị cũ
            if (json.status === "success" && Array.isArray(json.data)) {
                const found = json.data.find(p => p.maSanPham === productId);
                if (found && typeof found.soLuongTon === 'number') {
                    stock = found.soLuongTon;
                }
            }
            if (newQty > stock) {
                alert(`Sản phẩm chỉ còn ${stock} trong kho!`);
                cart[index].soLuong = stock;
            } else {
                cart[index].soLuong = newQty;
            }
            saveCartToLocalStorage();
            renderCart();
        })
        .catch(() => {
            cart[index].soLuong = newQty;
            saveCartToLocalStorage();
            renderCart();
        });
}

function removeItem(index) {
    if (confirm("Xóa sản phẩm này?")) {
        cart.splice(index, 1);
        saveCartToLocalStorage();
        renderCart();
    }
}

function showInvoiceModal(orderData, selectedItems, selectedTotal, discount, finalTotal, pointsUsed) {
    let modalHtml = `
        <div id="invoiceModal" style="position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.6); z-index:10000; display:flex; align-items:center; justify-content:center;">
            <div style="background:white; max-width:600px; width:90%; border-radius:12px; padding:20px; max-height:80vh; overflow-y:auto;">
                <h3 style="color:#27ae60;">🧾 XÁC NHẬN HÓA ĐƠN</h3>
                <div style="margin-bottom:10px; color:#555;"><i class="fas fa-globe"></i> Hình thức: <strong>Đặt hàng online</strong></div>
                <table style="width:100%; border-collapse:collapse; margin-bottom:15px;">
                    <thead><tr style="background:#f8f9fa;"><th>Sản phẩm</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr></thead>
                    <tbody>
                        ${selectedItems.map(item => `<tr><td>${escapeHtml(item.tenSanPham)}</td><td>${item.soLuong}</td><td>${formatCurrency(item.giaBan)}</td><td>${formatCurrency(item.giaBan * item.soLuong)}</td></tr>`).join('')}
                    </tbody>
                </table>
                <div style="border-top:1px solid #ddd; padding-top:10px;">
                    <div style="display:flex; justify-content:space-between;"><span>Tạm tính:</span><span>${formatCurrency(selectedTotal)}</span></div>
                    <div style="display:flex; justify-content:space-between; color:#e67e22;">
                        <span>Giảm giá từ điểm (${pointsUsed} điểm, 1đ=100đ, tối đa 50% đơn):</span>
                        <span>- ${formatCurrency(discount)}</span>
                    </div>
                    <div style="display:flex; justify-content:space-between; font-size:1.3rem; font-weight:bold; margin-top:10px;">
                        <span>Tổng cộng:</span><span>${formatCurrency(finalTotal)}</span>
                    </div>
                </div>
                <div style="display:flex; gap:10px; margin-top:20px;">
                    <button id="confirmOrderBtn" style="flex:1; background:#2ecc71; color:white; border:none; padding:10px; border-radius:6px; cursor:pointer;">✅ Xác nhận thanh toán</button>
                    <button id="cancelOrderBtn" style="flex:1; background:#e74c3c; color:white; border:none; padding:10px; border-radius:6px; cursor:pointer;">❌ Hủy</button>
                </div>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    document.getElementById('confirmOrderBtn').onclick = async () => {
        document.getElementById('invoiceModal').remove();
        await executeCheckout(orderData, finalTotal, pointsUsed, discount);
    };
    document.getElementById('cancelOrderBtn').onclick = () => {
        document.getElementById('invoiceModal').remove();
        const btn = document.getElementById('checkoutBtn');
        if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fas fa-credit-card"></i> ĐẶT HÀNG NGAY'; }
    };
}

async function executeCheckout(orderData, finalTotal, pointsUsed, discount) {
    const btn = document.getElementById('checkoutBtn');
    if (btn) { btn.disabled = true; btn.innerHTML = 'Đang xử lý...'; }
    try {
        const requestBody = {
            maNhanVien: orderData.maNhanVien,
            maKhachHang: orderData.maKhachHang,
            tongTien: finalTotal,
            danhSachChiTiet: orderData.danhSachChiTiet,
            pointsUsed: pointsUsed,
            amountPaid: finalTotal
        };
        const response = await fetch(API_HOADON, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });
        const data = await response.json();
        if (data.status === "success") {
            alert(`✅ Thanh toán thành công!\nMã đơn: ${data.maHoaDon}\nTổng tiền: ${formatCurrency(finalTotal)}\nĐã dùng ${pointsUsed} điểm (giảm ${formatCurrency(discount)})\nTích lũy mới: +${Math.floor(finalTotal / POINT_EARN_RATE)} điểm`);
            cart = cart.filter(item => item.selected !== true);
            saveCartToLocalStorage();
            renderCart();
            setTimeout(() => window.location.href = "mua-sap.html", 1500);
        } else {
            alert("Thanh toán thất bại: " + data.message);
            if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fas fa-credit-card"></i> ĐẶT HÀNG NGAY'; }
        }
    } catch (error) {
        console.error(error);
        alert("Lỗi kết nối server!");
        if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fas fa-credit-card"></i> ĐẶT HÀNG NGAY'; }
    }
}

async function checkout() {
    // Kiểm tra tồn kho trước
    const validation = await validateAndFixStockBeforeCheckout();
    if (!validation.isValid) {
        alert("⚠️ Một số sản phẩm không đủ số lượng:" + validation.message + "\n\nSố lượng đã được điều chỉnh. Vui lòng kiểm tra lại giỏ hàng.");
        renderCart();
        return;
    }

    const selectedItems = cart.filter(item => item.selected === true);
    if (selectedItems.length === 0) {
        alert("Vui lòng chọn ít nhất một sản phẩm để thanh toán!");
        return;
    }

    let user = JSON.parse(localStorage.getItem("user_info"));
    if (!user || user.vaiTro !== "CUSTOMER") {
        alert("Vui lòng đăng nhập!");
        window.location.href = "login.html";
        return;
    }
    if (!user.maKhachHang) {
        await fetchKhachHangInfo();
        user = JSON.parse(localStorage.getItem("user_info"));
        if (!user.maKhachHang) {
            alert("Không tìm thấy mã khách hàng!");
            window.location.href = "login.html";
            return;
        }
    }

    const selectedTotal = getSelectedTotal();
    let discount = 0, pointsUsed = 0, finalTotal = selectedTotal;
    if (usePoints && khachHangInfo && khachHangInfo.diemTichLuy > 0) {
        const calc = calculateDiscount(selectedTotal, khachHangInfo.diemTichLuy);
        discount = calc.discount;
        finalTotal = calc.finalTotal;
        pointsUsed = calc.pointsUsed;
    }

    const orderData = {
        maNhanVien: "ONLINE",
        maKhachHang: user.maKhachHang,
        tongTien: finalTotal,
        danhSachChiTiet: selectedItems.map(item => ({
            maSanPham: item.maSanPham,
            soLuong: item.soLuong,
            giaBan: item.giaBan
        }))
    };

    showInvoiceModal(orderData, selectedItems, selectedTotal, discount, finalTotal, pointsUsed);
}

function formatCurrency(amount) {
    if (isNaN(amount) || amount === undefined) amount = 0;
    return amount.toLocaleString('vi-VN') + ' đ';
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, m => {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

function setupUserMenu() {
    const userBtn = document.querySelector('.user-btn');
    const dropdown = document.querySelector('.dropdown-content');
    if (userBtn && dropdown) {
        userBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
        });
        window.addEventListener('click', () => {
            dropdown.style.display = 'none';
        });
    }
}

function logout() {
    if (confirm("Đăng xuất?")) {
        localStorage.removeItem('user_info');
        localStorage.removeItem('shopping_cart');
        window.location.href = "login.html";
    }
}