package DaoInterFace;

import java.util.List;

import DAO.PhieuNhapDAO;
import model.PhieuNhap;
import model.ChiTietPhieuNhap;

public interface IPhieuNhapDAO {

    boolean taoPhieuNhap(PhieuNhap pn, List<ChiTietPhieuNhap> dsChiTiet);
    public String sinhMaPhieuNhapMoi();
    public List<PhieuNhap> layTatCa();
    public PhieuNhap timTheoMa(String maPhieuNhap);
    public List<ChiTietPhieuNhap> layChiTietCuaPhieu(String maPhieuNhap);

}