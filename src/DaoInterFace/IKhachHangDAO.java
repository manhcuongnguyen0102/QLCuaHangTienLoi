package DaoInterFace;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import model.KhachHang;

public interface IKhachHangDAO {
    List<KhachHang> layTatCa();
    KhachHang timTheoSDT(String sdt);
    boolean them(KhachHang kh);
    boolean capNhatDiem(String maKH, int diemMoi);
    public String taoMaKhachHangMoi(Connection conn) throws SQLException;
    public boolean capNhatThongTin(String maKH, String tenKHMoi, String sdtMoi);
}