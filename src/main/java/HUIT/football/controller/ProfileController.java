package HUIT.football.controller;

import HUIT.football.model.KhachHang;
import HUIT.football.model.User;
import HUIT.football.repository.KhachHangRepository;
import HUIT.football.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired private UserRepository userRepository;
    @Autowired private KhachHangRepository khachHangRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired
    private HUIT.football.repository.HoaDonRepository hoaDonRepo;


    @GetMapping
    public String profilePage(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        // Cố gắng tìm khách hàng tương ứng với tài khoản này
        // (Lưu ý: Cần thêm hàm findByTaiKhoan(String taiKhoan) vào KhachHangRepository)
        KhachHang kh = khachHangRepo.findByTaiKhoan(username).orElse(new KhachHang());

        model.addAttribute("username", username);
        model.addAttribute("email", user != null ? user.getEmail() : "");
        model.addAttribute("tenKhach", kh.getTenKhach());
        model.addAttribute("soDienThoai", kh.getSoDienThoai());

        return "profile";
    }

    @PostMapping("/api/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestParam("tenKhach") String tenKhach,
                                           @RequestParam("soDienThoai") String soDienThoai,
                                           @RequestParam(value = "email", required = false) String email,
                                           @RequestParam(value = "password", required = false) String password,
                                           Principal principal) {
        Map<String, Object> response = new HashMap<>();
        if (principal == null) {
            response.put("success", false);
            return ResponseEntity.ok(response);
        }

        String username = principal.getName();

        // 1. Cập nhật bảng Khách Hàng (Tên, SĐT, Email)
        HUIT.football.model.KhachHang kh = khachHangRepo.findByTaiKhoan(username).orElse(new HUIT.football.model.KhachHang());
        kh.setTaiKhoan(username);
        kh.setTenKhach(tenKhach);
        kh.setSoDienThoai(soDienThoai);

        // Lưu Email vào bảng khach_hang
        if (email != null && email.trim().isEmpty()) {
            kh.setEmail(null);
        } else {
            kh.setEmail(email);
        }
        khachHangRepo.save(kh);

        // 2. Cập nhật bảng User (Email và Mật khẩu)
        HUIT.football.model.User user = userRepository.findByUsername(username).get();

        // Lưu Email vào bảng users
        if (email != null && email.trim().isEmpty()) {
            user.setEmail(null);
        } else {
            user.setEmail(email);
        }

        // Cập nhật Mật khẩu (nếu có nhập)
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(user);

        response.put("success", true);
        response.put("message", "Cập nhật hồ sơ thành công!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/baocao")
    public String customerReportPage() {
        return "baocao_khach"; // Sẽ trỏ tới file baocao_khach.html
    }

    // 2. API trả về tổng chi tiêu và danh sách hóa đơn của khách đang đăng nhập
    @GetMapping("/api/baocao")
    @ResponseBody
    public ResponseEntity<?> getCustomerReport(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }

        String username = principal.getName();

        // Tìm hồ sơ khách hàng dựa trên tên tài khoản đăng nhập
        HUIT.football.model.KhachHang kh = khachHangRepo.findByTaiKhoan(username).orElse(null);
        if (kh == null) {
            return ResponseEntity.ok(Map.of("totalSpent", 0.0, "bills", List.of()));
        }

        // Lấy toàn bộ hóa đơn Đã Thanh Toán của riêng khách hàng này
        List<HUIT.football.model.HoaDon> hoaDons = hoaDonRepo.findByKhachHangAndTrangThai(kh, "Đã Thanh Toán");

        List<Map<String, Object>> billList = new java.util.ArrayList<>();
        double calcTotalSpent = 0.0;
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (HUIT.football.model.HoaDon hd : hoaDons) {
            Map<String, Object> map = new HashMap<>();
            map.put("maHd", hd.getMaHD()); // Khớp chính xác với biến maHD trong HoaDon.java
            map.put("thoiGian", hd.getThoiGianKetThuc() != null ? hd.getThoiGianKetThuc().format(formatter) : "");
            map.put("tenSan", hd.getSanBong() != null ? hd.getSanBong().getTenSan() : "Sân đã xóa");
            map.put("tienSan", hd.getTienSan());
            map.put("tienDichVu", hd.getTienDichVu());
            map.put("tienGiamGia", hd.getTienGiamGia());
            map.put("tongTien", hd.getTongTien());

            calcTotalSpent += (hd.getTongTien() != null) ? hd.getTongTien() : 0.0;
            billList.add(map);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("tenKhach", kh.getTenKhach());
        response.put("totalSpent", calcTotalSpent); // Số tiền tính thực tế dựa trên danh sách hóa đơn
        response.put("bills", billList);

        return ResponseEntity.ok(response);
    }
}