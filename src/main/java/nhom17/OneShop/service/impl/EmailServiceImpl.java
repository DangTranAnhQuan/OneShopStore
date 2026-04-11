package nhom17.OneShop.service.impl;

import jakarta.mail.internet.MimeMessage;
import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from-address}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Override
    public void sendOtpEmail(String toEmail, String otp, String purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            
            if (purpose.equals("Đăng ký")) {
                message.setSubject("Xác thực tài khoản OneShop");
                message.setText(
                    "Xin chào,\n\n" +
                    "Cảm ơn bạn đã đăng ký tài khoản tại OneShop!\n\n" +
                    "Mã OTP của bạn là: " + otp + "\n\n" +
                    "Mã này sẽ hết hạn sau 5 phút.\n\n" +
                    "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n" +
                    "Trân trọng,\n" +
                    "OneShop Team"
                );
            } else if (purpose.equals("Quên mật khẩu")) {
                message.setSubject("Khôi phục mật khẩu OneShop");
                message.setText(
                    "Xin chào,\n\n" +
                    "Bạn đã yêu cầu khôi phục mật khẩu tài khoản OneShop.\n\n" +
                    "Mã OTP của bạn là: " + otp + "\n\n" +
                    "Mã này sẽ hết hạn sau 5 phút.\n\n" +
                    "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n" +
                    "Trân trọng,\n" +
                    "OneShop Team"
                );
            }
            
            mailSender.send(message);
            System.out.println("✅ Đã gửi email OTP đến: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }

    @Override
    public void sendContactEmail(User user, String subject, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(this.fromAddress);
            helper.setFrom(this.fromAddress, this.fromName + " - Khách Liên Hệ");

            helper.setReplyTo(user.getEmail());

            helper.setSubject("[Liên Hệ OneShop] - " + subject);

            String htmlContent = "<html><body>"
                    + "<h3>Bạn có một tin nhắn liên hệ mới:</h3>"
                    + "<p><strong>Khách hàng:</strong> " + user.getFullName() + "</p>"
                    + "<p><strong>Email:</strong> " + user.getEmail() + "</p>"
                    + "<p><strong>Số điện thoại:</strong> " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "Chưa cập nhật") + "</p>"
                    + "<p><strong>Chủ đề:</strong> " + subject + "</p>"
                    + "<hr>"
                    + "<p><strong>Nội dung:</strong></p>"
                    + "<p style=\"white-space: pre-wrap;\">" + message + "</p>"
                    + "</body></html>";

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Đã gửi email LIÊN HỆ đến shop từ: " + user.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email LIÊN HỆ: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email liên hệ: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendOrderConfirmationEmail(Order order) {
        if (order == null || order.getUser() == null || order.getUser().getEmail() == null) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(order.getUser().getEmail());
            message.setSubject("Xác nhận đơn hàng #" + order.getOrderId());
            message.setText(
                    "Xin chào " + order.getUser().getFullName() + ",\n\n"
                            + "Đơn hàng #" + order.getOrderId() + " của bạn đã được tạo thành công.\n"
                            + "Tổng tiền: " + order.getTotalAmount() + "\n"
                            + "Phương thức thanh toán: " + order.getPaymentMethod() + "\n\n"
                            + "Cảm ơn bạn đã mua sắm tại OneShop."
            );
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email xác nhận đơn hàng.", e);
        }
    }
}