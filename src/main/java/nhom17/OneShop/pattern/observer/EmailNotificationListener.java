package nhom17.OneShop.pattern.observer;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.service.EmailService;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationListener implements OrderListener {

    private final EmailService emailService;

    public EmailNotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void onOrderCreated(Order order) {
        emailService.sendOrderConfirmationEmail(order);
    }
}

