package nhom17.OneShop.repository;

import nhom17.OneShop.entity.SessionChat;
import nhom17.OneShop.entity.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionChatRepository extends JpaRepository<SessionChat, String> {

    List<SessionChat> findAllByOrderByLastMessageAtDesc();

    List<SessionChat> findByUnreadCountGreaterThanOrderByLastMessageAtDesc(Integer unreadCount);

    Optional<SessionChat> findByUser_UserId(Integer userId);
    Optional<SessionChat> findByCustomerName(String customerName);
    long countByStatus(SessionStatus status);
}
