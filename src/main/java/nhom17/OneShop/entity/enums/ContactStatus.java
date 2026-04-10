package nhom17.OneShop.entity.enums;

import java.util.Locale;

public enum ContactStatus {
    NEW("Mới"),
    IN_PROGRESS("Đang xử lý"),
    RESOLVED("Đã xử lý"),
    CLOSED("Đã đóng");

    private final String label;

    ContactStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ContactStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Trạng thái liên hệ không hợp lệ");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return ContactStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái liên hệ không hợp lệ: " + value, ex);
        }
    }
}

