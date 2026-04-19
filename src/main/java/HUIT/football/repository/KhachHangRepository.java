package HUIT.football.repository;

import HUIT.football.model.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    Optional<KhachHang> findByTaiKhoan(String taiKhoan);
}