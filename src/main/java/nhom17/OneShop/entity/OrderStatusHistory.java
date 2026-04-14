package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import nhom17.OneShop.entity.enums.OrderStatus;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "OrderStatusHistories")
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HistoryId")
    private Long historyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "FromStatus")
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "ToStatus")
    private OrderStatus toStatus;

    @Column(name = "ChangedAt")
    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AdminId")
    private User changedBy;

    public OrderStatusHistory() {
        // For JPA
    }

    public OrderStatusHistory(Order order, OrderStatus oldStatus, OrderStatus newStatus, User actor, LocalDateTime changedAt) {
        attachToOrder(order);
        this.fromStatus = oldStatus;
        this.toStatus = newStatus;
        this.changedAt = changedAt != null ? changedAt : LocalDateTime.now();
        this.changedBy = actor;
    }

    void attachToOrder(Order order) {
        this.order = order;
    }
}
