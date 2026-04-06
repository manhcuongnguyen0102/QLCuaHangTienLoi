package DaoInterFace;

import model.TaiKhoan;

public interface ITaiKhoanDAO {
    // Trả về đối tượng tài khoản nếu đúng user/pass, ngược lại trả về null
    TaiKhoan kiemTraDangNhap(String user, String pass);
    boolean doiMatKhau(String user, String oldPass, String newPass);
    boolean capNhatTrangThai(String user, int status); // 1: Active, 0: Locked
    //public boolean dangKyTaiKhoan(String tenDangNhap, String matKhau);
    public boolean kiemTraTonTai(String tenDangNhap);
    public boolean dangKyKhachHang(String tenKH, String sdt, String tenDN, String matKhau);
    public boolean doiMatKhauBangSDT(String sdt, String matKhauMoi);
    public boolean kiemTraSDTExist(String sdt);

}