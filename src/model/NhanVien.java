package model;

public class NhanVien {
    private String maNhanVien;
    private String tenNhanVien;
    private String chucVu;
    private String soDienThoai;
    private String tenDangNhap;

    public NhanVien() {
    }

    public NhanVien(String chucVu, String maNhanVien, String soDienThoai, String tenDangNhap, String tenNhanVien) {
        this.chucVu = chucVu;
        this.maNhanVien = maNhanVien;
        this.soDienThoai = soDienThoai;
        this.tenDangNhap = tenDangNhap;
        this.tenNhanVien = tenNhanVien;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }
}
