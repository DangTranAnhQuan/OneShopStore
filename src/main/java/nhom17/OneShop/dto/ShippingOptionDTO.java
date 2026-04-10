package nhom17.OneShop.dto;

import lombok.Data;
import nhom17.OneShop.entity.enums.ShippingMethod;

import java.math.BigDecimal;

@Data
public class ShippingOptionDTO {
    private Integer shippingFeeId;
    private String packageName;
    private ShippingMethod shippingMethod;
    private String shippingMethodLabel;
    private BigDecimal feeAmount;
    private Integer minDeliveryDays;
    private Integer maxDeliveryDays;
    private String timeUnit;
    private String carrierName;
}