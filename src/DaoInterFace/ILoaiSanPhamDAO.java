package DaoInterFace;

import java.util.List;
import model.LoaiSanPham;

public interface ILoaiSanPhamDAO {
    List<LoaiSanPham> layTatCa();
    boolean them(LoaiSanPham loai);
    boolean capNhat(LoaiSanPham loai);
    LoaiSanPham timTheoMa(String maLoai);
    int xoa(String maLoai);
}