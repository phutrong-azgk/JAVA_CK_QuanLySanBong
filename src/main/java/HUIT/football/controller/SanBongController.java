package HUIT.football.controller;

import HUIT.football.model.HoaDon;
import HUIT.football.model.SanBong;
import HUIT.football.service.SanBongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/san")
public class SanBongController {

    @Autowired
    private SanBongService sanBongService;

    // Trả về file HTML giao diện quản lý sân
    @GetMapping
    public String sanBongPage() {
        return "san"; // Trỏ tới src/main/resources/templates/san.html
    }

    // ==========================================
    // CÁC ENDPOINT API DÀNH CHO JQUERY AJAX
    // ==========================================

    @GetMapping("/api/get-all")
    @ResponseBody
    public List<SanBong> getAllSanAPI() {
        return sanBongService.getAllSan();
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createSan(@ModelAttribute SanBong sanBong) {
        sanBongService.saveSan(sanBong);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Thêm sân thành công!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/edit")
    @ResponseBody
    public ResponseEntity<?> editSan(@ModelAttribute SanBong sanBong) {
        sanBongService.saveSan(sanBong);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật sân thành công!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/delete")
    @ResponseBody
    public ResponseEntity<?> deleteSan(@RequestParam("maSan") Long maSan) {
        sanBongService.deleteSan(maSan);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa sân thành công!");
        return ResponseEntity.ok(response);
    }

    @Autowired
    private HUIT.football.repository.SanBongRepository sanBongRepo;
    @Autowired
    private HUIT.football.repository.HoaDonRepository hoaDonRepo;

    @PostMapping("/api/start")
    @ResponseBody
    public ResponseEntity<?> startSession(@RequestParam("maSan") Long maSan) {
        SanBong san = sanBongRepo.findById(maSan).orElse(null);
        if (san != null) {
            san.setTrangThai("Đang Chơi");
            sanBongRepo.save(san);

            HoaDon hd = new HoaDon();
            hd.setSanBong(san);
            hd.setThoiGianBatDau(java.time.LocalDateTime.now());
            hd.setTrangThai("Đang Chơi");
            hoaDonRepo.save(hd);

            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Lỗi!"));
    }

    @PostMapping("/api/end")
    @ResponseBody
    public ResponseEntity<?> endSession(@RequestParam("maSan") Long maSan) {
        SanBong san = sanBongRepo.findById(maSan).orElse(null);
        if (san != null) {

            // Cập nhật hóa đơn
            hoaDonRepo.findBySanBongAndTrangThai(san, "Đang Chơi").ifPresent(hd -> {
                hd.setThoiGianKetThuc(java.time.LocalDateTime.now());
                hd.setTrangThai("Đã Thanh Toán");

                // LOGIC MỚI: HOÀN TRẢ KHO CHO "THUÊ ĐỒ"
                // Lấy tất cả các món đã gọi trong hóa đơn này
                List<HUIT.football.model.ChiTietHoaDon> listChiTiet = chiTietRepo.findByHoaDon(hd);
                for (HUIT.football.model.ChiTietHoaDon ct : listChiTiet) {
                    HUIT.football.model.MatHang mh = ct.getMatHang();
                    // Nếu là đồ thuê thì cộng dồn số lượng về lại kho
                    if ("Thuê đồ".equals(mh.getLoaiHang())) {
                        mh.setSoLuongTon(mh.getSoLuongTon() + ct.getSoLuong());
                        matHangRepo.save(mh); // Lưu lại vào DB
                    }
                }

                hoaDonRepo.save(hd);
            });

            // Trả sân về Trống
            san.setTrangThai("Trống");
            sanBongRepo.save(san);

            return ResponseEntity.ok(Map.of("success", true, "message", "Thanh toán thành công! Các đồ thuê đã được hoàn lại kho."));
        }
        return ResponseEntity.ok(Map.of("success", false));
    }

    @Autowired
    private HUIT.football.repository.ChiTietHoaDonRepository chiTietRepo;

    @Autowired
    private HUIT.football.repository.MatHangRepository matHangRepo;

    @PostMapping("/api/book")
    @ResponseBody
    public ResponseEntity<?> bookSession(@RequestParam("maSan") Long maSan, java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Vui lòng đăng nhập để đặt sân!"));
        }

        SanBong san = sanBongRepo.findById(maSan).orElse(null);
        if (san != null && san.getTrangThai().equals("Trống")) {
            san.setTrangThai("Đặt Trước");
            sanBongRepo.save(san);

            // Tùy chọn: Bạn có thể lưu thêm thông tin ai đặt vào bảng riêng nếu muốn sau này
            return ResponseEntity.ok(Map.of("success", true, "message", "Đặt sân thành công! Vui lòng đến đúng giờ."));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Sân này đã có người đặt hoặc đang chơi!"));
    }

    // Lưu ý: Các API như StartSession, EndSession, Transfer bạn sẽ cần viết thêm
    // logic liên quan tới HoaDon (Hóa Đơn) tương tự cách làm trên.
}