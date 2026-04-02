/**
 * BIẾN TOÀN CỤC LƯU TRỮ TRẠNG THÁI
 */
let cart = []; // Biến lưu trữ danh sách sản phẩm trong giỏ

// Hàm vẽ lại con số trên Header
function updateCartUI() {
    const cartCountElement = document.querySelector('.cart-count');
    if (cartCountElement) {
        // Tính tổng số lượng của tất cả mặt hàng trong giỏ
        const totalQty = cart.reduce((sum, item) => sum + item.quantity, 0);
        cartCountElement.innerText = totalQty;
    }
}
let currentProduct = null; // Sản phẩm đang xem trong Modal
let allProducts = [];      // Danh sách sản phẩm gốc từ API

/**
 * 1. KHỞI TẠO KHI TRANG WEB LOAD XONG
 */
document.addEventListener('DOMContentLoaded', () => {
    fetchCategories();
    fetchProducts();
    setupUserMenu();
});

/**
 * 2. GỌI API LẤY DANH SÁCH SẢN PHẨM (GET)
 */
async function fetchProducts() {
    const grid = document.getElementById('product-grid');
    if (!grid) return;

    try {
        // 1. Dùng đường dẫn tương đối để tránh lỗi Context Path
        //const response = await fetch('../API/SanPhamAPI');
        const response = await fetch('http://localhost:8080/API/SanPhamAPI');
        if (!response.ok) {
            throw new Error("API lỗi hoặc không tìm thấy");
        }

        // 2. Nhận Object JSON từ Java
        const result = await response.json();
        console.log("Dữ liệu gốc từ API:", result); // Xem cấu trúc ở F12 Console

        // 3. Lấy mảng nằm trong thuộc tính 'data' (do Java trả về jsonResponse.add("data", ...))
        allProducts = result.data;

        if (allProducts && allProducts.length > 0) {
            renderProducts(allProducts);
        } else {
            grid.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 50px;">Không có sản phẩm nào trong danh sách data.</p>';
        }
    } catch (error) {
        console.error("Lỗi kết nối API:", error);
        grid.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: red; padding: 50px;">Lỗi: ${error.message}</p>`;
    }
}
/**
 * 3. VẼ SẢN PHẨM RA GIAO DIỆN
 */
