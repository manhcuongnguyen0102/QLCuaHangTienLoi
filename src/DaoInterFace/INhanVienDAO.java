package DaoInterFace;

import java.util.List;
import model.NhanVien;

public interface INhanVienDAO {
    List<NhanVien> layTatCa();
    NhanVien timTheoMa(String maNV);
    public boolean them(NhanVien nv, String matKhauMacDinh);
    boolean sua(NhanVien nv);
    boolean xoa(String maNV);
}