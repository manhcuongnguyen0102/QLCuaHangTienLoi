package DaoInterFace;

import java.util.List;
import model.PhieuNhap;
import model.ChiTietPhieuNhap;

public interface IPhieuNhapDAO {
    // Tương tự hóa đơn, dùng để nhập hàng vào kho
    boolean taoPhieuNhap(PhieuNhap pn, List<ChiTietPhieuNhap> dsChiTiet);

}