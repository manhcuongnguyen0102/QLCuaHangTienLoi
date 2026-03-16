package DaoInterFace;

import java.util.List;
import model.SanPham;

public interface IThongKeDAO {
    // Tính tổng doanh thu theo tháng và năm
    double tinhDoanhThu(int thang, int nam);
    // Lấy danh sách 5 sản phẩm có số lượng bán ra nhiều nhất
    List<SanPham> layTop5BanChay();
    // Lấy các sản phẩm có số lượng tồn kho dưới mức cảnh báo (ví dụ < 10)
    List<SanPham> layHangSapHet();
}