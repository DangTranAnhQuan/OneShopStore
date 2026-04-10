package nhom17.OneShop.service;
import nhom17.OneShop.entity.User;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp, String purpose);

    void sendContactEmail(User user, String subject, String message);
}