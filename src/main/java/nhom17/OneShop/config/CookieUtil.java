package nhom17.OneShop.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
public class CookieUtil {

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    /**
     * Tạo hoặc Ghi đè Cookie
     * @param response HttpServletResponse
     * @param name Tên cookie
     * @param value Giá trị
     * @param maxAgeInSeconds Thời gian sống (giây)
     */
    public void createCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // Ngăn JS đọc (nếu có thể)
        cookie.setSecure(secureCookie);
        cookie.setPath("/");      // Áp dụng cho toàn bộ domain
        cookie.setMaxAge(maxAgeInSeconds);
        response.addCookie(cookie);
    }

    /**
     * Đọc giá trị Cookie
     * @param request HttpServletRequest
     * @param name Tên cookie
     * @return Giá trị (String) hoặc null nếu không tìm thấy
     */
    public String readCookie(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        return cookie != null ? cookie.getValue() : null;
    }

    /**
     * Xóa Cookie
     * @param response HttpServletResponse
     * @param name Tên cookie
     */
    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Hết hạn ngay lập tức
        response.addCookie(cookie);
    }
}