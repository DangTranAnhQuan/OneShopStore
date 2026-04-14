package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import nhom17.OneShop.entity.enums.ShippingStatus;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "Shipments")
public class Shipping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShipmentId")
    private Long shippingId;

    @Column(name = "TrackingCode")
    private String trackingCode;

    @Column(name = "ShippedAt")
    private LocalDateTime shippedAt;

    @Column(name = "DeliveredAt")
    private LocalDateTime deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private ShippingStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CarrierId", nullable = false)
    private ShippingCarrier carrier;

    public Shipping() {
        // For JPA
    }

    public Shipping(String trackingCode, Order order, ShippingCarrier carrier) {
        this.trackingCode = trackingCode;
        this.order = Objects.requireNonNull(order, "Đơn hàng không hợp lệ");
        this.carrier = Objects.requireNonNull(carrier, "Nhà vận chuyển không hợp lệ");
        this.status = ShippingStatus.CREATED;
        this.shippedAt = LocalDateTime.now();
    }

    public void updateStatus(ShippingStatus newStatus) {
        ShippingStatus targetStatus = Objects.requireNonNull(newStatus, "Trạng thái vận chuyển không hợp lệ");
        if (!Objects.equals(this.status, targetStatus)) {
            this.status = targetStatus;
            if (ShippingStatus.DELIVERED.equals(targetStatus) && this.deliveredAt == null) {
                this.deliveredAt = LocalDateTime.now();
            }
        }
    }

    public void changeCarrier(ShippingCarrier carrier) {
        this.carrier = Objects.requireNonNull(carrier, "Nhà vận chuyển không hợp lệ");
    }

}
