package DaoInterFace;

import java.util.List;
import model.NhanVien;

public interface INhanVienDAO {
    List<NhanVien> layTatCa();
    NhanVien timTheoMa(String maNV);
    boolean them(NhanVien nv);
    boolean sua(NhanVien nv);
    boolean xoa(String maNV);
}