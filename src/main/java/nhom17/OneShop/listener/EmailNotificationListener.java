package nhom17.OneShop.listener;

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
        try {
            emailService.sendOrderConfirmationEmail(order);
            System.out.println("✅ [Observer] Đã gửi email xác nhận cho đơn hàng #" + order.getOrderId());
        } catch (Exception e) {
            System.err.println("❌ [Observer] Lỗi gửi email! Đơn hàng vẫn được tạo an toàn. Lỗi: " + e.getMessage());
        }
    }
}


