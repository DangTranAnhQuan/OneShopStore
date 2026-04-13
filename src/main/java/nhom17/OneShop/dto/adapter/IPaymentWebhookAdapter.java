package nhom17.OneShop.dto.adapter;

import java.math.BigDecimal;

public interface IPaymentWebhookAdapter {
    boolean isValid();
    String extractOrderId();
    BigDecimal extractAmount();
    boolean isIncomingTransaction();
    String getGatewayName();
}
