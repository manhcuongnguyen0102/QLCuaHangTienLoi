package API;

import DAO.LoaiSanPhamDAO;
import model.LoaiSanPham;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;

import java.io.*;
import java.util.List;

@WebServlet("/API/LoaiSanPhamAPI")
public class LoaiSanPhamAPI extends HttpServlet {

    private LoaiSanPhamDAO dao = new LoaiSanPhamDAO();

    // GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        List<LoaiSanPham> list = dao.layTatCa();

        PrintWriter out = response.getWriter();
        out.print("[");
        for (int i = 0; i < list.size(); i++) {
            LoaiSanPham l = list.get(i);
            out.print("{");
            out.print("\"maLoai\":\"" + l.getMaLoai() + "\",");
            out.print("\"tenLoai\":\"" + l.getTenLoai() + "\",");
            out.print("\"moTa\":\"" + l.getMoTa() + "\"");
            out.print("}");
            if (i < list.size() - 1) out.print(",");
        }
        out.print("]");
    }

    //  POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String maLoai = request.getParameter("maLoai");
        String tenLoai = request.getParameter("tenLoai");
        String moTa = request.getParameter("moTa");

        LoaiSanPham loai = new LoaiSanPham(maLoai, moTa, tenLoai);

        boolean kq = dao.them(loai);

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (kq) {
            out.print("{\"message\":\"Thêm thành công\"}");
        } else {
            out.print("{\"message\":\"Tên loại đã tồn tại hoặc lỗi\"}");
        }
    }

    // PUT
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        String json = sb.toString();

        String maLoai = json.split("\"maLoai\":\"")[1].split("\"")[0];
        String tenLoai = json.split("\"tenLoai\":\"")[1].split("\"")[0];
        String moTa = json.split("\"moTa\":\"")[1].split("\"")[0];

        LoaiSanPham loai = new LoaiSanPham(maLoai, moTa, tenLoai);

        boolean kq = dao.capNhat(loai);

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (kq) {
            out.print("{\"message\":\"Cập nhật thành công\"}");
        } else {out.print("{\"message\":\"Cập nhật thất bại\"}");
        }
    }

    // DELETE:
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maLoai = request.getParameter("maLoai");

        boolean kq = dao.xoa(maLoai);

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (kq) {
            out.print("{\"message\":\"Xóa thành công\"}");
        } else {
            out.print("{\"message\":\"Loại đang chứa sản phẩm, không thể xóa!\"}");
        }
    }
}