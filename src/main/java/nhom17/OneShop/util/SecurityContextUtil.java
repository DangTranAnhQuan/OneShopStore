package nhom17.OneShop.util;

import nhom17.OneShop.entity.User;
import nhom17.OneShop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * SINGLETON PATTERN: Được quản lý bởi Spring Container (@Component)
 * Cung cấp duy nhất một điểm truy cập để lấy thông tin User hiện tại từ Security Context,
 * loại bỏ việc lặp lại code ở nhiều Controller và Service.
 */
@Component
public class SecurityContextUtil {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        return getCurrentUserOptional()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng hiện tại trong CSDL hoặc người dùng chưa đăng nhập."));
    }

    public Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }
        
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByEmail(username);
    }
}