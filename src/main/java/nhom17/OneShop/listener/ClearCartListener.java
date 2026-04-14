package nhom17.OneShop.listener;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.enums.PaymentMethod;
import nhom17.OneShop.service.CartService;
import org.springframework.stereotype.Component;

@Component
public class ClearCartListener implements OrderListener {

    private final CartService cartService;

    public ClearCartListener(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void onOrderCreated(Order order) {
        if (PaymentMethod.COD.equals(order.getPaymentMethod())) {
            cartService.clearCart();
        }
    }
}


