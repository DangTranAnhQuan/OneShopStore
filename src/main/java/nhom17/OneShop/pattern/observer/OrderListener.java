package nhom17.OneShop.pattern.observer;

import nhom17.OneShop.entity.Order;

public interface OrderListener {
    void onOrderCreated(Order order);
}

