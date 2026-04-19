package HUIT.football.controller;

import HUIT.football.model.User;
import HUIT.football.Role;
import HUIT.football.repository.UserRepository;
import HUIT.football.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/taikhoan")
public class TaiKhoanController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Trả về trang giao diện
    @GetMapping
    public String taikhoanPage() {
        return "taikhoan";
    }

    // ==========================================
    // CÁC API TƯƠNG TÁC VỚI CƠ SỞ DỮ LIỆU
    // ==========================================

    @GetMapping("/api/get-all")
    @ResponseBody
    public List<Map<String, Object>> getAllAccounts() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (User u : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("email", u.getEmail());
            // Tránh NullPointerException nếu Role chưa được gán
            map.put("role", u.getRole() != null ? u.getRole().name() : "KHACH");
            responseList.add(map);
        }
        return responseList;
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createAccount(@ModelAttribute User user) {
        Map<String, Object> response = new HashMap<>();

        // Kiểm tra xem username đã tồn tại chưa
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            response.put("success", false);
            response.put("message", "Tên đăng nhập đã tồn tại!");
            return ResponseEntity.ok(response);
        }

        if (user.getEmail() != null && user.getEmail().trim().isEmpty()) {
            user.setEmail(null);
        }

        // Dùng hàm save của UserService để tự động mã hóa mật khẩu trước khi lưu
        userService.save(user);

        response.put("success", true);
        response.put("message", "Tạo tài khoản thành công!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/edit")
    @ResponseBody
    public ResponseEntity<?> editAccount(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("role") String role,
            @RequestParam(value = "password", required = false) String password) {

        Map<String, Object> response = new HashMap<>();
        Optional<User> optUser = userRepository.findByUsername(username);

        if (optUser.isPresent()) {
            User user = optUser.get();

            if (email != null && email.trim().isEmpty()) {
                user.setEmail(null);
            } else {
                user.setEmail(email);
            }

            user.setRole(Role.valueOf(role)); // Gắn Enum Role

            // Nếu người dùng có nhập mật khẩu mới thì mới mã hóa và cập nhật
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userRepository.save(user); // Dùng trực tiếp JPA save để cập nhật

            response.put("success", true);
            response.put("message", "Cập nhật tài khoản thành công!");
        } else {
            response.put("success", false);
            response.put("message", "Không tìm thấy tài khoản để cập nhật!");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/delete")
    @ResponseBody
    public ResponseEntity<?> deleteAccount(@RequestParam("username") String username) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> optUser = userRepository.findByUsername(username);

        if (optUser.isPresent()) {
            userRepository.delete(optUser.get());
            response.put("success", true);
            response.put("message", "Đã xóa tài khoản [" + username + "] thành công!");
        } else {
            response.put("success", false);
            response.put("message", "Không tìm thấy tài khoản cần xóa!");
        }
        return ResponseEntity.ok(response);
    }
}