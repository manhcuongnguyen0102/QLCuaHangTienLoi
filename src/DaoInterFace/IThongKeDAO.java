package DaoInterFace;

import java.util.List;
import java.util.Map;

import model.SanPham;
import model.ThongKeBieuDo;

public interface IThongKeDAO {
    // ========================================================
    // NHÓM 1: THỐNG KÊ TỔNG QUAN HÔM NAY (Dùng cho 4 thẻ thẻ bài trên Dashboard)
    // ========================================================
    public double doanhThuHomNay();
    public int soHoaDonHomNay();
    public int soKhachHangMoiHomNay();
    public int soSanPhamSapHet();
    // ========================================================
    // NHÓM 2: THỐNG KÊ KHOẢNG THỜI GIAN & BIỂU ĐỒ
    // ========================================================
    public double doanhThuKhoangThoiGian(java.sql.Date tuNgay, java.sql.Date denNgay);
    public double loiNhuanTheoKhoang(java.sql.Date tuNgay, java.sql.Date denNgay);
    // ========================================================
    // NHÓM 3: BẢNG XẾP HẠNG (TOP 5)
    // ========================================================
    public List<SanPham> layTop5BanChayTrongThang();
    public List<Map<String, Object>> layTop5KhachHangVIP();
    public List<SanPham> layHangSapHet();
    public List<ThongKeBieuDo> layDoanhThuBieuDoTheoKhoang(String tuNgay, String denNgay);
    public List<ThongKeBieuDo> layDoanhThu7NgayQua();
}