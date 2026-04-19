package HUIT.football.controller;

import HUIT.football.model.MatHang;
import HUIT.football.service.MatHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/kho")
public class MatHangController {

    @Autowired
    private MatHangService matHangService;

    // 1. Trả về giao diện HTML
    @GetMapping
    public String khoPage() {
        return "kho";
    }

    // ==========================================
    // CÁC API TƯƠNG TÁC VỚI DATABASE (REAL DATA)
    // ==========================================

    @GetMapping("/api/get-all")
    @ResponseBody
    public List<Map<String, Object>> getAllItems() {
        List<MatHang> danhSach = matHangService.getAll();
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (MatHang item : danhSach) {
            Map<String, Object> map = new HashMap<>();
            map.put("maMon", item.getMaMon());
            map.put("tenMon", item.getTenMon());
            map.put("donGia", item.getDonGia());
            map.put("soLuongTon", item.getSoLuongTon());
            map.put("loaiHang", item.getLoaiHang());

            // Xử lý logic trạng thái để frontend lên màu
            String status = "Còn hàng";
            if (item.getSoLuongTon() == 0) {
                status = "Hết hàng";
            } else if (item.getSoLuongTon() <= 10) {
                status = "Sắp hết";
            }
            map.put("Status", status);

            responseList.add(map);
        }
        return responseList;
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createItem(@ModelAttribute MatHang matHang) {
        // Gán loại hàng mặc định nếu chưa chọn
        if(matHang.getLoaiHang() == null || matHang.getLoaiHang().isEmpty()) {
            matHang.setLoaiHang("Dịch vụ");
        }

        matHangService.save(matHang);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã thêm mặt hàng thành công!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/edit")
    @ResponseBody
    public ResponseEntity<?> editItem(@ModelAttribute MatHang matHang) {
        // Hàm save của JPA nếu truyền vào object có ID đã tồn tại thì nó tự hiểu là Update
        matHangService.save(matHang);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã cập nhật thông tin mặt hàng!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/delete")
    @ResponseBody
    public ResponseEntity<?> deleteItem(@RequestParam("maMon") String maMon) {
        matHangService.delete(maMon);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã xóa mặt hàng thành công!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/update-stock")
    @ResponseBody
    public ResponseEntity<?> updateStock(@RequestParam("maMon") String maMon,
                                         @RequestParam("quantity") int quantity,
                                         @RequestParam("type") String type) {

        boolean success = matHangService.updateStock(maMon, quantity, type);
        Map<String, Object> response = new HashMap<>();

        if (success) {
            response.put("success", true);
            response.put("message", (type.equals("import") ? "Nhập" : "Xuất") + " kho thành công!");
        } else {
            response.put("success", false);
            response.put("message", "Thất bại: Số lượng xuất không được lớn hơn tồn kho!");
        }
        return ResponseEntity.ok(response);
    }
}