package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.OTP;
import nhom17.OneShop.repository.OTPRepository;
import nhom17.OneShop.service.EmailService;
import nhom17.OneShop.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpServiceImpl implements OtpService {

    @Autowired
    private OTPRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public String generateOtpForEmail(String email, String purpose) {
        // Tạo mã OTP 6 số ngẫu nhiên
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Lưu vào database
        OTP otpEntity = new OTP(otp, purpose, LocalDateTime.now().plusMinutes(5), email, null);
        otpRepository.save(otpEntity);

        // Gửi email
        emailService.sendOtpEmail(email, otp, purpose);

        return otp;
    }

    @Override
    @Transactional
    public boolean validateOtp(String email, String otp, String purpose) {
        // Tìm OTP
        OTP otpEntity = otpRepository
            .findByCodeAndPurpose(otp, purpose)
            .orElse(null);

        if (otpEntity == null) {
            return false;
        }

        // Kiểm tra email khớp
        if (!email.equals(otpEntity.getEmail())) {
            return false;
        }

        // Kiểm tra hết hạn
        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Kiểm tra đã sử dụng
        if (otpEntity.isUsed()) {
            return false;
        }

        // Đánh dấu đã sử dụng
        otpEntity.markUsed();
        otpRepository.save(otpEntity);

        return true;
    }
}