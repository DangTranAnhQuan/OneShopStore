package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "AuditLogs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogId")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AdminId")
    private User admin;

    @Column(name = "ActionName", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private String actionName;

    @Column(name = "TargetId", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private String targetId;

    @Column(name = "Status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status;

    @Column(name = "ErrorMessage", length = 500, columnDefinition = "NVARCHAR(500)")
    private String errorMessage;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    public AuditLog(User admin, String actionName, String targetId, String status, String errorMessage) {
        this.admin = admin;
        this.actionName = actionName;
        this.targetId = targetId;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}