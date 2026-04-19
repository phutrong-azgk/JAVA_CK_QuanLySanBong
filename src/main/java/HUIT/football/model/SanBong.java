package HUIT.football.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "san_bong")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanBong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_san")
    private Long maSan;

    @Column(name = "ten_san", length = 50, nullable = false)
    private String tenSan; // VD: Sân 1, Sân 2

    @Column(name = "gia_mot_gio", nullable = false)
    private Double gia; // Giá mỗi giờ đá

    @Column(name = "trang_thai", length = 20)
    private String trangThai; // Trống, Đang Chơi, Đặt Trước
}
