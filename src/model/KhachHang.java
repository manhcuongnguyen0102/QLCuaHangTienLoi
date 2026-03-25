package model;

import java.sql.Date;

public class KhachHang {
    private String maKhachHang;
    private  String tenKhachHang;
    private String soDienThoai;
    private Date ngayDangKy;
    private String tenDangNhap;
    private int diemTichLuy;

    public KhachHang() {
    }

    public KhachHang(int diemTichLuy, String maKhachHang, Date ngayDangKy, String soDienThoai, String tenDangNhap, String tenKhachHang) {
        this.diemTichLuy = diemTichLuy;
        this.maKhachHang = maKhachHang;
        this.ngayDangKy = ngayDangKy;
        this.soDienThoai = soDienThoai;
        this.tenDangNhap = tenDangNhap;
        this.tenKhachHang = tenKhachHang;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    public Date getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(Date ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
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

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }
}
