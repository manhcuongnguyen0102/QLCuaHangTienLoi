package model;

public class ChiTietPhieuNhap {
    private int maChiTiet;
    private String maPhieuNhap;
    private String maSanPham;
    private int soLuong;
    private double giaNhap;

    public ChiTietPhieuNhap() {
    }

    public ChiTietPhieuNhap(double giaNhap, int maChiTiet, String maPhieuNhap, String maSanPham, int soLuong) {
        this.giaNhap = giaNhap;
        this.maChiTiet = maChiTiet;
        this.maPhieuNhap = maPhieuNhap;
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
    }

    public double getGiaNhap() {
        return giaNhap;
    }

    public void setGiaNhap(double giaNhap) {
        this.giaNhap = giaNhap;
    }

    public int getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(int maChiTiet) {
        this.maChiTiet = maChiTiet;
    }

    public String getMaPhieuNhap() {
        return maPhieuNhap;
    }

    public void setMaPhieuNhap(String maPhieuNhap) {
        this.maPhieuNhap = maPhieuNhap;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }
}
