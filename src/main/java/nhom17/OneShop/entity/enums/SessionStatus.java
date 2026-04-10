package nhom17.OneShop.entity.enums;

import java.util.Locale;

public enum SessionStatus {
    OPEN("Đang mở"),
    CLOSED("Đã đóng");

    private final String label;

    SessionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static SessionStatus fromLabel(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Trạng thái phiên chat không hợp lệ");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return SessionStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái phiên chat không hợp lệ: " + value, ex);
        }
    }
}

