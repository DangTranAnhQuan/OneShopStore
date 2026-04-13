package nhom17.OneShop.pattern.observer;

import nhom17.OneShop.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DefaultOrderEventPublisher implements OrderEventPublisher {

    private final List<OrderListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void registerListener(OrderListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(OrderListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void publishOrderCreatedEvent(Order order) {
        Objects.requireNonNull(order, "Order must not be null");
        for (OrderListener listener : listeners) {
            listener.onOrderCreated(order);
        }
    }
}

