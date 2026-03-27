package model;
import java.sql.Date;

public class SanPham {
    private String maSanPham;
    private String tenSanPham;
    private double giaNhap;
    private double giaBan;
    private int soLuongTon;
    private Date ngayHetHan;
    private String maLoai;
    private String maNCC;
    private String tenLoai;
    private String tenNCC;
    public SanPham() {
    }

    public SanPham(String maSanPham, String tenSanPham, double giaNhap, double giaBan, int soLuongTon, Date ngayHetHan, String maLoai, String maNCC, String tenLoai, String tenNCC) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
        this.soLuongTon = soLuongTon;
        this.ngayHetHan = ngayHetHan;
        this.maLoai = maLoai;
        this.maNCC = maNCC;
        this.tenLoai=tenLoai;
        this.tenNCC=tenNCC;
    }

    public double getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(double giaBan) {
        this.giaBan = giaBan;
    }

    public double getGiaNhap() {
        return giaNhap;
    }

    public void setGiaNhap(double giaNhap) {
        this.giaNhap = giaNhap;
    }

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String maNCC) {
        this.maNCC = maNCC;
    }

    public String getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(String maLoai) {
        this.maLoai = maLoai;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public Date getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(Date ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getTenNCC() {
        return tenNCC;
    }

    public void setTenNCC(String tenNCC) {
        this.tenNCC = tenNCC;
    }
}

