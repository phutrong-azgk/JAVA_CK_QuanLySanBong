package HUIT.football.controller;

import HUIT.football.model.KhachHang;
import HUIT.football.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/khach")
public class KhachHangController {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @GetMapping
    public String khachPage() {
        return "khach"; // Trỏ đến src/main/resources/templates/khach.html
    }

    @GetMapping("/api/get-all")
    @ResponseBody
    public List<KhachHang> getAllCustomers() {
        return khachHangRepository.findAll();
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createCustomer(@ModelAttribute KhachHang khachHang) {
        // Đặt giá trị mặc định cho khách mới
        khachHang.setSoHoaDon(0);
        khachHang.setTongChi(0.0);

        khachHangRepository.save(khachHang);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Thêm khách hàng thành công!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/edit")
    @ResponseBody
    public ResponseEntity<?> editCustomer(@ModelAttribute KhachHang khachHangInput) {
        Map<String, Object> response = new HashMap<>();
        Optional<KhachHang> optKhach = khachHangRepository.findById(khachHangInput.getMaKhach());

        if (optKhach.isPresent()) {
            KhachHang existing = optKhach.get();
            // Chỉ cập nhật các thông tin cơ bản
            existing.setTenKhach(khachHangInput.getTenKhach());
            existing.setSoDienThoai(khachHangInput.getSoDienThoai());
            existing.setEmail(khachHangInput.getEmail());
            existing.setTaiKhoan(khachHangInput.getTaiKhoan());

            khachHangRepository.save(existing);

            response.put("success", true);
            response.put("message", "Cập nhật thông tin thành công!");
        } else {
            response.put("success", false);
            response.put("message", "Không tìm thấy khách hàng!");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/delete")
    @ResponseBody
    public ResponseEntity<?> deleteCustomer(@RequestParam("maKhach") Long maKhach) {
        Map<String, Object> response = new HashMap<>();
        if (khachHangRepository.existsById(maKhach)) {
            khachHangRepository.deleteById(maKhach);
            response.put("success", true);
            response.put("message", "Đã xóa khách hàng!");
        } else {
            response.put("success", false);
            response.put("message", "Không tìm thấy khách hàng để xóa!");
        }
        return ResponseEntity.ok(response);
    }
}