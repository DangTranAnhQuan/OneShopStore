package nhom17.OneShop.service;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.enums.PaymentMethod;
import nhom17.OneShop.entity.enums.ShippingMethod;
import java.math.BigDecimal;

public interface CheckoutService {
    Order placeOrder(Integer addressId,
                     PaymentMethod paymentMethod,
                     BigDecimal shippingFee,
                     ShippingMethod shippingMethod,
                     String appliedCouponCode,
                     String note);
}