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

    @Autowired
    private HUIT.football.repository.KhuyenMaiRepository khuyenMaiRepo;

    @Autowired
    private HUIT.football.repository.SanBongRepository sanBongRepo;

    @Autowired
    private HUIT.football.repository.HoaDonRepository hoaDonRepo;

    @Autowired
    private HUIT.football.repository.KhachHangRepository khachHangRepo;

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



    @PostMapping("/api/start")
    @ResponseBody
    public ResponseEntity<?> startSession(@RequestParam("maSan") Long maSan,
                                          @RequestParam(value = "maKhach", required = false) Long maKhach) {
        SanBong san = sanBongRepo.findById(maSan).orElse(null);
        if (san != null) {
            san.setTrangThai("Đang Chơi");
            sanBongRepo.save(san);

            HoaDon hd = new HoaDon();
            hd.setSanBong(san);
            hd.setThoiGianBatDau(java.time.LocalDateTime.now());
            hd.setTrangThai("Đang Chơi");

            // LOGIC MỚI: Liên kết Hóa đơn với Khách hàng nếu có chọn
            if (maKhach != null) {
                HUIT.football.model.KhachHang kh = khachHangRepo.findById(maKhach).orElse(null);
                hd.setKhachHang(kh);
            }

            hoaDonRepo.save(hd);

            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Lỗi!"));
    }

    @PostMapping("/api/end")
    @ResponseBody
    public ResponseEntity<?> endSession(@RequestParam("maSan") Long maSan,
                                        @RequestParam(value = "maKm", required = false) Long maKm) { // Nhận thêm tham số maKm
        SanBong san = sanBongRepo.findById(maSan).orElse(null);
        if (san != null) {

            hoaDonRepo.findBySanBongAndTrangThai(san, "Đang Chơi").ifPresent(hd -> {
                java.time.LocalDateTime bayGio = java.time.LocalDateTime.now();
                hd.setThoiGianKetThuc(bayGio);
                hd.setTrangThai("Đã Thanh Toán");

                // 1. TÍNH TIỀN DỊCH VỤ VÀ HOÀN KHO
                List<HUIT.football.model.ChiTietHoaDon> listChiTiet = chiTietRepo.findByHoaDon(hd);
                double tienDichVu = 0.0;
                for (HUIT.football.model.ChiTietHoaDon ct : listChiTiet) {
                    tienDichVu += ct.getThanhTien();
                    HUIT.football.model.MatHang mh = ct.getMatHang();
                    if ("Thuê đồ".equals(mh.getLoaiHang())) {
                        mh.setSoLuongTon(mh.getSoLuongTon() + ct.getSoLuong());
                        matHangRepo.save(mh);
                    }
                }
                hd.setTienDichVu(tienDichVu);

                // 2. TÍNH TIỀN SÂN
                java.time.Duration duration = java.time.Duration.between(hd.getThoiGianBatDau(), bayGio);
                long soPhut = duration.toMinutes();
                if (soPhut < 1) soPhut = 1;
                double tienSan = (soPhut / 60.0) * san.getGia();
                hd.setTienSan(tienSan);

                // 3. TÍNH KHUYẾN MÃI (LOGIC MỚI)
                double tongTruocGiam = tienSan + tienDichVu;
                double tienGiamGia = 0.0;

                // Nếu thu ngân có chọn Khuyến mãi trên giao diện
                if (maKm != null) {
                    HUIT.football.model.KhuyenMai km = khuyenMaiRepo.findById(maKm).orElse(null);
                    if (km != null) {
                        hd.setKhuyenMai(km); // Lưu liên kết vào DB
                        tienGiamGia = tongTruocGiam * (km.getPhanTramGiam() / 100.0); // Tính số tiền được giảm
                    }
                }
                hd.setTienGiamGia(tienGiamGia);

                // 4. CHỐT TỔNG TIỀN SAU GIẢM
                double tongTienSauGiam = tongTruocGiam - tienGiamGia;
                hd.setTongTien(tongTienSauGiam);

                // 5. CỘNG DỒN CHI TIÊU CHO KHÁCH
                if (hd.getKhachHang() != null) {
                    HUIT.football.model.KhachHang kh = hd.getKhachHang();
                    int soHoaDonCu = (kh.getSoHoaDon() != null) ? kh.getSoHoaDon() : 0;
                    double tongChiCu = (kh.getTongChi() != null) ? kh.getTongChi() : 0.0;

                    kh.setSoHoaDon(soHoaDonCu + 1);
                    kh.setTongChi(tongChiCu + tongTienSauGiam); // Lưu số tiền đã trừ khuyến mãi
                    khachHangRepo.save(kh);
                }

                hoaDonRepo.save(hd);
            });

            san.setTrangThai("Trống");
            sanBongRepo.save(san);

            return ResponseEntity.ok(Map.of("success", true, "message", "Thanh toán thành công! Đã áp dụng khuyến mãi (nếu có)."));
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