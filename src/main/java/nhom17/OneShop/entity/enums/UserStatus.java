package nhom17.OneShop.entity.enums;

import java.util.Locale;

public enum UserStatus {
    ACTIVE("Đang hoạt động"),
    INACTIVE("Ngưng hoạt động"),
    BANNED("Bị khóa");

    private final String label;

    UserStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static UserStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Trạng thái người dùng không hợp lệ");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return UserStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái người dùng không hợp lệ: " + value, ex);
        }
    }
}

