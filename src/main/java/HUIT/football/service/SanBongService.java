package HUIT.football.service;

import HUIT.football.model.SanBong;
import HUIT.football.repository.SanBongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SanBongService {

    @Autowired
    private SanBongRepository sanBongRepository;

    public List<SanBong> getAllSan() {
        return sanBongRepository.findAll();
    }

    public Optional<SanBong> getSanById(Long id) {
        return sanBongRepository.findById(id);
    }

    public SanBong saveSan(SanBong sanBong) {
        return sanBongRepository.save(sanBong);
    }

    public void deleteSan(Long id) {
        sanBongRepository.deleteById(id);
    }
}