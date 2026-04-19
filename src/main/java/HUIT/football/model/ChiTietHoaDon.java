package HUIT.football.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chi_tiet_hoa_don")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietHoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hd", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_mon", nullable = false)
    private MatHang matHang;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia", nullable = false)
    private Double donGia;

    @Column(name = "thanh_tien")
    private Double thanhTien;
}