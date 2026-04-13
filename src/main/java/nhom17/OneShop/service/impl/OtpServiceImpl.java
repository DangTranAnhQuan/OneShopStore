package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.OTP;
import nhom17.OneShop.entity.enums.OtpPurpose;
import nhom17.OneShop.repository.OTPRepository;
import nhom17.OneShop.service.OtpService;
import nhom17.OneShop.service.otp.AbstractOtpFlow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OtpServiceImpl implements OtpService {

    private final OTPRepository otpRepository;
    private final List<AbstractOtpFlow> otpFlows;

    public OtpServiceImpl(OTPRepository otpRepository, List<AbstractOtpFlow> otpFlows) {
        this.otpRepository = otpRepository;
        this.otpFlows = otpFlows;
    }

    @Override
    @Transactional
    public String generateOtpForEmail(String email, OtpPurpose purpose) {
        return resolveFlow(purpose).execute(email);
    }

    private AbstractOtpFlow resolveFlow(OtpPurpose purpose) {
        return otpFlows.stream()
            .filter(flow -> flow.supports(purpose))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("OtpPurpose không hợp lệ: " + purpose));
    }

    @Override
    @Transactional
    public boolean validateOtp(String email, String otp, OtpPurpose purpose) {
        // Tìm OTP
        OTP otpEntity = otpRepository
            .findByCodeAndPurpose(otp, purpose.getValue())
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