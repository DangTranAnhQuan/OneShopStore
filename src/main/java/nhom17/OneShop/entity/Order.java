package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import nhom17.OneShop.entity.enums.OrderStatus;
import nhom17.OneShop.entity.enums.PaymentMethod;
import nhom17.OneShop.entity.enums.PaymentStatus;
import nhom17.OneShop.entity.enums.ShippingMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderId")
    private Long orderId;

    @Column(name = "OrderedAt")
    private LocalDateTime orderedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "OrderStatus")
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "PaymentMethod")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "PaymentStatus")
    private PaymentStatus paymentStatus;

    @Column(name = "Subtotal")
    private BigDecimal subtotal;

    @Column(name = "ShippingFee")
    private BigDecimal shippingFee;

    @Column(name = "TotalAmount")
    private BigDecimal totalAmount;

    @Column(name = "ReceiverName")
    private String receiverName;

    @Column(name = "ReceiverPhone")
    private String receiverPhone;

    @Column(name = "ReceiverAddress")
    private String receiverAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "ShippingMethod")
    private ShippingMethod shippingMethod;

    @Column(name = "Note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VoucherCode")
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AddressId")
    private Address address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistories = new ArrayList<>();

    public Order() {
        // For JPA
    }

    private Order(Builder builder) {
        assignCustomer(builder.user);
        this.address = builder.address;
        this.receiverName = builder.receiverName;
        this.receiverPhone = builder.receiverPhone;
        this.receiverAddress = builder.receiverAddress;
        this.shippingMethod = Objects.requireNonNull(builder.shippingMethod, "Phương thức vận chuyển không hợp lệ");
        this.paymentMethod = Objects.requireNonNull(builder.paymentMethod, "Phương thức thanh toán không hợp lệ");
        this.paymentStatus = PaymentStatus.UNPAID;
        this.orderStatus = OrderStatus.PENDING;
        this.orderedAt = LocalDateTime.now();
        this.voucher = builder.voucher;
        applyShippingFee(builder.shippingFee);
        updateNote(builder.note);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private User user;
        private Address address;
        private String receiverName;
        private String receiverPhone;
        private String receiverAddress;
        private ShippingMethod shippingMethod;
        private PaymentMethod paymentMethod;
        private BigDecimal shippingFee;
        private Voucher voucher;
        private String note;

        private Builder() {
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder address(Address address) {
            this.address = address;
            return this;
        }

        public Builder receiverName(String receiverName) {
            this.receiverName = receiverName;
            return this;
        }

        public Builder receiverPhone(String receiverPhone) {
            this.receiverPhone = receiverPhone;
            return this;
        }

        public Builder receiverAddress(String receiverAddress) {
            this.receiverAddress = receiverAddress;
            return this;
        }

        public Builder shippingMethod(ShippingMethod shippingMethod) {
            this.shippingMethod = shippingMethod;
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder shippingFee(BigDecimal shippingFee) {
            this.shippingFee = shippingFee;
            return this;
        }

        public Builder voucher(Voucher voucher) {
            this.voucher = voucher;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }

    public void assignCustomer(User user) {
        this.user = Objects.requireNonNull(user, "Người dùng không hợp lệ");
    }

    public void addDetail(OrderDetail detail) {
        Objects.requireNonNull(detail, "Chi tiết đơn hàng không hợp lệ");
        if (this.orderDetails.contains(detail)) {
            return;
        }
        detail.attachToOrder(this);
        this.orderDetails.add(detail);
        recalculateSubtotal();
    }

    public void addStatusHistory(OrderStatusHistory history) {
        Objects.requireNonNull(history, "Lịch sử trạng thái không hợp lệ");
        if (this.statusHistories.contains(history)) {
            return;
        }
        history.attachToOrder(this);
        this.statusHistories.add(history);
    }


    public void applyShippingFee(BigDecimal fee) {
        this.shippingFee = Optional.ofNullable(fee).orElse(BigDecimal.ZERO);
        recalculateTotalAmount();
    }

    public Optional<OrderStatusHistory> changeStatusWithHistory(OrderStatus newStatus, User actor) {
        OrderStatus normalizedTargetStatus = Objects.requireNonNull(newStatus, "Trạng thái đơn hàng không hợp lệ");
        if (Objects.equals(this.orderStatus, normalizedTargetStatus)) {
            return Optional.empty();
        }
        OrderStatus oldStatus = this.orderStatus;
        this.orderStatus = normalizedTargetStatus;
        if (actor == null) {
            return Optional.empty();
        }
        OrderStatusHistory history = new OrderStatusHistory(this, oldStatus, this.orderStatus, actor, LocalDateTime.now());
        addStatusHistory(history);
        return Optional.of(history);
    }

    public void changePaymentStatus(PaymentStatus newPaymentStatus) {
        this.paymentStatus = Objects.requireNonNull(newPaymentStatus, "Trạng thái thanh toán không hợp lệ");
    }

    public void confirmPayment(BigDecimal amountPaid) {
        if (amountPaid == null) {
            throw new IllegalArgumentException("Số tiền thanh toán không hợp lệ");
        }
        if (getTotalAmount().compareTo(amountPaid) != 0) {
            throw new IllegalStateException("Số tiền thanh toán không khớp");
        }
        if (!PaymentStatus.UNPAID.equals(paymentStatus)) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái 'Chưa thanh toán'");
        }
        OrderStatus oldStatus = this.orderStatus;
        this.paymentStatus = PaymentStatus.PAID;
        if (OrderStatus.PENDING.equals(oldStatus)) {
            this.orderStatus = OrderStatus.CONFIRMED;
        }
    }

    public void cancelByCustomer(User currentUser) {
        requireOwnership(currentUser);
        ensureStatus(OrderStatus.PENDING, "Chỉ có thể hủy đơn hàng khi ở trạng thái 'Đang xử lý'.");
        this.orderStatus = OrderStatus.CANCELED;
    }

    public void cancelPendingOnline(User currentUser) {
        requireOwnership(currentUser);
        if (!PaymentMethod.VN_PAY.equals(this.paymentMethod)
                || !PaymentStatus.UNPAID.equals(this.paymentStatus)
                || !OrderStatus.PENDING.equals(this.orderStatus)) {
            throw new IllegalStateException("Đơn hàng không phải online chờ thanh toán để hủy");
        }
        this.orderStatus = OrderStatus.CANCELED;
    }

    public void cancelByAdmin(User admin) {
        changeStatusWithHistory(OrderStatus.CANCELED, admin);
    }

    public void approveReturn() {
        ensureStatus(OrderStatus.DELIVERED, "Chỉ có thể duyệt hoàn trả cho đơn hàng 'Đã giao'.");
        this.orderStatus = OrderStatus.RETURNED;
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    public int loyaltyPointsDelta(OrderStatus oldStatus, OrderStatus newStatus) {
        int points = calculateLoyaltyPoints();
        if (points == 0) return 0;

        if (!OrderStatus.DELIVERED.equals(oldStatus) && OrderStatus.DELIVERED.equals(newStatus)) {
            return points;
        }
        if (OrderStatus.DELIVERED.equals(oldStatus) && OrderStatus.RETURNED.equals(newStatus)) {
            return -points;
        }
        return 0;
    }

    public int calculateLoyaltyPoints() {
        BigDecimal subtotalValue = Optional.ofNullable(this.subtotal).orElse(BigDecimal.ZERO);
        if (subtotalValue.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return subtotalValue.divide(new BigDecimal("10000"), 0, RoundingMode.FLOOR).intValue();
    }

    public BigDecimal getTotalAmount() {
        if (this.totalAmount == null) {
            recalculateTotalAmount();
        }
        return this.totalAmount;
    }

    public BigDecimal getSubtotal() {
        if (this.subtotal == null) {
            recalculateSubtotal();
        }
        return this.subtotal;
    }

    @Transient
    public BigDecimal getDiscountAmount() {
        BigDecimal gross = getSubtotal().add(Optional.ofNullable(this.shippingFee).orElse(BigDecimal.ZERO));
        BigDecimal payable = Optional.ofNullable(this.totalAmount).orElse(gross);
        BigDecimal discount = gross.subtract(payable);
        return discount.max(BigDecimal.ZERO);
    }

    public void applyPayableAmount(BigDecimal payableAmount) {
        BigDecimal amount = Objects.requireNonNull(payableAmount, "Số tiền thanh toán không hợp lệ");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền thanh toán không hợp lệ");
        }
        this.totalAmount = amount;
    }

    public void updateNote(String note) {
        if (note == null || note.isBlank()) {
            this.note = null;
            return;
        }
        this.note = note.trim();
    }


    private void ensureStatus(OrderStatus expectedStatus, String messageIfNotMatch) {
        if (!Objects.equals(expectedStatus, this.orderStatus)) {
            throw new IllegalStateException(messageIfNotMatch);
        }
    }

    private void requireOwnership(User currentUser) {
        if (currentUser == null || user == null || !user.getUserId().equals(currentUser.getUserId())) {
            throw new IllegalStateException("Bạn không có quyền thao tác trên đơn hàng này.");
        }
    }

    private void recalculateSubtotal() {
        this.subtotal = orderDetails.stream()
                .map(OrderDetail::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        recalculateTotalAmount();
    }

    private void recalculateTotalAmount() {
        BigDecimal shippingFeeValue = Optional.ofNullable(this.shippingFee).orElse(BigDecimal.ZERO);
        this.totalAmount = getSubtotal().add(shippingFeeValue);
    }

}
