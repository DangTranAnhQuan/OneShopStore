package nhom17.OneShop.service.otp;

import nhom17.OneShop.entity.OTP;
import nhom17.OneShop.entity.enums.OtpPurpose;
import nhom17.OneShop.repository.OTPRepository;
import nhom17.OneShop.service.EmailService;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;

public abstract class AbstractOtpFlow {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OTPRepository otpRepository;
    private final EmailService emailService;

    protected AbstractOtpFlow(OTPRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    // Template Method: validate -> generate -> persist -> send email
    public final String execute(String email) {
        validate(email);
        String otpCode = generateOtpCode();
        OTP otpEntity = createOtpEntity(email, otpCode);
        otpRepository.save(otpEntity);
        emailService.sendOtpEmail(email, otpCode, getPurpose());
        return otpCode;
    }

    public boolean supports(OtpPurpose purpose) {
        return getPurpose() == purpose;
    }

    protected void validate(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
    }

    protected OTP createOtpEntity(String email, String otpCode) {
        return new OTP(otpCode, getPurpose().getValue(), LocalDateTime.now().plusMinutes(getExpiryMinutes()), email, null);
    }

    protected int getExpiryMinutes() {
        return 5;
    }

    protected String generateOtpCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    protected abstract OtpPurpose getPurpose();
}

