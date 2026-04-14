package nhom17.OneShop.service;


import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.enums.OtpPurpose;
import nhom17.OneShop.entity.User;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp, OtpPurpose purpose);

    void sendContactEmail(User user, String subject, String message);

    void sendOrderConfirmationEmail(Order order);
}