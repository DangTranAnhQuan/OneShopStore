package nhom17.OneShop.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {
    private Integer addressId;
    private String paymentMethod;
    private String shippingMethod;
    private String appliedCouponCode;
    private BigDecimal shippingFee;
    private String note;
}

