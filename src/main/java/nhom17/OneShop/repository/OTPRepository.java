package nhom17.OneShop.repository;

import nhom17.OneShop.entity.OTP;
import nhom17.OneShop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OTPRepository extends JpaRepository<OTP, UUID> {
    Optional<OTP> findByCodeAndPurpose(String code, String purpose);

    Optional<OTP> findByUserAndPurposeAndIsUsedFalseAndExpiresAtAfter(
        User user,
        String purpose,
        LocalDateTime now
    );

    void deleteByUserAndPurpose(User user, String purpose);
}