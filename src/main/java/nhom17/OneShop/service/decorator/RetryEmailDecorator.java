package nhom17.OneShop.service.decorator;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.enums.OtpPurpose;
import nhom17.OneShop.service.EmailService;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RetryEmailDecorator extends EmailServiceDecorator {

    private final int maxRetries;
    private final int maxSendPerWindow;
    private final long limitWindowMillis;
    private final ConcurrentMap<String, SendWindowCounter> otpSendCounters = new ConcurrentHashMap<>();

    public RetryEmailDecorator(EmailService delegate, int maxRetries, int maxSendPerWindow, long limitWindowMinutes) {
        super(delegate);
        this.maxRetries = Math.max(0, maxRetries);
        this.maxSendPerWindow = Math.max(1, maxSendPerWindow);
        this.limitWindowMillis = Math.max(1L, limitWindowMinutes) * 60_000L;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp, OtpPurpose purpose) {
        enforceSendLimit(toEmail, purpose);

        RuntimeException lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                super.sendOtpEmail(toEmail, otp, purpose);
                return;
            } catch (RuntimeException ex) {
                lastException = ex;
            }
        }

        throw new RuntimeException("Không thể gửi email OTP sau nhiều lần thử lại.", lastException);
    }

    @Override
    public void sendOrderConfirmationEmail(Order order) {
        RuntimeException lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                super.sendOrderConfirmationEmail(order);
                return;
            } catch (RuntimeException ex) {
                lastException = ex;
            }
        }

        throw new RuntimeException("Không thể gửi email xác nhận đơn hàng sau nhiều lần thử lại.", lastException);
    }

    private void enforceSendLimit(String toEmail, OtpPurpose purpose) {
        String key = (toEmail + "|" + purpose.getValue()).toLowerCase(Locale.ROOT);
        long now = System.currentTimeMillis();
        SendWindowCounter counter = otpSendCounters.computeIfAbsent(key, ignored -> new SendWindowCounter(now));

        boolean allowed = counter.tryAcquire(now, limitWindowMillis, maxSendPerWindow);
        if (!allowed) {
            throw new RuntimeException("Bạn đã yêu cầu gửi OTP quá nhiều lần. Vui lòng thử lại sau.");
        }
    }

    private static final class SendWindowCounter {
        private long windowStartMillis;
        private int count;

        private SendWindowCounter(long now) {
            this.windowStartMillis = now;
            this.count = 0;
        }

        private synchronized boolean tryAcquire(long now, long windowMillis, int maxSendPerWindow) {
            if (now - windowStartMillis >= windowMillis) {
                windowStartMillis = now;
                count = 0;
            }

            if (count >= maxSendPerWindow) {
                return false;
            }

            count++;
            return true;
        }
    }
}

