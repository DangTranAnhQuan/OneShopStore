package nhom17.OneShop.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import nhom17.OneShop.entity.enums.DiscountType;
import nhom17.OneShop.entity.enums.VoucherStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {
    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Size(max = 30, message = "Mã khuyến mãi không được vượt quá 30 ký tự")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Mã khuyến mãi chỉ chứa chữ hoa và số")
    private String voucherCode;

    @NotBlank(message = "Tên chiến dịch không được để trống")
    @Size(max = 200, message = "Tên chiến dịch không được vượt quá 200 ký tự")
    private String campaignName;

    @NotNull(message = "Vui lòng chọn kiểu áp dụng")
    private DiscountType discountType;

    @NotNull(message = "Giá trị không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị phải là số dương")
    private BigDecimal value;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startsAt;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @FutureOrPresent(message = "Thời gian kết thúc không được nằm trong quá khứ")
    private LocalDateTime endsAt;

    @Min(value = 0, message = "Đơn hàng tối thiểu không được âm")
    private BigDecimal minimumOrderAmount;

    @Min(value = 0, message = "Giảm tối đa không được âm")
    private BigDecimal maxDiscountAmount;

    @Min(value = 1, message = "Giới hạn phải là số dương")
    private Integer totalUsageLimit;

    @Min(value = 1, message = "Giới hạn phải là số dương")
    private Integer perUserLimit;

    private VoucherStatus status = VoucherStatus.ACTIVE;
}
