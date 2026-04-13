package nhom17.OneShop.dto.adapter;

import nhom17.OneShop.dto.SepayWebhookDTO;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SepayWebhookAdapter implements IPaymentWebhookAdapter {
    private final SepayWebhookDTO sepayWebhookDTO;
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("DH(\\d+)");

    public SepayWebhookAdapter(SepayWebhookDTO sepayWebhookDTO) {
        this.sepayWebhookDTO = sepayWebhookDTO;
    }

    @Override
    public boolean isValid() {
        return sepayWebhookDTO != null &&
               sepayWebhookDTO.getContent() != null &&
               sepayWebhookDTO.getTransferAmount() != null;
    }

    @Override
    public String extractOrderId() {
        if (sepayWebhookDTO == null || sepayWebhookDTO.getContent() == null) {
            return null;
        }
        Matcher matcher = ORDER_ID_PATTERN.matcher(sepayWebhookDTO.getContent());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public BigDecimal extractAmount() {
        return sepayWebhookDTO != null ? sepayWebhookDTO.getTransferAmount() : null;
    }

    @Override
    public boolean isIncomingTransaction() {
        return sepayWebhookDTO != null && "in".equalsIgnoreCase(sepayWebhookDTO.getTransferType());
    }

    @Override
    public String getGatewayName() {
        return "SEPAY";
    }
}
