package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "TemporaryRegisters")
public class TemporaryRegister {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TemporaryRegisterId")
    private Integer temporaryRegisterId;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Username", nullable = false)
    private String username;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "FullName", nullable = false)
    private String fullName;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "ExpiresAt", nullable = false)
    private LocalDateTime expiresAt;

    public TemporaryRegister() {
        // For JPA
    }

    public TemporaryRegister(String email, String username, String encodedPassword, String fullName, LocalDateTime expiresAt) {
        this.email = Objects.requireNonNull(email, "Email không hợp lệ");
        this.username = Objects.requireNonNull(username, "Tên đăng nhập không hợp lệ");
        this.password = Objects.requireNonNull(encodedPassword, "Mật khẩu không hợp lệ");
        this.fullName = Objects.requireNonNull(fullName, "Họ tên không hợp lệ");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Hạn OTP không hợp lệ");
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}