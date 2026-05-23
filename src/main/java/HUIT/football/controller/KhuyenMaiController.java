package HUIT.football.controller;

import HUIT.football.model.KhuyenMai;
import HUIT.football.repository.KhuyenMaiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/khuyenmai")
public class KhuyenMaiController {

    @Autowired
    private KhuyenMaiRepository khuyenMaiRepo;

    @GetMapping
    public String index() {
        return "khuyenmai";
    }

    @GetMapping("/api/get-all")
    @ResponseBody
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(khuyenMaiRepo.findAll());
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> create(@ModelAttribute KhuyenMai khuyenMai) {
        khuyenMaiRepo.save(khuyenMai);
        return ResponseEntity.ok(Map.of("success", true, "message", "Thêm khuyến mãi thành công!"));
    }

    @PostMapping("/api/edit")
    @ResponseBody
    public ResponseEntity<?> edit(@ModelAttribute KhuyenMai khuyenMai) {
        khuyenMaiRepo.save(khuyenMai);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật khuyến mãi thành công!"));
    }

    @PostMapping("/api/delete")
    @ResponseBody
    public ResponseEntity<?> delete(@RequestParam("maKM") Long maKM) {
        khuyenMaiRepo.deleteById(maKM);
        return ResponseEntity.ok(Map.of("success", true, "message", "Xóa khuyến mãi thành công!"));
    }
}