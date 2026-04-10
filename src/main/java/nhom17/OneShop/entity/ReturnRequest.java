package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import nhom17.OneShop.entity.enums.ReturnRequestStatus;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "ReturnRequests")
@Getter
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReturnRequestId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(name = "Reason", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Column(name = "EvidenceImageUrl", length = 500)
    private String evidenceImageUrl;

    @Column(name = "RequestStatus", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private ReturnRequestStatus requestStatus;

    @Column(name = "RequestedAt", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date requestedAt;

    @Column(name = "ProcessedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AdminId")
    private User adminProcessor;

    @Column(name = "AdminNotes", length = 500, columnDefinition = "NVARCHAR(500)")
    private String adminNotes;

    public ReturnRequest() {
        // For JPA
    }

    public ReturnRequest(Order order, User user, String reason, String evidenceImageUrl) {
        this.order = Objects.requireNonNull(order, "Đơn hàng không hợp lệ");
        this.user = Objects.requireNonNull(user, "Người dùng không hợp lệ");
        setReason(reason);
        this.evidenceImageUrl = evidenceImageUrl;
        this.requestStatus = ReturnRequestStatus.PENDING;
        this.requestedAt = new Date();
    }

    public void approve(User admin, String adminNotes) {
        ensurePending();
        this.requestStatus = ReturnRequestStatus.APPROVED;
        this.adminNotes = adminNotes;
        this.adminProcessor = admin;
        this.processedAt = new Date();
    }

    public void reject(User admin, String adminNotes) {
        ensurePending();
        this.requestStatus = ReturnRequestStatus.REJECTED;
        this.adminNotes = adminNotes;
        this.adminProcessor = admin;
        this.processedAt = new Date();
    }

    private void setReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do hoàn trả không được để trống");
        }
        this.reason = reason;
    }

    private void ensurePending() {
        if (!ReturnRequestStatus.PENDING.equals(this.requestStatus)) {
            throw new IllegalStateException("Yêu cầu đã được xử lý, không thể thay đổi trạng thái");
        }
    }
}