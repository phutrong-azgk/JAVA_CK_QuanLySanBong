package HUIT.football.repository;

import HUIT.football.model.HoaDon;
import HUIT.football.model.SanBong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {

    Optional<HoaDon> findBySanBongAndTrangThai(SanBong sanBong, String trangThai);

    @Query("SELECT h FROM HoaDon h WHERE h.trangThai = 'Đã Thanh Toán' AND h.thoiGianKetThuc >= :start AND h.thoiGianKetThuc <= :end")
    List<HoaDon> findDoanhThuByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}