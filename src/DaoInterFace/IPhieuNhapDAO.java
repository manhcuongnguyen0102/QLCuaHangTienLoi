package DaoInterFace;

import java.util.List;

import DAO.PhieuNhapDAO;
import model.PhieuNhap;
import model.ChiTietPhieuNhap;

public interface IPhieuNhapDAO {
    // Tương tự hóa đơn, dùng để nhập hàng vào kho
    boolean taoPhieuNhap(PhieuNhap pn, List<ChiTietPhieuNhap> dsChiTiet);
     // THÊM MỚI: Dùng cho phương thức GET (Lấy lịch sử nhập hàng)
    List<PhieuNhapDAO.PhieuNhapDTO> layDanhSachPhieuNhap();
}