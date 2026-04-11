package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.TemporaryRegister;
import nhom17.OneShop.entity.MembershipTier;
import nhom17.OneShop.entity.Role;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.enums.UserStatus;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.*;
import nhom17.OneShop.repository.TemporaryRegositerRepository;
import nhom17.OneShop.request.SignUpRequest;
import nhom17.OneShop.request.UserRequest;
import nhom17.OneShop.service.OtpService;
import nhom17.OneShop.service.StorageService;
import nhom17.OneShop.service.UserService;
import nhom17.OneShop.specification.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MembershipTierRepository membershipTierRepository;
    @Autowired
    private StorageService storageService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TemporaryRegositerRepository temporaryRegisterRepository;

    @Autowired
    private OtpService otpService;

    @Override
    public Page<User> findAll(String keyword, Integer roleId, Integer tierId, UserStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("userId").descending());

        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (StringUtils.hasText(keyword)) {
            spec = spec.and(UserSpecification.hasUsername(keyword));
        }
        if (roleId != null) {
            spec = spec.and(UserSpecification.hasRole(roleId));
        }
        if (tierId != null) {
            spec = spec.and(UserSpecification.hasMembershipTier(tierId));
        }
        if (status != null) {
            spec = spec.and(UserSpecification.hasStatus(status));
        }

        return userRepository.findAll(spec, pageable);
    }

    @Override
    public User findById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với ID: " + id));
    }

    @Override
    @Transactional
    public void save(UserRequest userRequest) {
        validateUniqueFields(userRequest);
        User user = prepareUserEntity(userRequest);
        String oldAvatar = user.getAvatarUrl();
        mapRequestToEntity(userRequest, user);
        userRepository.save(user);

        if (StringUtils.hasText(userRequest.getAvatarUrl()) && StringUtils.hasText(oldAvatar) && !oldAvatar.equals(userRequest.getAvatarUrl())) {
            storageService.deleteFile(oldAvatar);
        }
    }

    private void validateUniqueFields(UserRequest request) {
        if (request.getUserId() == null) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new DuplicateRecordException("Email '" + request.getEmail() + "' đã được sử dụng.");
            }
            if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
                throw new DuplicateRecordException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại.");
            }
        } else {
            if (userRepository.existsByEmailIgnoreCaseAndUserIdNot(request.getEmail(), request.getUserId())) {
                throw new DuplicateRecordException("Email '" + request.getEmail() + "' đã được người dùng khác sử dụng.");
            }
            if (userRepository.existsByUsernameIgnoreCaseAndUserIdNot(request.getUsername(), request.getUserId())) {
                throw new DuplicateRecordException("Tên đăng nhập '" + request.getUsername() + "' đã được người dùng khác sử dụng.");
            }
        }
    }

    private User prepareUserEntity(UserRequest userRequest) {
        if (userRequest.getUserId() != null) {
            return findById(userRequest.getUserId());
        }
        return new User();
    }

    private void mapRequestToEntity(UserRequest request, User user) {
        user.updateProfile(request.getFullName(), request.getEmail(), request.getUsername(), request.getPhoneNumber(), request.getStatus());
        if (StringUtils.hasText(request.getAvatarUrl())) {
            user.changeAvatar(request.getAvatarUrl());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setEncodedPassword(passwordEncoder.encode(request.getPassword()));
        }
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new NotFoundException("Vai trò không hợp lệ với ID: " + request.getRoleId()));
        user.assignRole(role);
        if (request.getMembershipTierId() != null) {
            MembershipTier tier = membershipTierRepository.findById(request.getMembershipTierId())
                    .orElseThrow(() -> new NotFoundException("Hạng thành viên không hợp lệ với ID: " + request.getMembershipTierId()));
            user.assignMembership(tier);
        } else {
            user.clearMembership();
        }
    }

    @Override
    @Transactional(noRollbackFor = DataIntegrityViolationException.class)
    public void delete(int id) {
        User userToDelete = findById(id);

        if (orderRepository.existsByUser_UserId(id)) {
            userToDelete.changeStatus(UserStatus.INACTIVE);
            userRepository.save(userToDelete);
            throw new DataIntegrityViolationException("Không thể xóa người dùng '" + userToDelete.getFullName() + "' vì đã có lịch sử đặt hàng. Tài khoản đã được chuyển sang trạng thái 'Khóa'.");
        }

        if (StringUtils.hasText(userToDelete.getAvatarUrl())) {
            storageService.deleteFile(userToDelete.getAvatarUrl());
        }
        userRepository.delete(userToDelete);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    @Override
    @Transactional
    public User registerNewUser(SignUpRequest signUpRequest) {
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được đăng ký: " + signUpRequest.getEmail());
        }

        if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại: " + signUpRequest.getUsername());
        }

        temporaryRegisterRepository.deleteByEmail(signUpRequest.getEmail());

        TemporaryRegister temporaryRegister = new TemporaryRegister(
                signUpRequest.getEmail(),
                signUpRequest.getUsername(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFullName(),
                LocalDateTime.now().plusMinutes(30)
        );

        temporaryRegisterRepository.save(temporaryRegister);

        otpService.generateOtpForEmail(signUpRequest.getEmail(), "Đăng ký");

        return null;
    }

    @Override
    @Transactional
    public boolean verifyEmailOtp(String email, String otp) {
        boolean isValid = otpService.validateOtp(email, otp, "Đăng ký");

        if (!isValid) {
            return false;
        }

        TemporaryRegister temporaryRegister = temporaryRegisterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký cho email này."));

        if (temporaryRegister.isExpired()) {
            temporaryRegisterRepository.delete(temporaryRegister);
            throw new RuntimeException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
        }

        Role userRole = roleRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò mặc định cho người dùng."));

        User newUser = new User(temporaryRegister.getFullName(), temporaryRegister.getEmail(), temporaryRegister.getUsername(), temporaryRegister.getPassword(), userRole);
        newUser.changeStatus(UserStatus.ACTIVE);
        newUser.markEmailVerified();

        userRepository.save(newUser);

        temporaryRegisterRepository.delete(temporaryRegister);

        return true;
    }

    @Override
    @Transactional
    public void sendResetPasswordOtp(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));

        otpService.generateOtpForEmail(email, "Quên mật khẩu");
    }

    @Override
    @Transactional
    public boolean verifyResetPasswordOtp(String email, String otp) {
        return otpService.validateOtp(email, otp, "Quên mật khẩu");
    }

    // ✅ PHƯƠNG THỨC QUAN TRỌNG NHẤT - ĐÃ SỬA
    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        System.out.println("========== BẮT ĐẦU RESET PASSWORD ==========");
        System.out.println("📧 Email: " + email);
        System.out.println("🔑 Mật khẩu mới (raw): " + newPassword);
        
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

            System.out.println("✅ Tìm thấy user ID: " + user.getUserId());
            System.out.println("👤 Username: " + user.getUsername());
            System.out.println("🔐 Mật khẩu CŨ (30 ký tự đầu): " + user.getPassword().substring(0, 30) + "...");
            String encodedPassword = passwordEncoder.encode(newPassword);
            System.out.println("🔐 Mật khẩu MỚI đã mã hóa (30 ký tự đầu): " + encodedPassword.substring(0, 30) + "...");

            user.updatePasswordAndTimestamp(encodedPassword);

            System.out.println("📝 Đã set: MatKhau + NgayCapNhat");

            User savedUser = userRepository.save(user);
            System.out.println("💾 Đã gọi userRepository.save()");

            userRepository.flush();
            System.out.println("✅ Đã flush vào database");

            User reloadedUser = userRepository.findById(user.getUserId()).orElse(null);
            if (reloadedUser != null) {
                System.out.println("🔄 Reload user từ DB - Mật khẩu (30 ký tự đầu): " + reloadedUser.getPassword().substring(0, 30) + "...");
                System.out.println("📅 NgayCapNhat: " + reloadedUser.getUpdatedAt());
            }

            System.out.println("========== KẾT THÚC RESET PASSWORD ==========");

        } catch (Exception e) {
            System.err.println("❌ LỖI KHI RESET PASSWORD: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}