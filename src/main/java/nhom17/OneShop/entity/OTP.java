package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "Otps")
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "OtpId")
    private UUID otpId;

    @Column(name = "Code", nullable = false)
    private String code;

    @Column(name = "Purpose")
    private String purpose;

    @Column(name = "ExpiresAt", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "IsUsed")
    private boolean isUsed = false;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "Email")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId")
    private User user;

    public OTP() {
        // For JPA
    }

    public OTP(String code, String purpose, LocalDateTime expiresAt, String email, User user) {
        this.code = code;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
        this.email = email;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.isUsed = false;
    }


    public void markUsed() {
        this.isUsed = true;
    }

}