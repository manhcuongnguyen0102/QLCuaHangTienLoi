package model;

public class ThongKeBieuDo {
    private String ngay;
    private double doanhThu;

    public ThongKeBieuDo(String ngay, double doanhThu) {
        this.ngay = ngay;
        this.doanhThu = doanhThu;
    }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }
    public double getDoanhThu() { return doanhThu; }
    public void setDoanhThu(double doanhThu) { this.doanhThu = doanhThu; }
}