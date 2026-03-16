package DaoInterFace;

import java.util.List;
import model.KhachHang;

public interface IKhachHangDAO {
    List<KhachHang> layTatCa();
    KhachHang timTheoSDT(String sdt);
    boolean them(KhachHang kh);
    boolean capNhatDiem(String maKH, int diemMoi);
}