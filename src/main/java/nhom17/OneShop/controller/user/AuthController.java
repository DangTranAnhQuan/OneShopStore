//package nhom17.OneShop.controller;
//
//import nhom17.OneShop.request.SignUpRequest;
//import nhom17.OneShop.service.OtpService;
//import nhom17.OneShop.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@Controller
//public class AuthController {
//
//    @Autowired
//    private UserService userService;
//
//    // Bổ sung OtpService
//    @Autowired
//    private OtpService otpService;
//
//    @GetMapping("/sign-in")
//    public String showSignInForm() {
//        return "user/account/sign-in";
//    }
//
//    @GetMapping("/sign-up")
//    public String showSignUpForm(Model model) {
//        model.addAttribute("user", new SignUpRequest());
//        return "user/account/sign-up";
//    }
//
//    // Cập nhật lại logic để chuyển sang trang xác thực OTP thay vì đăng nhập ngay
//    @PostMapping("/sign-up")
//    public String processSignUp(@ModelAttribute("user") SignUpRequest signUpRequest,
//                                RedirectAttributes redirectAttributes,
//                                Model model) { // Thêm Model
//        try {
//            userService.registerNewUser(signUpRequest);
//            // Thêm email vào model để trang OTP có thể sử dụng
//            model.addAttribute("email", signUpRequest.getEmail());
//            // Trả về trang xác thực OTP
//            return "user/account/verify-otp-simple";
//        } catch (RuntimeException e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/sign-up";
//        }
//    }
//
//    // ==================== BỔ SUNG LOGIC XÁC THỰC VÀ GỬI LẠI OTP ====================
//
//    @PostMapping("/verify-otp")
//    public String verifyOtp(@RequestParam("email") String email,
//                            @RequestParam("otp") String otp,
//                            RedirectAttributes redirectAttributes) {
//        try {
//            boolean isValid = userService.verifyEmailOtp(email, otp);
//            if (isValid) {
//                redirectAttributes.addFlashAttribute("success", "Xác thực thành công! Vui lòng đăng nhập.");
//                return "redirect:/sign-in";
//            } else {
//                redirectAttributes.addFlashAttribute("error", "Mã OTP không đúng hoặc đã hết hạn!");
//                // Giữ lại email để người dùng không phải nhập lại
//                redirectAttributes.addFlashAttribute("email", email);
//                return "redirect:/verify-otp?email=" + email;
//            }
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            redirectAttributes.addFlashAttribute("email", email);
//            return "redirect:/verify-otp?email=" + email;
//        }
//    }
//
//    @GetMapping("/verify-otp")
//    public String showVerifyOtpForm(@RequestParam("email") String email, Model model) {
//        model.addAttribute("email", email);
//        return "user/account/verify-otp-simple";
//    }
//
//    @GetMapping("/resend-otp")
//    public String resendOtp(@RequestParam("email") String email,
//                            RedirectAttributes redirectAttributes) {
//        try {
//            otpService.generateOtpForEmail(email, "Đăng ký");
//            redirectAttributes.addFlashAttribute("success", "Đã gửi lại mã OTP. Vui lòng kiểm tra email!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Không thể gửi lại OTP. Vui lòng thử lại!");
//        }
//        return "redirect:/verify-otp?email=" + email;
//    }
//
//    // ==================== BỔ SUNG TOÀN BỘ LOGIC QUÊN MẬT KHẨU ====================
//
//    @GetMapping("/forgot-password")
//    public String showForgotPasswordForm() {
//        return "user/account/forgot-password";
//    }
//
//    @PostMapping("/forgot-password")
//    public String processForgotPassword(@RequestParam("email") String email,
//                                        RedirectAttributes redirectAttributes,
//                                        Model model) {
//        try {
//            userService.sendResetPasswordOtp(email);
//            model.addAttribute("email", email);
//            return "user/account/verify-reset-password";
//        } catch (RuntimeException e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/forgot-password";
//        }
//    }
//
//    @PostMapping("/verify-reset-password")
//    public String verifyResetPasswordOtp(@RequestParam("email") String email,
//                                         @RequestParam("otp") String otp,
//                                         RedirectAttributes redirectAttributes,
//                                         Model model) {
//        try {
//            boolean isValid = userService.verifyResetPasswordOtp(email, otp);
//            if (isValid) {
//                model.addAttribute("email", email);
//                return "user/account/reset-password";
//            } else {
//                redirectAttributes.addFlashAttribute("error", "Mã OTP không đúng hoặc đã hết hạn!");
//                redirectAttributes.addFlashAttribute("email", email);
//                return "redirect:/verify-reset-password?email=" + email;
//            }
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/verify-reset-password?email=" + email;
//        }
//    }
//
//    @GetMapping("/verify-reset-password")
//    public String showVerifyResetPasswordForm(@RequestParam("email") String email, Model model) {
//        model.addAttribute("email", email);
//        return "user/account/verify-reset-password";
//    }
//
//    @PostMapping("/reset-password")
//    public String processResetPassword(@RequestParam("email") String email,
//                                       @RequestParam("newPassword") String newPassword,
//                                       @RequestParam("confirmPassword") String confirmPassword,
//                                       RedirectAttributes redirectAttributes) {
//        try {
//            if (!newPassword.equals(confirmPassword)) {
//                throw new RuntimeException("Mật khẩu xác nhận không khớp!");
//            }
//            if (newPassword.length() < 6) {
//                throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự!");
//            }
//            userService.resetPassword(email, newPassword);
//            redirectAttributes.addFlashAttribute("success", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
//            return "redirect:/sign-in";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            redirectAttributes.addFlashAttribute("email", email);
//            return "redirect:/reset-password?email=" + email;
//        }
//    }
//
//    @GetMapping("/reset-password")
//    public String showResetPasswordForm(@RequestParam("email") String email, Model model) {
//        model.addAttribute("email", email);
//        return "user/account/reset-password";
//    }
//
//    @GetMapping("/resend-reset-otp")
//    public String resendResetOtp(@RequestParam("email") String email,
//                                 RedirectAttributes redirectAttributes) {
//        try {
//            otpService.generateOtpForEmail(email, "Quên mật khẩu");
//            redirectAttributes.addFlashAttribute("success", "Đã gửi lại mã OTP. Vui lòng kiểm tra email!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Không thể gửi lại OTP. Vui lòng thử lại!");
//        }
//        return "redirect:/verify-reset-password?email=" + email;
//    }
//}
package nhom17.OneShop.controller.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import nhom17.OneShop.config.JwtUtil;
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
            otpService.generateOtpForEmail(email, "Đăng ký");
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
            // 1. Xác thực người dùng (giống hệt FailureHandler cũ)
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
            // 6. Logic điều hướng (giống hệt SuccessHandler cũ)
            for (GrantedAuthority auth : userDetails.getAuthorities()) {
                if ("ROLE_ADMIN".equals(auth.getAuthority())) {
                    return "redirect:/admin/dashboard";
                }
            }
            return "redirect:/";

        } catch (DisabledException e) {
            // Xử lý tài khoản bị khóa (logic từ FailureHandler cũ)
            redirectAttributes.addFlashAttribute("error", "Tài khoản của bạn đã bị khóa.");
            return "redirect:/sign-in?disabled=true";

        } catch (Exception e) {
            // Xử lý sai username/password (logic từ FailureHandler cũ)
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
}