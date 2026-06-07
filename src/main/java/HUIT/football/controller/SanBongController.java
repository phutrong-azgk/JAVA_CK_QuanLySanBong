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
import java.util.Optional;

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



    @PostMapping("/api/edit")
    @ResponseBody
    public ResponseEntity<?> editSan(@ModelAttribute SanBong sanBong) {
        // 1. Tìm sân cũ dưới cơ sở dữ liệu lên để đối chiếu
        SanBong existingSan = sanBongRepo.findById(sanBong.getMaSan()).orElse(null);

        if (existingSan != null) {
            existingSan.setTenSan(sanBong.getTenSan());
            existingSan.setGia(sanBong.getGia());

            String trangThaiCu = existingSan.getTrangThai();
            String trangThaiMoi = sanBong.getTrangThai();

            // LOGIC BẢO VỆ: Chỉ cho phép đổi qua lại giữa "Trống" và "Bảo Trì"
            // Nếu trạng thái cũ hoặc mới liên quan đến Đang Chơi/Đặt Trước thì bỏ qua không cho sửa bậy
            if ("Trống".equals(trangThaiCu) || "Bảo Trì".equals(trangThaiCu)) {
                if ("Trống".equals(trangThaiMoi) || "Bảo Trì".equals(trangThaiMoi)) {
                    existingSan.setTrangThai(trangThaiMoi);
                }
            }

            sanBongRepo.save(existingSan);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật sân thành công!"));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Sân không tồn tại!"));
    }
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createSan(@ModelAttribute SanBong sanBong) {
        try {
            // Sân mới tạo mặc định luôn luôn ở trạng thái "Trống"
            sanBong.setTrangThai("Trống");

            sanBongRepo.save(sanBong);

            return ResponseEntity.ok(Map.of("success", true, "message", "Thêm sân mới thành công!"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Lỗi khi thêm sân: " + e.getMessage()));
        }
    }

    @PostMapping("/api/delete")
    @ResponseBody
    public ResponseEntity<?> deleteSan(@RequestParam("maSan") Long maSan) {
        SanBong san = sanBongRepo.findById(maSan).orElse(null);
        if (san != null) {
            san.setDaXoa(true); // Thực hiện Xóa mềm (Đánh dấu là đã xóa)
            san.setTrangThai("Ngừng hoạt động"); // Tùy chọn: Đổi thêm trạng thái cho chắc chắn
            sanBongRepo.save(san); // Lưu lại thay đổi thay vì gọi lệnh xóa

            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa sân thành công!"));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Sân không tồn tại!"));
    }



    @PostMapping("/api/start")
    @ResponseBody
    public ResponseEntity<?> startSession(@RequestParam("maSan") Long maSan,
                                          @RequestParam(value = "maKhach", required = false) Long maKhach) {
        SanBong san = sanBongRepo.findById(maSan).orElse(null);
        if (san != null) {

            // Tìm xem có hóa đơn đặt trước nào của sân này không
            Optional<HoaDon> reservedHd = hoaDonRepo.findAll().stream()
                    .filter(h -> h.getSanBong().getMaSan().equals(maSan) && h.getTrangThai().equals("Đặt Trước"))
                    .findFirst();

            if (reservedHd.isPresent()) {
                HoaDon hd = reservedHd.get();
                hd.setTrangThai("Đang Chơi");
                hd.setThoiGianBatDau(java.time.LocalDateTime.now()); // Reset lại thời gian bắt đầu đá thật
                hoaDonRepo.save(hd);
            } else {
                // Nếu là sân trống hoàn toàn thì tạo mới bình thường
                HoaDon hd = new HoaDon();
                hd.setSanBong(san);
                hd.setThoiGianBatDau(java.time.LocalDateTime.now());
                hd.setTrangThai("Đang Chơi");
                if (maKhach != null) {
                    HUIT.football.model.KhachHang kh = khachHangRepo.findById(maKhach).orElse(null);
                    hd.setKhachHang(kh);
                }
                hoaDonRepo.save(hd);
            }

            san.setTrangThai("Đang Chơi");
            sanBongRepo.save(san);
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Lỗi!"));
    }

    @PostMapping("/api/end")
    @ResponseBody
    public ResponseEntity<?> endSession(@RequestParam("maSan") Long maSan,
                                        @RequestParam(value = "maKm", required = false) Long maKm,
                                        @RequestParam("hinhThuc") String hinhThuc) { // Nhận thêm tham số Hình thức thanh toán
        SanBong san = sanBongRepo.findById(maSan).orElse(null);
        if (san != null) {

            hoaDonRepo.findBySanBongAndTrangThai(san, "Đang Chơi").ifPresent(hd -> {
                java.time.LocalDateTime bayGio = java.time.LocalDateTime.now();
                hd.setThoiGianKetThuc(bayGio);
                hd.setTrangThai("Đã Thanh Toán");

                // LƯU HÌNH THỨC THANH TOÁN VÀO CƠ SỞ DỮ LIỆU
                hd.setHinhThucThanhToan(hinhThuc);

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

                // 3. TÍNH KHUYẾN MÃI
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

            return ResponseEntity.ok(Map.of("success", true, "message", "Thanh toán thành công!"));
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

            String username = principal.getName();
            HUIT.football.model.KhachHang kh = khachHangRepo.findByTaiKhoan(username).orElse(null);

            HoaDon hd = new HoaDon();
            hd.setSanBong(san);
            hd.setKhachHang(kh);
            hd.setThoiGianBatDau(java.time.LocalDateTime.now());
            hd.setTrangThai("Đặt Trước");
            hoaDonRepo.save(hd);

            return ResponseEntity.ok(Map.of("success", true, "message", "Đặt sân thành công! Vui lòng đến đúng giờ."));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Sân này đã có người đặt hoặc đang chơi!"));
    }

    @GetMapping("/api/get-all")
    @ResponseBody
    public List<Map<String, Object>> getAllSanAPI() {
        List<SanBong> sans = sanBongService.getAllSan().stream()
                .filter(s -> s.getDaXoa() == null || !s.getDaXoa())
                .collect(java.util.stream.Collectors.toList());

        List<Map<String, Object>> responseList = new java.util.ArrayList<>();

        for (SanBong s : sans) {
            Map<String, Object> map = new HashMap<>();
            map.put("maSan", s.getMaSan());
            map.put("tenSan", s.getTenSan());
            map.put("gia", s.getGia());
            map.put("trangThai", s.getTrangThai());

            // Tìm hóa đơn đang kích hoạt (Đang Chơi hoặc Đặt Trước) của sân này
            Optional<HoaDon> activeHd = hoaDonRepo.findAll().stream()
                    .filter(hd -> hd.getSanBong().getMaSan().equals(s.getMaSan())
                            && (hd.getTrangThai().equals("Đang Chơi") || hd.getTrangThai().equals("Đặt Trước")))
                    .findFirst();

            if (activeHd.isPresent() && activeHd.get().getKhachHang() != null) {
                HUIT.football.model.KhachHang kh = activeHd.get().getKhachHang();
                map.put("tenKhach", kh.getTenKhach());
                map.put("soDienThoai", kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "Chưa cập nhật");
                map.put("email", kh.getEmail() != null ? kh.getEmail() : "Chưa cập nhật");
            } else {
                map.put("tenKhach", "Khách vãng lai");
                map.put("soDienThoai", "-");
                map.put("email", "-");
            }
            responseList.add(map);
        }
        return responseList;
    }

    @PostMapping("/api/cancel-booking")
    @ResponseBody
    public ResponseEntity<?> cancelBooking(@RequestParam("maSan") Long maSan) {
        SanBong san = sanBongRepo.findById(maSan).orElse(null);

        if (san != null && san.getTrangThai().equals("Đặt Trước")) {
            // 1. Trả sân về trạng thái Trống
            san.setTrangThai("Trống");
            sanBongRepo.save(san);

            // 2. Tìm hóa đơn đang đặt trước và chuyển thành Đã Hủy
            hoaDonRepo.findAll().stream()
                    .filter(h -> h.getSanBong().getMaSan().equals(maSan) && h.getTrangThai().equals("Đặt Trước"))
                    .findFirst()
                    .ifPresent(hd -> {
                        hd.setTrangThai("Đã Hủy"); // Giữ lại lịch sử bị "bom" sân
                        hoaDonRepo.save(hd);
                    });

            return ResponseEntity.ok(Map.of("success", true, "message", "Đã hủy lịch đặt sân!"));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Không thể hủy sân này!"));
    }

    // Lưu ý: Các API như StartSession, EndSession, Transfer bạn sẽ cần viết thêm
    // logic liên quan tới HoaDon (Hóa Đơn) tương tự cách làm trên.
}