function renderProducts(data) {
    const grid = document.getElementById('product-grid');
    if (!grid) return;

    grid.innerHTML = data.map(p => `
        <div class="product-card" onclick="openProductModal('${p.maSanPham}')">
            <div class="product-img-container">
                <img src="../img/${p.maSanPham}.jpg" alt="${p.tenSanPham}" 
                     onerror="this.src='https://via.placeholder.com/200?text=Sản+Phẩm'">
            </div>
            <div class="product-info-content">
                <div class="product-name">${p.tenSanPham}</div>
                <div class="product-code">Mã SP: ${p.maSanPham}</div>
                <div class="product-price-row">
                    <span class="product-price">${p.giaBan.toLocaleString()} đ</span>
                    <button class="quick-add-icon" style="border:none; background:none;">
                        <i class="fas fa-shopping-cart"></i>
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

/**
 * 4. MỞ MODAL CHI TIẾT
 */
function openProductModal(maSP) {
    currentProduct = allProducts.find(p => p.maSanPham === maSP);
    if (!currentProduct) return;

    const modal = document.getElementById('productDetailModal');

    document.getElementById('modal-product-name').innerText = currentProduct.tenSanPham;
    document.getElementById('modal-product-code').innerText = 'Mã SP: ' + currentProduct.maSanPham;
    document.getElementById('modal-product-price').innerText = currentProduct.giaBan.toLocaleString() + ' đ';

    const imgContainer = document.getElementById('modal-img-container');
    if (imgContainer) {
        imgContainer.innerHTML = `<img src="../img/${currentProduct.maSanPham}.jpg" 
            style="width:100%; height:auto; max-height:350px; object-fit:contain;" 
            onerror="this.src='https://via.placeholder.com/400?text=Sản+Phẩm'">`;
    }

    const stockStatus = document.getElementById('modal-stock-status');
    const btnAdd = document.getElementById('add-to-cart-action-btn');

    if (currentProduct.soLuongTon > 0) {
        stockStatus.innerText = `Còn lại: ${currentProduct.soLuongTon}`;
        stockStatus.style.color = "#27ae60";
        btnAdd.disabled = false;
        btnAdd.style.opacity = "1";
    } else {
        stockStatus.innerText = "Hết hàng";
        stockStatus.style.color = "red";
        btnAdd.disabled = true;
        btnAdd.style.opacity = "0.5";
    }

    document.getElementById('purchase-quantity').value = 1;
    document.getElementById('modal-error-msg').style.display = 'none';

    modal.style.display = 'flex';
}

/**
 * 5. TĂNG GIẢM SỐ LƯỢNG
 */
function adjustQuantity(amount) {
    const qtyInput = document.getElementById('purchase-quantity');
    const errorMsg = document.getElementById('modal-error-msg');
    let currentVal = parseInt(qtyInput.value) || 1;
    let newQty = currentVal + amount;

    if (newQty < 1) return;

    if (newQty > currentProduct.soLuongTon) {
        errorMsg.innerText = `Kho chỉ còn ${currentProduct.soLuongTon} sản phẩm!`;
        errorMsg.style.display = 'block';
        qtyInput.value = currentProduct.soLuongTon;
        return;
    }

    errorMsg.style.display = 'none';
    qtyInput.value = newQty;
}

/**
 * 6. THÊM VÀO GIỎ HÀNG
 */
function handleAddToCartFromModal() {

    const qtyInput = document.getElementById('purchase-quantity');
    const qty = parseInt(qtyInput.value);

    // 1. Kiểm tra tồn kho từ biến currentProduct (lấy từ API)
    if (qty > currentProduct.soLuongTon) {
        alert(`Kho chỉ còn ${currentProduct.soLuongTon} sản phẩm!`);
        return;
    }

    // 2. Kiểm tra xem sản phẩm này đã có trong giỏ hàng chưa
    const existingItem = cart.find(item => item.maSanPham === currentProduct.maSanPham);

    if (existingItem) {
        // Nếu đã có, chỉ cộng thêm số lượng mới vào số lượng cũ
        existingItem.quantity += qty;
    } else {
        // Nếu chưa có, thêm một đối tượng mới hoàn toàn vào mảng
        cart.push({
            maSanPham: currentProduct.maSanPham,
            tenSanPham: currentProduct.tenSanPham,
            giaBan: currentProduct.giaBan,
            quantity: qty
        });
    }

    // 3. Gọi hàm cập nhật con số hiển thị trên Header
    updateCartUI();

    // 4. Thông báo và đóng Modal
    alert(`Đã thêm ${qty} ${currentProduct.tenSanPham} vào giỏ hàng!`);
    closeProductModal();
}

/**
 * 7. ĐÓNG MODAL
 */
function closeProductModal() {
    document.getElementById('productDetailModal').style.display = 'none';
}

window.onclick = function(event) {
    const modal = document.getElementById('productDetailModal');
    if (event.target == modal) {
        closeProductModal();
    }
}

/**
 * 8. DANH MỤC & MENU
 */

async function fetchCategories() {
    const listLoai = document.getElementById('listLoaiSP');
    if (!listLoai) return;

    try {
        // const response = await fetch('../API/LoaiSanPhamAPI');
        const response = await fetch('http://localhost:8080/API/LoaiSanPhamAPI');
        const result = await response.json();
        const categories = result.data; // Lấy mảng từ thuộc tính data của API

        // Tạo nội dung HTML
        let html = `<a href="javascript:void(0)" class="active" onclick="filterByCategory('all', this)">Tất cả sản phẩm</a>`;

        html += categories.map(cat => `
            <a href="javascript:void(0)" onclick="filterByCategory('${cat.maLoai}', this)">
                ${cat.tenLoai}
            </a>
        `).join('');

        listLoai.innerHTML = html;
    } catch (error) {
        console.error("Lỗi danh mục:", error);
        listLoai.innerHTML = '<p>Lỗi tải danh mục</p>';
    }
}

function filterByCategory(maLoai, element) {
    // 1. Xóa class 'active' của tất cả các mục danh mục khác
    const allLinks = document.querySelectorAll('#listLoaiSP a');
    allLinks.forEach(link => link.classList.remove('active'));

    // 2. Thêm class 'active' vào mục vừa được nhấn
    element.classList.add('active');

    // 3. Logic lọc sản phẩm (giữ nguyên của bạn)
    const filtered = (maLoai === 'all') ? allProducts : allProducts.filter(p => p.maLoai === maLoai);
    renderProducts(filtered);
}

function setupUserMenu() {
    const userBtn = document.querySelector('.user-btn');
    const dropdown = document.querySelector('.dropdown-content');
    if (userBtn && dropdown) {
        userBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            // Toggle hiển thị
            const isDisplayed = dropdown.style.display === 'block';
            dropdown.style.display = isDisplayed ? 'none' : 'block';
        });
        window.addEventListener('click', () => {
            dropdown.style.display = 'none';
        });
    }
}

function logout() {
    if(confirm("Bạn có chắc chắn muốn đăng xuất không?")) {
        localStorage.removeItem('user_info');
        window.location.href = "login.html";
    }
}