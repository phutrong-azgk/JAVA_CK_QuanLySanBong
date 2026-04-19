package HUIT.football.controller;

import HUIT.football.model.*;
import HUIT.football.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired private SanBongRepository sanBongRepo;
    @Autowired private HoaDonRepository hoaDonRepo;
    @Autowired private MatHangRepository matHangRepo;
    @Autowired private ChiTietHoaDonRepository chiTietRepo;

    @GetMapping
    public String orderPage() { return "order"; }

    @GetMapping("/api/active-pitches")
    @ResponseBody
    public List<Map<String, Object>> getActivePitches() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<SanBong> activePitches = sanBongRepo.findByTrangThai("Đang Chơi");

        for (SanBong san : activePitches) {
            hoaDonRepo.findBySanBongAndTrangThai(san, "Đang Chơi").ifPresent(hd -> {
                Map<String, Object> map = new HashMap<>();
                map.put("maSan", san.getMaSan());
                map.put("tenSan", san.getTenSan());
                map.put("maHD", hd.getMaHD());
                list.add(map);
            });
        }
        return list;
    }

    @GetMapping("/api/menu")
    @ResponseBody
    public List<Map<String, Object>> getMenu(@RequestParam("type") String type) {
        List<Map<String, Object>> list = new ArrayList<>();

        // Lấy tất cả kho hàng lên
        for (HUIT.football.model.MatHang item : matHangRepo.findAll()) {

            // THÊM ĐOẠN NÀY: Nếu type khác "ALL" và loại hàng không khớp với type thì bỏ qua, không add vào list
            if (!"ALL".equals(type) && !type.equals(item.getLoaiHang())) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("maMon", item.getMaMon());
            map.put("tenMon", item.getTenMon());
            map.put("donGia", item.getDonGia());
            map.put("soLuongTon", item.getSoLuongTon());
            list.add(map);
        }
        return list;
    }

    @GetMapping("/api/session-orders")
    @ResponseBody
    public Map<String, Object> getSessionOrders(@RequestParam("maHD") Long maHD) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> orders = new ArrayList<>();
        double totalFee = 0.0;

        Optional<HoaDon> optHD = hoaDonRepo.findById(maHD);
        if (optHD.isPresent()) {
            for (ChiTietHoaDon ct : chiTietRepo.findByHoaDon(optHD.get())) {
                Map<String, Object> map = new HashMap<>();
                map.put("maMon", ct.getMatHang().getMaMon());
                map.put("tenMon", ct.getMatHang().getTenMon());
                map.put("donGia", ct.getDonGia());
                map.put("soLuong", ct.getSoLuong());
                map.put("thanhTien", ct.getThanhTien());
                orders.add(map);
                totalFee += ct.getThanhTien();
            }
        }
        response.put("orders", orders);
        response.put("totalFee", totalFee);
        return response;
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<?> addItem(@RequestParam("maHD") Long maHD, @RequestParam("maMon") String maMon, @RequestParam("qty") int qty) {
        HoaDon hd = hoaDonRepo.findById(maHD).orElseThrow();
        MatHang mh = matHangRepo.findById(maMon).orElseThrow();

        if (mh.getSoLuongTon() < qty) return ResponseEntity.ok(Map.of("success", false));

        Optional<ChiTietHoaDon> optCt = chiTietRepo.findByHoaDonAndMatHang(hd, mh);
        if (optCt.isPresent()) {
            ChiTietHoaDon ct = optCt.get();
            ct.setSoLuong(ct.getSoLuong() + qty);
            ct.setThanhTien(ct.getSoLuong() * ct.getDonGia());
            chiTietRepo.save(ct);
        } else {
            ChiTietHoaDon ct = new ChiTietHoaDon();
            ct.setHoaDon(hd);
            ct.setMatHang(mh);
            ct.setSoLuong(qty);
            ct.setDonGia(mh.getDonGia());
            ct.setThanhTien(qty * mh.getDonGia());
            chiTietRepo.save(ct);
        }

        mh.setSoLuongTon(mh.getSoLuongTon() - qty); // Trừ tồn kho
        matHangRepo.save(mh);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/api/update")
    @ResponseBody
    public ResponseEntity<?> updateItem(@RequestParam("maHD") Long maHD, @RequestParam("maMon") String maMon, @RequestParam("qty") int newQty) {
        HoaDon hd = hoaDonRepo.findById(maHD).orElseThrow();
        MatHang mh = matHangRepo.findById(maMon).orElseThrow();
        ChiTietHoaDon ct = chiTietRepo.findByHoaDonAndMatHang(hd, mh).orElseThrow();

        int diff = newQty - ct.getSoLuong();
        if (mh.getSoLuongTon() < diff) return ResponseEntity.ok(Map.of("success", false)); // Không đủ hàng

        ct.setSoLuong(newQty);
        ct.setThanhTien(newQty * ct.getDonGia());
        chiTietRepo.save(ct);

        mh.setSoLuongTon(mh.getSoLuongTon() - diff);
        matHangRepo.save(mh);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/api/remove")
    @ResponseBody
    public ResponseEntity<?> removeItem(@RequestParam("maHD") Long maHD, @RequestParam("maMon") String maMon) {
        HoaDon hd = hoaDonRepo.findById(maHD).orElseThrow();
        MatHang mh = matHangRepo.findById(maMon).orElseThrow();

        chiTietRepo.findByHoaDonAndMatHang(hd, mh).ifPresent(ct -> {
            mh.setSoLuongTon(mh.getSoLuongTon() + ct.getSoLuong()); // Hoàn lại tồn kho
            matHangRepo.save(mh);
            chiTietRepo.delete(ct);
        });

        return ResponseEntity.ok(Map.of("success", true));
    }
}