package DaoInterFace;

import java.util.List;
import model.NhaCungCap;

public interface INhaCungCapDAO {
    List<NhaCungCap> layTatCa();
    public NhaCungCap timTheoMa(String maNCC);
    public boolean them(NhaCungCap ncc);
    public boolean xoa(String maNCC);
    public boolean capNhat(NhaCungCap ncc);

}
