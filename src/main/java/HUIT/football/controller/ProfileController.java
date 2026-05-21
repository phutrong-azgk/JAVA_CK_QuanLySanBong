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
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired private UserRepository userRepository;
    @Autowired private KhachHangRepository khachHangRepo;
    @Autowired private PasswordEncoder passwordEncoder;

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
}