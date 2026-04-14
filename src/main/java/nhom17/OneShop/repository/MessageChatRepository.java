package nhom17.OneShop.repository;

import nhom17.OneShop.entity.MessageChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageChatRepository extends JpaRepository<MessageChat, Long> {

    List<MessageChat> findBySession_SessionIdOrderBySentAtAsc(String sessionId);

    @Query("SELECT COUNT(m) FROM MessageChat m WHERE m.session.sessionId = ?1 AND m.seen = false AND (m.user IS NULL OR UPPER(m.user.role.roleName) NOT IN ('ADMIN', 'ROLE_ADMIN'))")
    long countUnreadCustomerMessagesBySessionId(String sessionId);

    @Modifying
    @Query("UPDATE MessageChat t SET t.seen = true WHERE t.session.sessionId = ?1 AND (t.user IS NULL OR UPPER(t.user.role.roleName) NOT IN ('ADMIN', 'ROLE_ADMIN'))")
    void markAllAsReadBySessionId(String sessionId);

    MessageChat findFirstBySession_SessionIdOrderBySentAtDesc(String sessionId);
}
