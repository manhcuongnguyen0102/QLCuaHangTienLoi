package model;

public class TaiKhoan {
    // 1. Các thuộc tính (Private để đảm bảo tính đóng gói - Encapsulation)
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private boolean trangThai; // BIT trong SQL Server tương đương boolean trong Java

    // 2. Hàm tạo không tham số (Bắt buộc phải có để các Framework hoặc DAO khởi tạo đối tượng rỗng)
    public TaiKhoan() {
    }

    // 3. Hàm tạo có đầy đủ tham số
    public TaiKhoan(String tenDangNhap, String matKhau, String vaiTro, boolean trangThai) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
    }

    // 4. Các hàm Getter và Setter
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public boolean isTrangThai() { return trangThai; } // Với boolean, thường dùng 'is' thay vì 'get'
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
}