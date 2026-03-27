document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    const messageBox = document.getElementById('messageBox');
    const registerBtn = document.getElementById('registerBtn');

    // Hàm hiển thị thông báo lỗi/thành công lên màn hình
    function showMessage(text, isError) {
        messageBox.style.display = 'block';
        messageBox.textContent = text;
        messageBox.className = isError ? 'message-box message-error' : 'message-box message-success';
    }

    registerForm.addEventListener('submit', function(event) {
        event.preventDefault(); // Ngăn trang web bị load lại

        // 1. Lấy dữ liệu từ các ô nhập liệu (Dựa trên ID trong file HTML của bạn)
        const tenKH = document.getElementById('reg-tenkh').value;
        const sdt = document.getElementById('reg-phone').value;
        const user = document.getElementById('reg-username').value;
        const pass = document.getElementById('reg-password').value;
        const confirmPass = document.getElementById('reg-confirm-password').value;

        // 2. Kiểm tra nghiệp vụ cơ bản tại Client (Tiết kiệm tài nguyên Server)
        if (pass !== confirmPass) {
            showMessage("Mật khẩu xác nhận không khớp! Vui lòng kiểm tra lại.", true);
            return;
        }

        if (sdt.length < 10) {
            showMessage("Số điện thoại không hợp lệ!", true);
            return;
        }

        // 3. Đóng gói dữ liệu (KEY phải khớp 100% với class RegisterRequest trong Java)
        const dataGoiLen = {
            tenKH: tenKH,
            sdt: sdt,
            username: user,
            password: pass
        };

        // Hiệu ứng chờ cho nút bấm
        registerBtn.textContent = "Đang xử lý...";
        registerBtn.disabled = true;

        // 4. Gọi API (Lưu ý: Chữ /API/Register phải khớp chính xác với @WebServlet của bạn)
        const urlAPI = 'http://localhost:8080/QuanLyCuaHangTienLoi/API/RegisterAPI';

        fetch(urlAPI, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(dataGoiLen)
        })
        .then(response => {
            // Nhận phản hồi và bóc tách dữ liệu JSON
            return response.json().then(data => ({ status: response.status, body: data }));
        })
        .then(ketQua => {
            registerBtn.textContent = "Đăng ký tài khoản";
            registerBtn.disabled = false;

            if (ketQua.status === 200) {
                // Đăng ký thành công
                showMessage(ketQua.body.message, false);
                
                // Đợi 2 giây cho người dùng kịp đọc thông báo rồi đá về trang Login
                setTimeout(() => {
                    window.location.href = "login.html"; 
                }, 2000);
            } else {
                // Thất bại (Trùng username, lỗi server...)
                showMessage(ketQua.body.message, true);
            }
        })
        .catch(error => {
            console.error('Lỗi kết nối:', error);
            registerBtn.textContent = "Đăng ký tài khoản";
            registerBtn.disabled = false;
            showMessage("Không thể kết nối đến Máy chủ. Vui lòng kiểm tra lại Tomcat!", true);
        });
    });
});