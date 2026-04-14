package nhom17.OneShop.service.impl;

import jakarta.annotation.PostConstruct;
import nhom17.OneShop.entity.Address;
import nhom17.OneShop.entity.CartItem;
import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.request.OrderRequest;
import nhom17.OneShop.event.OrderEventPublisher;
import nhom17.OneShop.listener.OrderListener;
import nhom17.OneShop.service.AddressService;
import nhom17.OneShop.service.CartService;
import nhom17.OneShop.service.CheckoutService;
import nhom17.OneShop.service.InventoryService;
import nhom17.OneShop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired
    private CartService cartService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    @Qualifier("clearCartListener")
    private OrderListener clearCartListener;

    @Autowired
    @Qualifier("emailNotificationListener")
    private OrderListener emailNotificationListener;

    @PostConstruct
    public void registerOrderListeners() {
        orderEventPublisher.registerListener(clearCartListener);
        orderEventPublisher.registerListener(emailNotificationListener);
    }

    @Override
    @Transactional
    public Order placeOrder(OrderRequest request, User currentUser) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu đặt hàng không hợp lệ.");
        }
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }

        Address shippingAddress = addressService.getValidatedShippingAddress(request.getAddressId(), currentUser);

        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống.");
        }

        inventoryService.deductStockForCartItems(cartItems);

        Order savedOrder = orderService.createAndSaveOrderForCheckout(
                currentUser,
                shippingAddress,
                cartItems,
                request
        );

        orderEventPublisher.publishOrderCreatedEvent(savedOrder);
        return savedOrder;
    }
}
