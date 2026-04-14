package nhom17.OneShop.listener;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationListener implements OrderListener {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationListener.class);

    private final EmailService emailService;

    public EmailNotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    @Async
    public void onOrderCreated(Order order) {
        try {
            emailService.sendOrderConfirmationEmail(order);
            log.info("[Observer] Da gui email xac nhan cho don hang #{}", order.getOrderId());
        } catch (Exception e) {
            Long orderId = order != null ? order.getOrderId() : null;
            log.error("[Observer] Loi gui email xac nhan cho don hang #{}. Bo qua loi de khong anh huong request.", orderId, e);
        }
    }
}


