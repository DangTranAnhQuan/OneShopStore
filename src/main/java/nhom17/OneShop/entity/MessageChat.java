package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import nhom17.OneShop.entity.enums.MessageSenderType;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "ChatMessages")
public class MessageChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MessageId")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SessionId", nullable = false)
    private SessionChat session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId")
    private User user;

    @Column(name = "Content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "SenderType")
    private MessageSenderType senderType;

    @Column(name = "SentAt")
    private LocalDateTime sentAt;

    @Column(name = "IsSeen")
    private Boolean seen = false;

    public MessageChat() {
        // For JPA
    }

    public MessageChat(User user, String content, MessageSenderType senderType) {
        this.user = user;
        this.content = content;
        this.senderType = Objects.requireNonNull(senderType, "Loại người gửi không hợp lệ");
        this.sentAt = LocalDateTime.now();
        this.seen = Boolean.FALSE;
    }

    public MessageChat(SessionChat session, User user, String content, MessageSenderType senderType) {
        this(user, content, senderType);
        attachToSession(session);
    }

    void attachToSession(SessionChat session) {
        this.session = Objects.requireNonNull(session, "Phiên chat không hợp lệ");
    }

    void detachFromSession() {
        this.session = null;
    }
}
