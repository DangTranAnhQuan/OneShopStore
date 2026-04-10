package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import nhom17.OneShop.entity.enums.DiscountType;
import nhom17.OneShop.entity.enums.VoucherStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "Vouchers")
public class Voucher {
    @Id
    @Column(name = "VoucherCode")
    private String voucherCode;

    @Column(name = "CampaignName")
    private String campaignName;

    @Enumerated(EnumType.STRING)
    @Column(name = "DiscountType")
    private DiscountType discountType;

    @Column(name = "Value")
    private BigDecimal value;

    @Column(name = "StartsAt")
    private LocalDateTime startsAt;

    @Column(name = "EndsAt")
    private LocalDateTime endsAt;

    @Column(name = "MinimumOrderAmount")
    private BigDecimal minimumOrderAmount;

    @Column(name = "MaxDiscountAmount")
    private BigDecimal maxDiscountAmount;

    @Column(name = "TotalUsageLimit")
    private Integer totalUsageLimit;

    @Column(name = "PerUserLimit")
    private Integer perUserLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private VoucherStatus status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    public Voucher() {
        // For JPA
    }

    public Voucher(String code, LocalDateTime createdAt) {
        this.voucherCode = Objects.requireNonNull(code, "Mã khuyến mãi không được trống");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public void configure(String campaignName, DiscountType discountType, BigDecimal value,
                          LocalDateTime startsAt, LocalDateTime endsAt, BigDecimal minimumOrderAmount,
                          BigDecimal maxDiscountAmount, Integer totalUsageLimit, Integer perUserLimit, VoucherStatus status) {
        if (startsAt != null && endsAt != null && endsAt.isBefore(startsAt)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        }
        this.campaignName = campaignName;
        this.discountType = discountType;
        this.value = value;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.minimumOrderAmount = minimumOrderAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.totalUsageLimit = totalUsageLimit;
        this.perUserLimit = perUserLimit;
        this.status = status;
    }
}
