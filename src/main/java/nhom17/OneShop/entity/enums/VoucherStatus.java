package nhom17.OneShop.entity.enums;

import java.util.Locale;

public enum VoucherStatus {
    ACTIVE("Đang hoạt động"),
    DISABLED("Đã vô hiệu"),
    EXPIRED("Hết hạn"),
    USED_UP("Đã dùng hết");

    private final String label;

    VoucherStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static VoucherStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Trạng thái voucher không hợp lệ");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return VoucherStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái voucher không hợp lệ: " + value, ex);
        }
    }
}

