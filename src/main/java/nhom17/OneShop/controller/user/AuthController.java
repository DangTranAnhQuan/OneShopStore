package nhom17.OneShop.controller.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import nhom17.OneShop.config.JwtUtil;
import nhom17.OneShop.entity.enums.OtpPurpose;
import nhom17.OneShop.request.SignUpRequest;
import nhom17.OneShop.service.OtpService;
import nhom17.OneShop.service.UserService;
import nhom17.OneShop.service.impl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/sign-in")
    public String showSignInForm() {
        return "user/account/sign-in";
    }

    @GetMapping("/sign-up")
    public String showSignUpForm(Model model) {
        model.addAttribute("user", new SignUpRequest());
        return "user/account/sign-up";
    }

    @PostMapping("/sign-up")
    public String processSignUp(@ModelAttribute("user") SignUpRequest signUpRequest,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            userService.registerNewUser(signUpRequest);
            model.addAttribute("email", signUpRequest.getEmail());
            return "user/account/verify-otp-simple";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/sign-up";
        }
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("email") String email,
                            @RequestParam("otp") String otp,
                            RedirectAttributes redirectAttributes) {
        try {
            boolean isValid = userService.verifyEmailOtp(email, otp);
            if (isValid) {
                redirectAttributes.addFlashAttribute("success", "Xác thực thành công! Vui lòng đăng nhập.");
                return "redirect:/sign-in";
            } else {
                redirectAttributes.addFlashAttribute("error", "Mã OTP không đúng hoặc đã hết hạn!");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/verify-otp?email=" + email;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/verify-otp?email=" + email;
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "user/account/verify-otp-simple";
    }

    @GetMapping("/resend-otp")
    public String resendOtp(@RequestParam("email") String email,
                            RedirectAttributes redirectAttributes) {
        try {
            otpService.generateOtpForEmail(email, OtpPurpose.SIGN_UP);
            redirectAttributes.addFlashAttribute("success", "Đã gửi lại mã OTP. Vui lòng kiểm tra email!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể gửi lại OTP. Vui lòng thử lại!");
        }
        return "redirect:/verify-otp?email=" + email;
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String email,
                               @RequestParam("password") String password,
                               HttpServletResponse response,
                               RedirectAttributes redirectAttributes) {

        try {
            // 1. Xác thực người dùng
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            // 2. Nếu xác thực thành công, tải UserDetails
            final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            // 3. Tạo JWT Token
            final String token = jwtUtil.generateToken(userDetails);
            // 4. Tạo Cookie chứa JWT
            Cookie jwtCookie = jwtUtil.createJwtCookie(token);
            // 5. Thêm cookie vào response
            response.addCookie(jwtCookie);
            // 6. Logic điều hướng
            for (GrantedAuthority auth : userDetails.getAuthorities()) {
                if (isAdminAuthority(auth.getAuthority())) {
                    return "redirect:/admin/dashboard";
                }
            }
            return "redirect:/";

        } catch (DisabledException e) {
            // Xử lý tài khoản bị khóa
            redirectAttributes.addFlashAttribute("error", "Tài khoản của bạn đã bị khóa.");
            return "redirect:/sign-in?disabled=true";

        } catch (Exception e) {
            // Xử lý sai username/password
            redirectAttributes.addFlashAttribute("error", "Email hoặc mật khẩu không chính xác.");
            return "redirect:/sign-in?error=true";
        }
    }

    @PostMapping("/logout")
    public String processLogout(HttpServletResponse response) {
        // 1. Tạo một cookie rỗng (thời gian sống = 0)
        Cookie emptyCookie = jwtUtil.createEmptyJwtCookie();

        // 2. Ghi đè cookie cũ bằng cookie rỗng
        response.addCookie(emptyCookie);

        // 3. Điều hướng về trang đăng nhập
        return "redirect:/sign-in?logout";
    }

    private boolean isAdminAuthority(String authority) {
        return "ROLE_ADMIN".equalsIgnoreCase(authority) || "ADMIN".equalsIgnoreCase(authority);
    }
}