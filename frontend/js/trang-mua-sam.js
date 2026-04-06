/**
 * BIẾN TOÀN CỤC LƯU TRỮ TRẠNG THÁI
 */
let cart = JSON.parse(localStorage.getItem('shopping_cart')) || [];
let currentPage = 1;
let productsPerPage = 12; // 1 trang hiện 12 món (sếp có thể đổi thành 8, 10, 15 tùy ý)
let currentFilteredList = []; // Danh sách đang được lọc (để biết có bao nhiêu trang)


function updateCartUI() {
    const cartCountElement = document.querySelector('.cart-count');
    if (cartCountElement) {
        const totalQty = cart.reduce((sum, item) => sum + (item.soLuong || 0), 0);
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
    hienThiTenKhach();
    updateCartUI();
    setupSearch();
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
        const response = await fetch('http://localhost:8080/QuanLyCuaHangTienLoi/API/SanPhamAPI');
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

    // 1. Cập nhật mảng gốc vào biến toàn cục để dùng cho các trang sau
    currentFilteredList = data;

    // 2. Thuật toán cắt mảng (Lấy đúng số món của trang hiện tại)
    const startIndex = (currentPage - 1) * productsPerPage;
    const endIndex = startIndex + productsPerPage;
    
    // Tạo ra mảng dataToShow chỉ chứa 12 món
    const dataToShow = currentFilteredList.slice(startIndex, endIndex);

    // 3. Render HTML
    if (dataToShow.length === 0) {
        grid.innerHTML = '<p style="text-align:center; width:100%; grid-column: 1/-1; padding: 20px;">Không có sản phẩm nào.</p>';
    } else {
        grid.innerHTML = dataToShow.map(p => `
            <div class="product-card" onclick="openProductModal('${p.maSanPham}')">
                <div class="product-img-container">
                    <img src="../img/${p.maSanPham}.jpg" alt="${p.tenSanPham}" 
                    onerror="this.onerror=null;this.src='../img/${p.maSanPham}.png';this.onerror=function(){this.src='https://via.placeholder.com/200?text=Chua+Co+Anh'};">
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

    // 4. Gọi hàm vẽ thanh nút bấm ở bên dưới
    renderPaginationUI(currentFilteredList.length);
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
            onerror="this.onerror=null; this.src='https://via.placeholder.com/400?text=San+Pham'">`;
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
function handleAddToCartFromModal() {
    const qtyInput = document.getElementById('purchase-quantity');
    const qty = parseInt(qtyInput.value);

    if (qty > currentProduct.soLuongTon) {
        alert(`Kho chỉ còn ${currentProduct.soLuongTon} sản phẩm!`);
        return;
    }

    const existingItem = cart.find(item => item.maSanPham === currentProduct.maSanPham);

   if (existingItem) {
        // Nếu đã có hàng, chỉ cộng dồn vào biến soLuong
        existingItem.soLuong += qty; 
    } else {
        // Nếu chưa có, thêm mới (Tuyệt đối không nhét thêm chữ quantity vào đây nữa)
        cart.push({
            maSanPham: currentProduct.maSanPham,
            tenSanPham: currentProduct.tenSanPham,
            giaBan: currentProduct.giaBan,
            soLuong: qty, 
            soLuongTon: currentProduct.soLuongTon
        });
    }

    // Lưu lại bộ nhớ (dùng chuẩn tên shopping_cart)
    localStorage.setItem('shopping_cart', JSON.stringify(cart));

    updateCartUI();
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
        const response = await fetch('http://localhost:8080/QuanLyCuaHangTienLoi/API/LoaiSanPhamAPI');
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
    currentPage = 1;
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
function hienThiTenKhach() {
    const userStr = localStorage.getItem('user_info');
    if (userStr) {
        const user = JSON.parse(userStr);
        const nameDisplay = document.getElementById('user-name-display');
        if (nameDisplay) {
            nameDisplay.innerText = user.tenDangNhap;
        }
    } else {
        window.location.href = "login.html";
    }
}

function logout() {
    if(confirm("Bạn có chắc chắn muốn đăng xuất không?")) {
        localStorage.removeItem('user_info');
        localStorage.removeItem('shopping_cart'); // Xóa luôn giỏ hàng khi đăng xuất
        window.location.href = "login.html";
    }
}
function setupSearch() {
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) return;

    // Lắng nghe sự kiện 'input' (mỗi khi gõ, xóa, hoặc copy paste vào ô)
    searchInput.addEventListener('input', function(e) {
        // Lấy chữ người dùng gõ, chuyển hết thành chữ thường và xóa dấu cách thừa
        const keyword = e.target.value.toLowerCase().trim();
        currentPage = 1;
        // Nếu ô tìm kiếm trống, hiển thị lại toàn bộ sản phẩm
        if (keyword === '') {
            renderProducts(allProducts);
            return;
        }

        // Lọc trong mảng allProducts gốc: 
        // Lấy những SP mà tên của nó (đổi ra chữ thường) có chứa từ khóa
        const filteredProducts = allProducts.filter(p => 
            p.tenSanPham.toLowerCase().includes(keyword)
        );

        // Gọi hàm render quen thuộc để vẽ lại lưới sản phẩm với mảng đã lọc
        renderProducts(filteredProducts);
    });
    
    
}
// Hàm vẽ các nút 1, 2, 3, Trước, Sau
function renderPaginationUI(totalItems) {
    const container = document.getElementById('pagination-container');
    if (!container) return;
    container.innerHTML = ''; // Xóa cũ

    const totalPages = Math.ceil(totalItems / productsPerPage);
    if (totalPages <= 1) return; // Có 1 trang thì không cần hiện nút

    // Nút "Trước"
    let html = `<button class="page-btn" ${currentPage === 1 ? 'disabled' : ''} onclick="changePage(${currentPage - 1})"><i class="fas fa-chevron-left"></i></button>`;

    // Các nút số 1, 2, 3...
    for (let i = 1; i <= totalPages; i++) {
        html += `<button class="page-btn ${currentPage === i ? 'active' : ''}" onclick="changePage(${i})">${i}</button>`;
    }

    // Nút "Sau"
    html += `<button class="page-btn" ${currentPage === totalPages ? 'disabled' : ''} onclick="changePage(${currentPage + 1})"><i class="fas fa-chevron-right"></i></button>`;

    container.innerHTML = html;
}

// Hàm xử lý khi bấm vào số trang
function changePage(pageNumber) {
    const totalPages = Math.ceil(currentFilteredList.length / productsPerPage);
    if (pageNumber < 1 || pageNumber > totalPages) return;

    currentPage = pageNumber; // Cập nhật trang hiện tại
    renderProducts(currentFilteredList); // Load lại lưới sản phẩm

    // Cuộn mượt mà lên đầu Lưới sản phẩm để khách xem
    document.getElementById('product-grid').scrollIntoView({ behavior: 'smooth', block: 'start' });
}
