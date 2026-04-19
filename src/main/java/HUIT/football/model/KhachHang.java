package HUIT.football.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "khach_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_khach")
    private Long maKhach;

    @Column(name = "ten_khach", length = 100, nullable = false)
    private String tenKhach;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    // THÊM 2 TRƯỜNG MỚI NÀY
    @Column(name = "email", length = 100)
    private String email;

    @NotBlank(message = "Tài khoản liên kết là bắt buộc")
    @Column(name = "tai_khoan", length = 50)
    private String taiKhoan; // Lưu tên đăng nhập của User để liên kết

    @Column(name = "so_hoa_don")
    private Integer soHoaDon = 0;

    @Column(name = "tong_chi")
    private Double tongChi = 0.0;
}