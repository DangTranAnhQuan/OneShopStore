package nhom17.OneShop.entity.enums;

import java.util.Locale;

public enum PaymentMethod {
    COD("COD", "Thanh toán khi nhận hàng"),
    VN_PAY("VN_PAY", "VNPay");

    private final String code;
    private final String label;

    PaymentMethod(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static PaymentMethod fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return PaymentMethod.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ: " + value, ex);
        }
    }
}
