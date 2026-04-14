package nhom17.OneShop.service;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.request.OrderRequest;

public interface CheckoutService {
    Order placeOrder(OrderRequest request, User currentUser);
}