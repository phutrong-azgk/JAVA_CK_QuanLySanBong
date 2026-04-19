package HUIT.football.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mat_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatHang {

    @Id
    @Column(name = "ma_mon", length = 20, nullable = false)
    private String maMon; // Dùng String để có thể đặt mã như "NUOC01", "AOBIB01"

    @Column(name = "ten_mon", length = 100, nullable = false)
    private String tenMon;

    @Column(name = "don_gia", nullable = false)
    private Double donGia;

    @Column(name = "so_luong_ton", nullable = false)
    private Integer soLuongTon;

    @Column(name = "loai_hang", length = 20)
    private String loaiHang; // Đồ ăn, Đồ uống, Thuê đồ
}
