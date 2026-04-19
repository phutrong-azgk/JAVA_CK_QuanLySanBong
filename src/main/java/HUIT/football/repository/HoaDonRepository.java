package HUIT.football.repository;

import HUIT.football.model.HoaDon;
import HUIT.football.model.SanBong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {
    Optional<HoaDon> findBySanBongAndTrangThai(SanBong sanBong, String trangThai);
}