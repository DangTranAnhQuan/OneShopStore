package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import nhom17.OneShop.entity.enums.SessionStatus;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "ChatSessions")
public class SessionChat {

    @Id
    @Column(name = "SessionId", length = 100)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = true)
    private User user;

    @Column(name = "CustomerName", length = 150)
    private String customerName;

    @Column(name = "CustomerEmail", length = 255)
    private String customerEmail;

    @Column(name = "FirstMessageAt")
    private LocalDateTime firstMessageAt;

    @Column(name = "LastMessageAt")
    private LocalDateTime lastMessageAt;

    @Column(name = "Status", length = 20)
    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.OPEN;

    @Column(name = "UnreadCount")
    private Integer unreadCount = 0;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageChat> messages = new ArrayList<>();

    public SessionChat() {
        // For JPA
    }

    public SessionChat(String sessionId, User user) {
        this.sessionId = sessionId;
        this.user = Objects.requireNonNull(user, "Người dùng không hợp lệ");
        this.customerName = user.getFullName();
        this.customerEmail = user.getEmail();
        markStarted();
    }

    public SessionChat(String sessionId, String customerName, String customerEmail) {
        this.sessionId = sessionId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        markStarted();
    }

    public void reopen() {
        this.status = SessionStatus.OPEN;
    }

    public void close() {
        this.status = SessionStatus.CLOSED;
    }

    private void markStarted() {
        LocalDateTime now = LocalDateTime.now();
        this.firstMessageAt = now;
        this.lastMessageAt = now;
        this.unreadCount = 0;
    }

    public void markNewMessage(LocalDateTime time, boolean fromCustomer) {
        this.lastMessageAt = time;
        if (fromCustomer) {
            int currentUnread = this.unreadCount == null ? 0 : this.unreadCount;
            this.unreadCount = currentUnread + 1;
        }
    }

    public void markAllRead() {
        this.unreadCount = 0;
    }

    public void addMessage(MessageChat message) {
        Objects.requireNonNull(message, "Tin nhắn không hợp lệ");
        if (this.messages.contains(message)) {
            return;
        }
        message.attachToSession(this);
        this.messages.add(message);
    }
}