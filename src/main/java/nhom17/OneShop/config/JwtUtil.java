// Đặt file này trong package: nhom17.OneShop.config
package nhom17.OneShop.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 1. Lấy chuỗi secret key từ application.properties
    // QUAN TRỌNG: Hãy thêm một dòng vào file application.properties của bạn:
    // jwt.secret=day_la_mot_chuoi_bi_mat_rat_dai_va_an_toan_32_ky_tu
    @Value("${jwt.secret}")
    private String secret;

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    // 2. Thời gian hết hạn của token (7 ngày)
    private final long JWT_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    // 3. Tên của Cookie
    public static final String JWT_COOKIE_NAME = "jwt_token";

    // 4. Tạo SecretKey một lần
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 5. Trích xuất username (email) từ token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 6. Trích xuất thời gian hết hạn
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 7. Hàm helper để trích xuất một claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 8. Hàm helper để giải mã toàn bộ token (CÚ PHÁP MỚI CHO 0.12.5)
    private Claims extractAllClaims(String token) {
        return Jwts.parser() // Bỏ 'parserBuilder()'
                .verifyWith(getSigningKey()) // Dùng 'verifyWith' thay cho 'setSigningKey'
                .build()
                .parseSignedClaims(token) // Dùng 'parseSignedClaims' thay cho 'parseClaimsJws'
                .getPayload(); // Dùng 'getPayload' thay cho 'getBody'
    }

    // 9. Kiểm tra token đã hết hạn chưa
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 10. Tạo token mới từ UserDetails
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Bạn có thể thêm các thông tin khác vào claims nếu muốn
        // ví dụ: claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    // 11. (Hàm chính) Tạo chuỗi JWT (CÚ PHÁP MỚI CHO 0.12.5)
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // Dùng 'claims' (số nhiều) thay cho 'setClaims'
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSigningKey()) // Bỏ 'SignatureAlgorithm.HS256'
                .compact();
    }

    // 12. Xác thực token (quan trọng)
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // 13. Helper: Tạo Cookie từ token
    public Cookie createJwtCookie(String token) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, token);
        cookie.setHttpOnly(true); // Quan trọng: Ngăn JS truy cập
        cookie.setSecure(secureCookie);
        cookie.setPath("/"); // Cookie áp dụng cho toàn bộ domain
        cookie.setMaxAge((int) (JWT_EXPIRATION / 1000)); // Đặt thời gian sống cho cookie
        return cookie;
    }

    // 14. Helper: Tạo Cookie rỗng (để logout)
    public Cookie createEmptyJwtCookie() {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Hết hạn ngay lập tức
        return cookie;
    }

    // 15. Helper: Đọc token từ request
    public String getTokenFromRequest(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, JWT_COOKIE_NAME);
        return cookie != null ? cookie.getValue() : null;
    }
}