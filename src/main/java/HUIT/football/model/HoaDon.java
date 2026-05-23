package HUIT.football.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hoa_don")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_hd")
    private Long maHD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san", nullable = false)
    private SanBong sanBong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khach")
    private KhachHang khachHang; // Có thể null nếu khách lẻ không lưu tên

    @Column(name = "thoi_gian_bat_dau")
    private LocalDateTime thoiGianBatDau;

    @Column(name = "thoi_gian_ket_thuc")
    private LocalDateTime thoiGianKetThuc;

    @Column(name = "tien_san")
    private Double tienSan = 0.0;

    @Column(name = "tien_dich_vu")
    private Double tienDichVu = 0.0;

    @Column(name = "tong_tien")
    private Double tongTien = 0.0;

    @Column(name = "trang_thai")
    private String trangThai; // Đang Chơi, Đã Thanh Toán
    // Nối với bảng Khuyến Mãi (Nếu lỗi import, hãy trỏ chuột và ấn Alt+Enter)
    @ManyToOne
    @JoinColumn(name = "ma_km")
    private KhuyenMai khuyenMai;

    // Lưu lại số tiền đã được giảm
    @Column(name = "tien_giam_gia")
    private Double tienGiamGia = 0.0;
}
