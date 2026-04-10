package nhom17.OneShop.repository;

import nhom17.OneShop.entity.MessageChat;
import nhom17.OneShop.entity.enums.MessageSenderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageChatRepository extends JpaRepository<MessageChat, Long> {

    List<MessageChat> findBySession_SessionIdOrderBySentAtAsc(String sessionId);

    long countBySession_SessionIdAndSeenFalseAndSenderType(String sessionId, MessageSenderType senderType);

    @Modifying
    @Query("UPDATE MessageChat t SET t.seen = true WHERE t.session.sessionId = ?1 AND t.senderType = ?2")
    void markAllAsReadBySessionId(String sessionId, MessageSenderType senderType);

    MessageChat findFirstBySession_SessionIdOrderBySentAtDesc(String sessionId);
}
