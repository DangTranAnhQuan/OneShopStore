package nhom17.OneShop.service;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.dto.adapter.IPaymentWebhookAdapter;

public interface PaymentService {
    Order getOrderForQrPayment(Long orderId);
    String generatePaymentMemo(Long orderId);
    void processIpnPayment(IPaymentWebhookAdapter adapter);
}
