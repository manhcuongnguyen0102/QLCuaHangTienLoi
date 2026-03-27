package DaoInterFace;

import java.util.List;
import model.SanPham;

public interface ISanPhamDAO {
    List<SanPham> layTatCa();
    List<SanPham> timTheoTen(String ten);
    List<SanPham> layTheoLoai(String maLoai);
    boolean them(SanPham sp);
    boolean sua(SanPham sp);
    boolean capNhatSoLuongTon(String maSP, int soLuongThayDoi);
    public int xoa(String maSP);
    public SanPham timTheoMa(String maSP);
}