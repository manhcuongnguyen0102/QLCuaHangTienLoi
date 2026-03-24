document.addEventListener('DOMContentLoaded', function() {
    const forgotForm = document.getElementById('forgotPasswordForm');
    const messageBox = document.getElementById('messageBox');
    const recoveryBtn = document.getElementById('recoveryBtn');
    const passwordSection = document.getElementById('passwordSection');
    const newPassInput = document.getElementById('new-password');
    const sdtInput = document.getElementById('recovery-sdt');

    let currentStep = 1;

    // Hàm hiển thị thông báo
    function showMessage(text, isError) {
        messageBox.style.display = 'block';
        messageBox.textContent = text;
        messageBox.className = isError ? 'message-box message-error' : 'message-box message-success';
    }

    forgotForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const sdt = sdtInput.value;
        // Gọi API xử lý đổi mật khẩu (Đường dẫn khớp với Servlet)
        const urlAPI = 'http://localhost:8080/QuanLyCuaHangTienLoi/API/ForgotPasswordAPI';

        if (currentStep === 1) {
            // Bước 1: Kiểm tra xem số điện thoại có tồn tại trên hệ thống không
            recoveryBtn.textContent = 'Đang kiểm tra...';
            recoveryBtn.disabled = true;

            fetch(`${urlAPI}?sdt=${sdt}`, {
                method: 'GET'
            })
            .then(response => response.json().then(data => ({ status: response.status, body: data })))
            .then(res => {
                recoveryBtn.disabled = false;
                if (res.status === 200 && res.body.exists === true) {
                    // Thành công: SDT tồn tại -> Chuyển sang Bước 2
                    currentStep = 2;
                    messageBox.style.display = 'none'; // Ẩn thông báo lỗi nếu có
                    passwordSection.style.display = 'block'; // Hiện ô nhập mật khẩu
                    newPassInput.required = true;
                    sdtInput.readOnly = true; // Khóa không cho đổi SDT nữa
                    recoveryBtn.textContent = 'Nhập mật khẩu mới';
                } else {
                    showMessage("Số điện thoại không tồn tại trong hệ thống!", true);
                    recoveryBtn.textContent = 'Xác minh';
                }
            })
            .catch(error => {
                console.error('Lỗi kết nối Bước 1:', error);
                showMessage("Không thể kết nối đến Máy chủ!", true);
                recoveryBtn.textContent = 'Xác minh';
                recoveryBtn.disabled = false;
            });

        } else if (currentStep === 2) {
            // Bước 2: Gọi API đổi mật khẩu
            const newPass = newPassInput.value;

            recoveryBtn.textContent = 'Đang lưu...';
            recoveryBtn.disabled = true;

            fetch(urlAPI, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    sdt: sdt,
                    newPass: newPass
                })
            })
            .then(response => response.json().then(data => ({ status: response.status, body: data })))
            .then(res => {
                if (res.status === 200) {
                    // Thành công đổi mật khẩu
                    showMessage(res.body.message, false);
                    setTimeout(() => {
                        window.location.href = 'login.html';
                    }, 2000);
                } else {
                    showMessage(res.body.message || "Đã xảy ra lỗi!", true);
                    recoveryBtn.textContent = 'Nhập mật khẩu mới';
                    recoveryBtn.disabled = false;
                }
            })
            .catch(error => {
                console.error('Lỗi kết nối Bước 2:', error);
                showMessage("Không thể kết nối đến Máy chủ!", true);
                recoveryBtn.textContent = 'Nhập mật khẩu mới';
                recoveryBtn.disabled = false;
            });
        }
    });
});