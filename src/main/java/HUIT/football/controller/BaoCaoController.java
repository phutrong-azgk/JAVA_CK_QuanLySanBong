package HUIT.football.controller;

import HUIT.football.model.HoaDon;
import HUIT.football.repository.HoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/baocao")
public class BaoCaoController {

    @Autowired
    private HoaDonRepository hoaDonRepo;

    @GetMapping
    public String index() {
        return "baocao";
    }

    // API RÚT DỮ LIỆU THẬT TỪ DATABASE
    @GetMapping("/api/doanh-thu")
    @ResponseBody
    public Map<String, Object> getDoanhThu(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // 1. Nếu không truyền ngày, lấy mặc định từ mùng 1 đầu tháng đến hôm nay
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        // Ép sang LocalDateTime (Bắt đầu từ 00:00:00 ngày startDate đến 23:59:59 ngày endDate)
        LocalDateTime startDT = startDate.atStartOfDay();
        LocalDateTime endDT = endDate.atTime(LocalTime.MAX);

        // 2. Lấy dữ liệu thật từ DB
        List<HoaDon> bills = hoaDonRepo.findDoanhThuByDateRange(startDT, endDT);

        // 3. Tính toán các con số tổng quan
        double totalRevenue = 0;
        for (HoaDon hd : bills) {
            totalRevenue += (hd.getTongTien() != null ? hd.getTongTien() : 0);
        }
        int totalBills = bills.size();
        double averageBill = totalBills > 0 ? totalRevenue / totalBills : 0;

        // 4. Gom nhóm doanh thu theo từng ngày để vẽ Biểu đồ
        Map<LocalDate, Double> dailyMap = bills.stream()
                .filter(b -> b.getThoiGianKetThuc() != null && b.getTongTien() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getThoiGianKetThuc().toLocalDate(), // Nhóm theo ngày
                        Collectors.summingDouble(HoaDon::getTongTien) // Cộng dồn tiền
                ));

        // DÙNG HASHMAP THAY VÌ MAP.OF ĐỂ TRÁNH LỖI ÉP KIỂU
        List<Map<String, Object>> dailyRevenueList = dailyMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", entry.getKey().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    map.put("revenue", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        // 5. Trả về Frontend bằng HashMap
        Map<String, Object> response = new HashMap<>();
        response.put("totalRevenue", totalRevenue);
        response.put("totalBills", totalBills);
        response.put("averageBill", averageBill);
        response.put("dailyRevenue", dailyRevenueList);

        return response;

    }

    // API CUNG CẤP DỮ LIỆU CHO BẢNG DATATABLES
    @GetMapping("/api/hoadon")
    @ResponseBody
    @Transactional
    public List<Map<String, Object>> getAllHoaDon() {
        List<HoaDon> list = hoaDonRepo.findAll();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (HoaDon hd : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("maHD", hd.getMaHD());
            map.put("tenSan", hd.getSanBong() != null ? hd.getSanBong().getTenSan() : "Sân đã xóa");
            map.put("tenKhach", hd.getKhachHang() != null ? hd.getKhachHang().getTenKhach() : "Khách vãng lai");

            map.put("thoiGian", hd.getThoiGianKetThuc() != null ? hd.getThoiGianKetThuc().format(fmt) : "Chưa chốt");
            // Thời gian thô để DataTables sắp xếp cho chuẩn
            map.put("thoiGianRaw", hd.getThoiGianKetThuc() != null ? hd.getThoiGianKetThuc().toString() : "");

            map.put("tongTien", hd.getTongTien() != null ? hd.getTongTien() : 0.0);
            map.put("trangThai", hd.getTrangThai());
            map.put("hinhThuc", hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan() : "-");

            result.add(map);
        }
        return result;
    }
}