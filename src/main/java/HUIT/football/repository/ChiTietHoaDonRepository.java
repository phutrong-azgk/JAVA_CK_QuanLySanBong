package HUIT.football.repository;

import HUIT.football.model.ChiTietHoaDon;
import HUIT.football.model.HoaDon;
import HUIT.football.model.MatHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Long> {
    List<ChiTietHoaDon> findByHoaDon(HoaDon hoaDon);
    Optional<ChiTietHoaDon> findByHoaDonAndMatHang(HoaDon hoaDon, MatHang matHang);
}