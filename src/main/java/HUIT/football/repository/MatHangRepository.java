package HUIT.football.repository;

import HUIT.football.model.MatHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatHangRepository extends JpaRepository<MatHang, String> {
    // String là kiểu dữ liệu của khóa chính (maMon)
}