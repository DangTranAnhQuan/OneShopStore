package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import nhom17.OneShop.entity.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Getter
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "Email")
    private String email;

    @Column(name = "Username")
    private String username;

    @Column(name = "Password")
    private String password;

    @Column(name = "FullName")
    private String fullName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private UserStatus status;

    @Column(name = "AvatarUrl")
    private String avatarUrl;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "EmailVerified")
    private Boolean emailVerified = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RoleId")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TierId")
    private MembershipTier membershipTier;

    @Column(name = "RewardPoints", nullable = false)
    private Integer rewardPoints = 0;


    public User() {
        // For JPA
    }

    public User(String fullName, String email, String username, String encodedPassword, Role role) {
        this.fullName = requireFullName(fullName);
        this.email = email;
        this.username = username;
        setEncodedPassword(encodedPassword);
        this.role = Objects.requireNonNull(role, "Vai trò không hợp lệ");
        this.status = UserStatus.ACTIVE;
        this.emailVerified = Boolean.TRUE;
    }

    public void updateProfile(String fullName, String email, String username, String phoneNumber, UserStatus status) {
        this.fullName = requireFullName(fullName);
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }

    public void updateContactInfo(String fullName, String phoneNumber) {
        this.fullName = requireFullName(fullName);
        this.phoneNumber = phoneNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeAvatar(String newAvatarUrl) {
        this.avatarUrl = newAvatarUrl;
    }

    public void changeStatus(UserStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "Trạng thái không hợp lệ");
    }

    public void setEncodedPassword(String encodedPassword) {
        if (!String.valueOf(encodedPassword).isBlank()) {
            this.password = encodedPassword;
        }
    }

    public void markEmailVerified() {
        this.emailVerified = Boolean.TRUE;
    }

    public void assignRole(Role role) {
        this.role = Objects.requireNonNull(role, "Vai trò không hợp lệ");
    }

    public void assignMembership(MembershipTier tier) {
        this.membershipTier = tier;
    }

    public void clearMembership() {
        this.membershipTier = null;
    }

    public void adjustPoints(int delta) {
        int current = Optional.ofNullable(this.rewardPoints).orElse(0);
        int updated = current + delta;
        this.rewardPoints = Math.max(0, updated);
    }

    public void updatePasswordAndTimestamp(String encodedPassword) {
        setEncodedPassword(encodedPassword);
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String requireFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Họ tên không hợp lệ");
        }
        return fullName.trim();
    }
}