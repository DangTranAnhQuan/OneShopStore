package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "MembershipTiers")
public class MembershipTier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TierId")
    private Integer tierId;

    @Column(name = "TierName")
    private String tierName;

    @Column(name = "MinPoints")
    private Integer minPoints;

    @Column(name = "DiscountPercentage")
    private BigDecimal discountPercentage;

    public MembershipTier() {
        // For JPA
    }

    public MembershipTier(String tierName, Integer minPoints, BigDecimal discountPercentage) {
        update(tierName, minPoints, discountPercentage);
    }

    public void update(String tierName, Integer minPoints, BigDecimal discountPercentage) {
        this.tierName = tierName;
        this.minPoints = minPoints;
        this.discountPercentage = discountPercentage;
    }
}
