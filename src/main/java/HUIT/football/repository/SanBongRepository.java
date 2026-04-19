package HUIT.football.repository;

import HUIT.football.model.SanBong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanBongRepository extends JpaRepository<SanBong, Long> {
    List<SanBong> findByTrangThai(String trangThai);
}