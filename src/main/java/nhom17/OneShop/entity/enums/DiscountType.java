package nhom17.OneShop.entity.enums;

import java.util.Arrays;
import java.util.Locale;

public enum DiscountType {
    PERCENTAGE(0, "Phần trăm"),
    FIXED_AMOUNT(1, "Số tiền cố định");

    private final int code;
    private final String label;

    DiscountType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static DiscountType fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Loại giảm giá không hợp lệ");
        }
        try {
            return DiscountType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Loại giảm giá không hợp lệ: " + value, ex);
        }
    }

    public static DiscountType fromCode(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("Loại giảm giá không hợp lệ");
        }
        return Arrays.stream(values())
                .filter(v -> v.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Loại giảm giá không hợp lệ: " + code));
    }
}

