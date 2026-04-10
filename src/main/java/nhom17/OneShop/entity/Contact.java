package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import nhom17.OneShop.entity.enums.ContactStatus;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "Contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ContactId")
    private Long contactId;

    @Column(name = "Subject")
    private String subject;

    @Lob
    @Column(name = "Content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private ContactStatus status;

    @Column(name = "SentAt")
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignedStaffId")
    private User assignedStaff;

    public Contact() {
        // For JPA
    }

    public Contact(User user, String subject, String content) {
        this.user = Objects.requireNonNull(user, "Người dùng không hợp lệ");
        this.subject = subject;
        this.content = content;
        this.status = ContactStatus.NEW;
        this.sentAt = LocalDateTime.now();
    }
}
