document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const messageBox = document.getElementById('messageBox');
    const loginBtn = document.getElementById('loginBtn');

    // Hàm hiển thị thông báo trên màn hình
    function showMessage(text, isError) {
        messageBox.style.display = 'block';
        messageBox.textContent = text;
        if (isError) {
            messageBox.className = 'message-box message-error';
        } else {
            messageBox.className = 'message-box message-success';
        }
    }

    loginForm.addEventListener('submit', function(event) {
        event.preventDefault(); // Ngăn load lại trang

        // 1. Lấy dữ liệu
        const user = document.getElementById('username').value;
        const pass = document.getElementById('password').value;

        const dataGoiLen = {
            username: user,
            password: pass
        };

        // Đổi trạng thái nút bấm cho chuyên nghiệp
        loginBtn.textContent = "Đang xử lý...";
        loginBtn.disabled = true;

        // 2. Cấu hình URL API (Giữ nguyên như cũ của bạn)
        const urlAPI = 'http://localhost:8080/API/LoginAPI';

        // 3. Gọi Fetch API
        fetch(urlAPI, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(dataGoiLen)
        })
        .then(response => {
            return response.json().then(data => ({ status: response.status, body: data }));
        })
        .then(ketQua => {
            // Khôi phục nút bấm
            loginBtn.textContent = "Đăng nhập ngay";
            loginBtn.disabled = false;

            if (ketQua.status === 200) {
                // Thành công
                showMessage("Đăng nhập thành công! Đang chuyển hướng...", false);
                console.log("Dữ liệu từ Java gửi về là: ", ketQua.body.data);

                // Lưu dữ liệu để dùng cho các trang sau
                let userData = ketQua.body.data;
                userData.selectedProducts = [];
                localStorage.setItem('user_info', JSON.stringify(userData));
                
                // Đợi 1 giây rồi chuyển trang cho mượt
                setTimeout(() => {
                    window.location.href = "index.html"; 
                }, 1000);

            } else {
                // Thất bại (sai pass, sai user)
                showMessage(ketQua.body.message, true);
            }
        })
        .catch(error => {
            console.error('Lỗi API:', error);
            loginBtn.textContent = "Đăng nhập ngay";
            loginBtn.disabled = false;
            showMessage("Không thể kết nối đến Máy chủ. Vui lòng thử lại!", true);
        });
    });
});