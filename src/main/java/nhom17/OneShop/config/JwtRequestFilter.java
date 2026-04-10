package nhom17.OneShop.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nhom17.OneShop.service.impl.CustomUserDetailsService; // Đảm bảo import đúng
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService; // Dùng lại service cũ của bạn

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String jwt = jwtUtil.getTokenFromRequest(request);
        String username = null;

        if (jwt != null) {
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (IllegalArgumentException e) {
                System.out.println("Không thể lấy JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token đã hết hạn");
                // Có thể xử lý xóa cookie ở đây nếu muốn
            } catch (JwtException e) {
                System.out.println("JWT Token không hợp lệ hoặc sai chữ ký");
                response.addCookie(jwtUtil.createEmptyJwtCookie());
            }
        }

        // Nếu có username và chưa được xác thực trong context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 1. Tải UserDetails (giống hệt cách formLogin đã làm)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 2. Xác thực token
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // 3. Tạo đối tượng Authentication
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. (QUAN TRỌNG) Đặt Authentication vào SecurityContext
                // Đây chính là bước "đăng nhập" cho request này
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Chuyển request cho filter tiếp theo
        filterChain.doFilter(request, response);
    }
}