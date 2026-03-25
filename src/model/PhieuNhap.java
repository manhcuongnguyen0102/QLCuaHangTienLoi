package model;

import java.sql.Timestamp;
import java.sql.Date;

public class PhieuNhap {
    private String maPhieuNhap;
    private String maNCC;
    private String maNhanVien;
    private Timestamp ngayNhap;

    public PhieuNhap() {
    }

    public PhieuNhap(String maNCC, String maNhanVien, String maPhieuNhap, Timestamp ngayNhap) {
        this.maNCC = maNCC;
        this.maNhanVien = maNhanVien;
        this.maPhieuNhap = maPhieuNhap;
        this.ngayNhap = ngayNhap;
    }

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String maNCC) {
        this.maNCC = maNCC;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getMaPhieuNhap() {
        return maPhieuNhap;
    }

    public void setMaPhieuNhap(String maPhieuNhap) {
        this.maPhieuNhap = maPhieuNhap;
    }

    public Timestamp getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(Timestamp ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

}
