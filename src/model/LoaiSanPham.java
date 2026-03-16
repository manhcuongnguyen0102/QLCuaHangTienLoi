package model;

public class LoaiSanPham {
    private  String maLoai;
    private  String tenLoai;
    private  String moTa;

    public LoaiSanPham() {
    }

    public LoaiSanPham(String maLoai, String moTa, String tenLoai) {
        this.maLoai = maLoai;
        this.moTa = moTa;
        this.tenLoai = tenLoai;
    }

    public String getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(String maLoai) {
        this.maLoai = maLoai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }
}
