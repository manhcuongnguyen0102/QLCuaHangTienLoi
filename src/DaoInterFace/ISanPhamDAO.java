package DaoInterFace;

import java.util.List;
import model.SanPham;

public interface ISanPhamDAO {
    List<SanPham> layTatCa();
    List<SanPham> timTheoTen(String ten);
    List<SanPham> layTheoLoai(String maLoai);
    boolean them(SanPham sp);
    boolean sua(SanPham sp);
    // Quan trọng: Thay đổi số lượng tồn kho sau khi mua/nhập hàng
    boolean capNhatSoLuongTon(String maSP, int soLuongThayDoi);
    public boolean xoa(String maSP);
    public SanPham timTheoMa(String maSP);
}