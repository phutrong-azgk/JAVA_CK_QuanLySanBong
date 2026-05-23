package HUIT.football.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "khuyen_mai")
@Data
public class KhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maKm;

    private String tenKm;
    private Integer phanTramGiam;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String dieuKien;
}