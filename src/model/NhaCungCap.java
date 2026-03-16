package model;

public class NhaCungCap {
    private String maNCC;
    private String tenNCC;
    private String diaChi;
    private String soDienThoai;

    public NhaCungCap() {
    }

    public NhaCungCap(String diaChi, String tenNCC, String maNCC, String soDienThoai) {
        this.diaChi = diaChi;
        this.tenNCC = tenNCC;
        this.maNCC = maNCC;
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String maNCC) {
        this.maNCC = maNCC;
    }

    public String getTenNCC() {
        return tenNCC;
    }

    public void setTenNCC(String tenNCC) {
        this.tenNCC = tenNCC;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }
}
