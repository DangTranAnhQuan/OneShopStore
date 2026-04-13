package nhom17.OneShop.service.decorator;

import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.enums.OtpPurpose;
import nhom17.OneShop.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEmailDecorator extends EmailServiceDecorator {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailDecorator.class);

    public LoggingEmailDecorator(EmailService delegate) {
        super(delegate);
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp, OtpPurpose purpose) {
        log.info("[Email] Sending OTP email to={} purpose={}", toEmail, purpose);
        super.sendOtpEmail(toEmail, otp, purpose);
        log.info("[Email] OTP email sent to={} purpose={}", toEmail, purpose);
    }

    @Override
    public void sendContactEmail(User user, String subject, String message) {
        String sender = user != null ? user.getEmail() : "unknown";
        log.info("[Email] Sending contact email from={} subject={}", sender, subject);
        super.sendContactEmail(user, subject, message);
        log.info("[Email] Contact email sent from={} subject={}", sender, subject);
    }
}

