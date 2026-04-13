package nhom17.OneShop.repository;

import nhom17.OneShop.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByAdmin_UserId(Integer adminId);
    List<AuditLog> findByTargetIdOrderByCreatedAtDesc(String targetId);
}
