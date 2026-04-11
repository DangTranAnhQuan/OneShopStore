package nhom17.OneShop.pattern.observer;

import nhom17.OneShop.entity.Order;

public interface OrderEventPublisher {
    void registerListener(OrderListener listener);

    void removeListener(OrderListener listener);

    void publishOrderCreatedEvent(Order order);
}

