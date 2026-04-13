package nhom17.OneShop.service;

import nhom17.OneShop.entity.enums.OtpPurpose;

public interface OtpService {
    String generateOtpForEmail(String email, OtpPurpose purpose);
    boolean validateOtp(String email, String otp, OtpPurpose purpose);
}