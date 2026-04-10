package nhom17.OneShop.entity.enums;

import java.util.Locale;

public enum ShippingMethod {
    STANDARD("Tiêu chuẩn"),
    EXPRESS("Hỏa tốc"),
    ECONOMY("Tiết kiệm");

    private final String label;

    ShippingMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ShippingMethod fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phương thức vận chuyển không hợp lệ");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return ShippingMethod.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Phương thức vận chuyển không hợp lệ: " + value, ex);
        }
    }
}

