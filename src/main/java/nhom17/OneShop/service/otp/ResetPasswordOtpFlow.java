package nhom17.OneShop.service.otp;

import nhom17.OneShop.entity.enums.OtpPurpose;
import nhom17.OneShop.repository.OTPRepository;
import nhom17.OneShop.service.EmailService;
import org.springframework.stereotype.Component;

@Component
public class ResetPasswordOtpFlow extends AbstractOtpFlow {

    public ResetPasswordOtpFlow(OTPRepository otpRepository, EmailService emailService) {
        super(otpRepository, emailService);
    }

    @Override
    protected OtpPurpose getPurpose() {
        return OtpPurpose.RESET_PASSWORD;
    }
}

