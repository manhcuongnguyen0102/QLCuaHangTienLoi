package DaoInterFace;

import java.util.List;
import model.HoaDon;
import model.ChiTietHoaDon;

public interface IHoaDonDAO {
    // Hàm này sẽ lưu vào cả bảng HoaDon và bảng ChiTietHoaDon (dùng Transaction)
    boolean taoHoaDon(HoaDon hd, List<ChiTietHoaDon> dsChiTiet);
    List<HoaDon> layLichSu(String maKH);
    public String sinhMaHoaDonMoi();
    public List<HoaDon> layTatCa();
    public HoaDon timTheoMa(String maHD);
    public List<ChiTietHoaDon> layChiTietCuaHoaDon(String maHD);
}