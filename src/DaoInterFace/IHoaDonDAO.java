package DaoInterFace;

import java.util.List;
import model.HoaDon;
import model.ChiTietHoaDon;

public interface IHoaDonDAO {
    // Hàm này sẽ lưu vào cả bảng HoaDon và bảng ChiTietHoaDon (dùng Transaction)
    boolean taoHoaDon(HoaDon hd, List<ChiTietHoaDon> dsChiTiet);
    List<HoaDon> layLichSu(String maKH);

}