package nhom17.OneShop.service.decorator;

import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.enums.OtpPurpose;
import nhom17.OneShop.service.EmailService;

public abstract class EmailServiceDecorator implements EmailService {

    protected final EmailService delegate;

    protected EmailServiceDecorator(EmailService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp, OtpPurpose purpose) {
        delegate.sendOtpEmail(toEmail, otp, purpose);
    }

    @Override
    public void sendContactEmail(User user, String subject, String message) {
        delegate.sendContactEmail(user, subject, message);
    }
}

