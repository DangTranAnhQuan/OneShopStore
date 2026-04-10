package nhom17.OneShop.entity.enums;

import java.util.Locale;

public enum OrderStatus {
    PENDING("Đang xử lý"),
    CONFIRMED("Đã xác nhận"),
    SHIPPING("Đang giao"),
    DELIVERY_FAILED("Giao hàng thất bại"),
    DELIVERED("Đã giao"),
    CANCELED("Đã hủy"),
    RETURNED("Trả hàng-Hoàn tiền");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static OrderStatus fromLabel(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return OrderStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ: " + value, ex);
        }
    }
}

