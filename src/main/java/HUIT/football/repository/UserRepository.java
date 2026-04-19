package HUIT.football.repository;

import HUIT.football.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Security will use this method to find a user during login
    Optional<User> findByUsername(String username);
}