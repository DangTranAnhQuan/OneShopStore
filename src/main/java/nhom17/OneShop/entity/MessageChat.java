package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

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

    @Column(name = "SentAt")
    private LocalDateTime sentAt;

    @Column(name = "IsSeen")
    private Boolean seen = false;

    public MessageChat() {
        // For JPA
    }

    public MessageChat(User user, String content) {
        this.user = user;
        this.content = content;
        this.sentAt = LocalDateTime.now();
        this.seen = Boolean.FALSE;
    }

    public MessageChat(SessionChat session, User user, String content) {
        this(user, content);
        attachToSession(session);
    }

    void attachToSession(SessionChat session) {
        this.session = session;
    }
}
