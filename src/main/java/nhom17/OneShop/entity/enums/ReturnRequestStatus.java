package nhom17.OneShop.entity.enums;

import java.util.Locale;

public enum ReturnRequestStatus {
    PENDING("Đang chờ duyệt"),
    APPROVED("Đã chấp thuận"),
    REJECTED("Đã từ chối");

    private final String label;

    ReturnRequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ReturnRequestStatus fromLabel(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Trạng thái yêu cầu hoàn trả không hợp lệ");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return ReturnRequestStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái yêu cầu hoàn trả không hợp lệ: " + value, ex);
        }
    }
}

