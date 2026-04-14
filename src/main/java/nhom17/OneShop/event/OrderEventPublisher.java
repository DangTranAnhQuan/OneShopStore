package nhom17.OneShop.event;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.listener.OrderListener;

public interface OrderEventPublisher {
    void registerListener(OrderListener listener);

    void removeListener(OrderListener listener);

    void publishOrderCreatedEvent(Order order);
}


