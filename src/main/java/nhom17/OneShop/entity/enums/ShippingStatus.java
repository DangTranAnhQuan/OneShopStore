package nhom17.OneShop.entity.enums;

import java.util.Locale;

public enum ShippingStatus {
    CREATED("Đã khởi tạo"),
    PICKED_UP("Đã lấy hàng"),
    IN_TRANSIT("Đang giao"),
    DELIVERED("Đã giao"),
    FAILED("Giao thất bại");

    private final String label;

    ShippingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ShippingStatus fromLabel(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Trạng thái vận chuyển không hợp lệ");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return ShippingStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái vận chuyển không hợp lệ: " + value, ex);
        }
    }
}

