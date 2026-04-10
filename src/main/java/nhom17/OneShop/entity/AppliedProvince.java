package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AppliedProvinces")
public class AppliedProvince {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AppliedProvinceId")
    private Integer id;

    @Column(name = "ProvinceName", nullable = false, length = 100)
    private String provinceName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShippingFeeId", nullable = false)
    private ShippingFee shippingFee;
}
