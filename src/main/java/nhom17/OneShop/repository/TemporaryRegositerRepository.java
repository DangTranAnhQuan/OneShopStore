package nhom17.OneShop.repository;

import nhom17.OneShop.entity.TemporaryRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TemporaryRegositerRepository extends JpaRepository<TemporaryRegister, Integer> {
    Optional<TemporaryRegister> findByEmail(String email);
    
    @Transactional
    void deleteByEmail(String email);
    
    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}