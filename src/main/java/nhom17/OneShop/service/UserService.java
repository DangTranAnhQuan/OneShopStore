package nhom17.OneShop.service;

import nhom17.OneShop.entity.User;
import nhom17.OneShop.request.UserRequest;
import org.springframework.data.domain.Page;
import nhom17.OneShop.request.SignUpRequest;
import nhom17.OneShop.entity.enums.UserStatus;

public interface UserService {
    Page<User> findAll(String keyword, Integer roleId, Integer tierId, UserStatus status, int page, int size);
    User findById(int id);
    void save(UserRequest userRequest);
    void delete(int id);

    User registerNewUser(SignUpRequest signUpRequest);
    boolean verifyEmailOtp(String email, String otp);
    User findByEmail(String email);

    void sendResetPasswordOtp(String email);
    boolean verifyResetPasswordOtp(String email, String otp);
    void resetPassword(String email, String newPassword);
}