package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.*;
import nhom17.OneShop.entity.enums.ShippingMethod;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ShippingFees")
public class ShippingFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShippingFeeId")
    private Integer shippingFeeId;

    @Column(name = "PackageName", nullable = false, length = 200)
    private String packageName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CarrierId", nullable = false)
    private ShippingCarrier shippingCarrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "ShippingMethod", nullable = false, length = 50)
    private ShippingMethod shippingMethod;

    @Column(name = "ShippingCost", nullable = false, precision = 18, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "MinDeliveryDays", nullable = false)
    private Integer minDeliveryDays;

    @Column(name = "MaxDeliveryDays", nullable = false)
    private Integer maxDeliveryDays;

    @Column(name = "TimeUnit", nullable = false, length = 20)
    private String timeUnit;

    @OneToMany(mappedBy = "shippingFee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<AppliedProvince> appliedProvinces = new HashSet<>();
}
