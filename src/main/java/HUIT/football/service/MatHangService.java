package HUIT.football.service;

import HUIT.football.model.MatHang;
import HUIT.football.repository.MatHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MatHangService {

    @Autowired
    private MatHangRepository matHangRepository;

    public List<MatHang> getAll() {
        return matHangRepository.findAll();
    }

    public MatHang save(MatHang matHang) {
        return matHangRepository.save(matHang);
    }

    public void delete(String maMon) {
        matHangRepository.deleteById(maMon);
    }

    public boolean updateStock(String maMon, int quantity, String type) {
        Optional<MatHang> optional = matHangRepository.findById(maMon);
        if (optional.isPresent()) {
            MatHang matHang = optional.get();
            int currentStock = matHang.getSoLuongTon();

            if ("import".equals(type)) {
                matHang.setSoLuongTon(currentStock + quantity);
            } else if ("export".equals(type)) {
                // Kiểm tra xuất kho có vượt quá số lượng hiện tại không
                if (currentStock < quantity) {
                    return false;
                }
                matHang.setSoLuongTon(currentStock - quantity);
            }
            matHangRepository.save(matHang);
            return true;
        }
        return false;
    }
}