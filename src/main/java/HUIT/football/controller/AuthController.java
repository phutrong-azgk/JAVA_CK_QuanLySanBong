package HUIT.football.controller;

import HUIT.football.model.User;
import HUIT.football.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Gọi file login.html
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Gọi file register.html
    }

    @PostMapping("/register")
    public String registerUser(@Valid User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        // Kiểm tra xem username đã tồn tại chưa
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "register";
        }

        user.setRole(HUIT.football.Role.KHACH); // Set quyền mặc định
        userService.save(user);

        return "redirect:/login?success"; // Chuyển về login kèm thông báo thành công
    }
}